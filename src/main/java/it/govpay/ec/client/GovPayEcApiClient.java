/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC
 * http://www.gov4j.it/govpay
 *
 * Copyright (c) 2014-2026 Link.it srl (http://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.govpay.ec.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import it.govpay.ec.client.api.impl.ApiClient;
import it.govpay.ec.client.beans.Documento;
import it.govpay.ec.client.beans.NuovaVocePendenza;
import it.govpay.ec.client.beans.NuovoDocumento;
import it.govpay.ec.client.beans.TipoRiferimentoDocumento;
import it.govpay.ec.client.beans.TipoRiferimentoVocePendenza;
import it.govpay.ec.client.beans.VocePendenza;
import it.govpay.ec.client.beans.VocePendenzaPagata;

/**
 * Client EC compatibile con i payload legacy: il tipo di voce/documento viene
 * dedotto dai campi, senza richiedere o emettere tipoRiferimento.
 * Le API generate accettano questa classe come ApiClient.
 */
public class GovPayEcApiClient extends ApiClient {

    public GovPayEcApiClient() {
        super();
        configuraJsonLegacy();
    }

    /**
     * Conserva autenticazione, interceptor, timeout e observation del RestTemplate.
     * Il template deve essere dedicato al client EC: ne vengono sostituiti i soli
     * converter JSON. I mapper originali (anche se condivisi) non sono modificati.
     */
    public GovPayEcApiClient(RestTemplate restTemplate) {
        super(restTemplate);
        configuraJsonLegacy();
    }

    private void configuraJsonLegacy() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        boolean configurato = false;
        for (HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
            if (converter instanceof JacksonJsonHttpMessageConverter json) {
                var builder = json.getMapper().rebuild();
                // Il generatore ripete JsonTypeInfo anche sui figli: non basta
                // registrare il mix-in soltanto sulla classe base.
                for (Class<?> tipo : List.of(TipoRiferimentoVocePendenza.class,
                        NuovaVocePendenza.class, VocePendenza.class, VocePendenzaPagata.class,
                        TipoRiferimentoDocumento.class, NuovoDocumento.class, Documento.class)) {
                    builder.addMixIn(tipo, SenzaDiscriminator.class);
                }
                var compatibile = new JacksonJsonHttpMessageConverter(builder.build());
                compatibile.setSupportedMediaTypes(json.getSupportedMediaTypes());
                compatibile.setDefaultCharset(json.getDefaultCharset());
                converters.add(compatibile);
                configurato = true;
            } else {
                converters.add(converter);
            }
        }
        if (!configurato) {
            throw new IllegalArgumentException("Il client EC richiede un converter JSON Jackson 3");
        }
        restTemplate.setMessageConverters(converters);
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    @JsonIgnoreProperties(value = "tipoRiferimento")
    private abstract static class SenzaDiscriminator {
    }
}
