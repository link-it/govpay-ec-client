package it.govpay.ec.client.beans;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SoggettoTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
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
