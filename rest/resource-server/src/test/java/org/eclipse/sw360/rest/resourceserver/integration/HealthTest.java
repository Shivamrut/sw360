/*
 * Copyright Rohit Borra 2025.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.rest.resourceserver.integration;

import org.eclipse.sw360.rest.resourceserver.SW360RestHealthIndicator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(SpringJUnit4ClassRunner.class)
public class HealthTest extends TestIntegrationBase {

    @Value("${local.server.port}")
    private int port;

    @MockitoBean
    private SW360RestHealthIndicator restHealthIndicatorMock;

    @Before
    public void before() {
        Health springHealth = Health.up()
                .withDetail("isDbReachable", true)
                .withDetail("serviceStatus", "UP")
                .build();
        given(this.restHealthIndicatorMock.health()).willReturn(springHealth);
    }

    @Test
    public void should_get_health_status() throws IOException {
        HttpHeaders headers = getHeaders(port);
        ResponseEntity<String> response =
                new TestRestTemplate().exchange("http://localhost:" + port + "/api/health",
                        HttpMethod.GET,
                        new HttpEntity<>(null, headers),
                        String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String responseBody = response.getBody();
        assertTrue("Response should contain status", responseBody.contains("status"));
        assertTrue("Response should contain components", responseBody.contains("components"));
        assertTrue("Response should contain diskSpace", responseBody.contains("diskSpace"));
        assertTrue("Response should contain ping", responseBody.contains("ping"));
        assertTrue("Response should contain total", responseBody.contains("total"));
        assertTrue("Response should contain free", responseBody.contains("free"));
        assertTrue("Response should contain threshold", responseBody.contains("threshold"));
        assertTrue("Response should contain path", responseBody.contains("path"));
        assertTrue("Response should contain exists", responseBody.contains("exists"));
    }

    @Test
    public void should_get_health_status_unhealthy() throws IOException {
        Health springHealthDown = Health.down()
                .withDetail("isDbReachable", true)
                .withDetail("serviceStatus", "DOWN")
                .withException(new Exception("Fake"))
                .build();
        given(this.restHealthIndicatorMock.health()).willReturn(springHealthDown);

        HttpHeaders headers = getHeaders(port);
        ResponseEntity<String> response =
                new TestRestTemplate().exchange("http://localhost:" + port + "/api/health",
                        HttpMethod.GET,
                        new HttpEntity<>(null, headers),
                        String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String responseBody = response.getBody();
        assertTrue("Response should contain status", responseBody.contains("status"));
        assertTrue("Response should contain components", responseBody.contains("components"));
        assertTrue("Response should contain diskSpace", responseBody.contains("diskSpace"));
        assertTrue("Response should contain ping", responseBody.contains("ping"));
    }

    @Test
    public void should_get_health_status_without_authentication() throws IOException {
        ResponseEntity<String> response =
                new TestRestTemplate().exchange("http://localhost:" + port + "/api/health",
                        HttpMethod.GET,
                        new HttpEntity<>(null, new HttpHeaders()),
                        String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String responseBody = response.getBody();
        assertTrue("Response should contain status", responseBody.contains("status"));
        assertTrue("Response should contain components", responseBody.contains("components"));
    }
}
