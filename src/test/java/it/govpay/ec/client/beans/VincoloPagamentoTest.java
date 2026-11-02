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
package it.govpay.ec.client.beans;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class VincoloPagamentoTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @ParameterizedTest
    @EnumSource(value = TipoSogliaVincoloPagamento.class, names = { "RIDOTTO", "SCONTATO" })
    void testSogliaSenzaGiorni(TipoSogliaVincoloPagamento tipo) throws Exception {
        VincoloPagamento vincolo = new VincoloPagamento().tipo(tipo);

        String json = objectMapper.writeValueAsString(vincolo);
        assertTrue(json.contains("\"tipo\":\"" + tipo.getValue() + "\""));

        VincoloPagamento deserialized = objectMapper.readValue("{\"tipo\":\"" + tipo.getValue() + "\"}", VincoloPagamento.class);
        assertEquals(tipo, deserialized.getTipo());
        assertNull(deserialized.getGiorni());
    }

    @ParameterizedTest
    @EnumSource(value = TipoSogliaVincoloPagamento.class, names = { "ENTRO", "OLTRE" })
    void testSogliaConGiorni(TipoSogliaVincoloPagamento tipo) throws Exception {
        VincoloPagamento vincolo = new VincoloPagamento().tipo(tipo).giorni(new BigDecimal(5));

        String json = objectMapper.writeValueAsString(vincolo);
        VincoloPagamento deserialized = objectMapper.readValue(json, VincoloPagamento.class);
        assertEquals(vincolo, deserialized);
    }
}
