package it.govpay.ec.client.api.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiClientTest {

    @Test
    void testCreazioneDefault() {
        ApiClient client = new ApiClient();

        assertNotNull(client);
        assertNotNull(client.getBasePath());
    }

    @Test
    void testSetBasePath() {
        ApiClient client = new ApiClient();
        client.setBasePath("http://localhost:8080/govpay/ec");

        assertEquals("http://localhost:8080/govpay/ec", client.getBasePath());
    }

    @Test
    void testSetUserAgent() {
        ApiClient client = new ApiClient();
        client.setUserAgent("govpay-ec-client/1.0.0");

        assertNotNull(client);
    }

    @Test
    void testAddDefaultHeader() {
        ApiClient client = new ApiClient();
        client.addDefaultHeader("X-Custom-Header", "test-value");

        assertNotNull(client);
    }
}
