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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;
import it.govpay.ec.client.api.VerificaPendenzeApi;
import it.govpay.ec.client.beans.*;

class GovPayEcApiClientTest {
    private static final String PAYLOAD = """
        {"stato":"NON_ESEGUITA","pendenza":{"idA2A":"APP","idPendenza":"P1",
         "documento":{"identificativo":"DOC","descrizione":"Documento","rata":2},
         "voci":[
          {"idVocePendenza":"1","importo":10,"descrizione":"tributo","codEntrata":"TARI"},
          {"idVocePendenza":"2","importo":20,"descrizione":"inline",
           "ibanAccredito":"IT60X0542811101000000123456","ibanAppoggio":"IT60X0542811101000000123457",
           "codiceTassonomicoPagoPA":"9/0101101IM/"},
          {"idVocePendenza":"3","importo":16,"descrizione":"bollo","tipoBollo":"01",
           "hashDocumento":"hash-test","provinciaResidenza":"RM","codiceTassonomicoPagoPA":"9/0101101IM/"}
         ]}}
        """;

    static class ClientTestabile extends GovPayEcApiClient {
        RestTemplate template() { return restTemplate; }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void leggeETrasmetteLeTreVariantiAttraversoLeApiGenerate(boolean fornito) {
        RestTemplate template;
        GovPayEcApiClient client;
        if (fornito) {
            template = new RestTemplate();
            template.getInterceptors().add((request, body, execution) -> {
                request.getHeaders().set("X-EC-Test", "preservato");
                return execution.execute(request, body);
            });
            client = new GovPayEcApiClient(template);
        } else {
            var predefinito = new ClientTestabile();
            client = predefinito;
            template = predefinito.template();
        }
        client.setBasePath("https://ente.test/ec/v2");
        var server = MockRestServiceServer.bindTo(template).build();
        var attesa = server.expect(requestTo("https://ente.test/ec/v2/avvisi/01234567890/301000000000000001"))
                .andExpect(method(HttpMethod.GET));
        if (fornito) attesa.andExpect(header("X-EC-Test", "preservato"));
        attesa.andRespond(withSuccess(PAYLOAD, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://ente.test/ec/v2/test"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("tipoRiferimento"))))
                .andExpect(jsonPath("$.voci[0].codEntrata").value("TARI"))
                .andExpect(jsonPath("$.voci[1].ibanAccredito").value("IT60X0542811101000000123456"))
                .andExpect(jsonPath("$.voci[2].hashDocumento").value("hash-test"))
                .andRespond(withSuccess());
        var api = new VerificaPendenzeApi(client);
        var risultato = api.getAvviso("01234567890", "301000000000000001");
        var voci = risultato.getPendenza().getVoci();
        assertEquals("TARI", voci.get(0).getCodEntrata());
        assertEquals("IT60X0542811101000000123457", voci.get(1).getIbanAppoggio());
        assertEquals("9/0101101IM/", voci.get(1).getCodiceTassonomicoPagoPA());
        assertEquals("01", voci.get(2).getTipoBollo().getValue());
        assertEquals("RM", voci.get(2).getProvinciaResidenza());
        assertEquals(2, risultato.getPendenza().getDocumento().getRata().intValueExact());
        api.invokeAPI("/test", HttpMethod.POST, risultato.getPendenza(), new ParameterizedTypeReference<Void>() {});
        server.verify();
    }

    @Test
    void conservaConfigurazioneDelMapperSenzaModificareQuelloCondiviso() {
        JsonMapper originale = JsonMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES).build();
        var converter = new JacksonJsonHttpMessageConverter(originale);
        RestTemplate template = new RestTemplate();
        template.setMessageConverters(List.of(converter));
        new GovPayEcApiClient(template);
        var configurato = (JacksonJsonHttpMessageConverter) template.getMessageConverters().get(0);
        assertNotSame(converter, configurato);
        assertSame(originale, converter.getMapper());
        assertThrows(tools.jackson.databind.exc.InvalidTypeIdException.class,
                () -> originale.readValue("{\"codEntrata\":\"TARI\"}", NuovaVocePendenza.class));
        var voce = configurato.getMapper().readValue("{\"CODENTRATA\":\"TARI\"}", NuovaVocePendenza.class);
        assertEquals("TARI", voce.getCodEntrata());
    }

    @Test
    void copreAncheVociLetteEPagateETolleraDiscriminatorOpzionale() {
        var client = new ClientTestabile();
        var mapper = client.template().getMessageConverters().stream()
                .filter(JacksonJsonHttpMessageConverter.class::isInstance)
                .map(JacksonJsonHttpMessageConverter.class::cast).findFirst().orElseThrow().getMapper();
        for (Class<? extends TipoRiferimentoVocePendenza> tipo : List.of(
                TipoRiferimentoVocePendenza.class, NuovaVocePendenza.class, VocePendenza.class, VocePendenzaPagata.class)) {
            var voce = mapper.readValue("{\"codEntrata\":\"TARI\",\"tipoRiferimento\":\"riferimentoEntrata\"}", tipo);
            assertEquals("TARI", voce.getCodEntrata());
            assertFalse(mapper.writeValueAsString(voce).contains("tipoRiferimento"));
        }
    }
}
