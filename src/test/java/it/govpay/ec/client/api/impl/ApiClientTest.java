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
