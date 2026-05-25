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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class SoggettoTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSerializzazioneDeserializzazione() throws Exception {
        Soggetto soggetto = new Soggetto()
                .tipo(TipoSoggetto.F)
                .identificativo("RSSMRA80A01H501U")
                .anagrafica("Mario Rossi")
                .indirizzo("Via Roma 1")
                .civico("1")
                .cap("00100")
                .localita("Roma")
                .provincia("RM")
                .nazione("IT")
                .email("mario.rossi@example.com")
                .cellulare("+39333123456");

        String json = objectMapper.writeValueAsString(soggetto);
        assertNotNull(json);
        assertTrue(json.contains("RSSMRA80A01H501U"));
        assertTrue(json.contains("Mario Rossi"));

        Soggetto deserialized = objectMapper.readValue(json, Soggetto.class);
        assertEquals(soggetto, deserialized);
    }

    @Test
    void testEqualsEHashCode() {
        Soggetto s1 = new Soggetto()
                .tipo(TipoSoggetto.F)
                .identificativo("RSSMRA80A01H501U")
                .anagrafica("Mario Rossi");

        Soggetto s2 = new Soggetto()
                .tipo(TipoSoggetto.F)
                .identificativo("RSSMRA80A01H501U")
                .anagrafica("Mario Rossi");

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());

        s2.setAnagrafica("Luigi Verdi");
        assertNotEquals(s1, s2);
    }

    @Test
    void testToString() {
        Soggetto soggetto = new Soggetto()
                .tipo(TipoSoggetto.G)
                .identificativo("01234567890")
                .anagrafica("Ente Creditore S.r.l.");

        String str = soggetto.toString();
        assertNotNull(str);
        assertTrue(str.contains("01234567890"));
        assertTrue(str.contains("Ente Creditore S.r.l."));
    }
}
