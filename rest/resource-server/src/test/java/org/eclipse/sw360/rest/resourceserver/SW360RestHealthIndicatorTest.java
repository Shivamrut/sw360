/*
 * Copyright Bosch.IO GmbH 2020
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.rest.resourceserver;

import org.eclipse.sw360.datahandler.cloudantclient.DatabaseInstanceCloudant;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.datahandler.thrift.users.UserGroup;
import org.eclipse.sw360.services.health.Health;
import org.eclipse.sw360.services.health.HealthService;
import org.eclipse.sw360.services.health.Status;
import org.eclipse.sw360.rest.resourceserver.user.Sw360UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/* @DirtiesContext is necessary because the context needs to be reloaded inbetween the tests
    otherwise the responses of previous tests are taken. NoOpCacheManager through @AutoConfigureCache
    was not enough to avoid this bug.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Sw360ResourceServer.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SW360RestHealthIndicatorTest {

    @LocalServerPort
    private int port;

    @MockitoSpyBean
    private SW360RestHealthIndicator restHealthIndicatorMock;

    @MockitoBean
    private HealthService healthServiceMock;

    @MockitoBean
    private Sw360UserService userServiceMock;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private static final String IS_DB_REACHABLE = "isDbReachable";
    private static final String SERVICE_STATUS  = "serviceStatus";
    private static final String ERROR           = "error";

    @Before
    public void before() {
        given(this.userServiceMock.getUserByEmailOrExternalId("admin@sw360.org")).willReturn(
                new User("admin@sw360.org", "sw360").setId("123456789").setUserGroup(UserGroup.ADMIN));
    }

    private ResponseEntity<Map> getMapResponseEntityForHealthEndpointRequest(String endpoint) {
        return this.testRestTemplate.getForEntity(
                "http://localhost:" + this.port + Sw360ResourceServer.REST_BASE_PATH + endpoint, Map.class);
    }

    private LinkedHashMap getSW360RestDetails(ResponseEntity<Map> entity) {
        LinkedHashMap body        = (LinkedHashMap) entity.getBody();
        LinkedHashMap components  = (LinkedHashMap) body.get("components");
        LinkedHashMap sw360Rest   = (LinkedHashMap) components.get("SW360Rest");
        return (LinkedHashMap) sw360Rest.get("details");
    }

    @Test
    public void info_should_return_200() {
        ResponseEntity<Map> entity = getMapResponseEntityForHealthEndpointRequest("/info");
        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void health_should_return_503_with_missing_db() throws Exception {
        DatabaseInstanceCloudant databaseInstanceMock = mock(DatabaseInstanceCloudant.class);
        when(databaseInstanceMock.checkIfDbExists(anyString())).thenReturn(false);
        when(restHealthIndicatorMock.makeDatabaseInstance()).thenReturn(databaseInstanceMock);

        Health health = new Health().setStatus(Status.UP);
        when(healthServiceMock.getHealth()).thenReturn(health);

        ResponseEntity<Map> entity = getMapResponseEntityForHealthEndpointRequest("/health");

        then(entity.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        LinkedHashMap details = getSW360RestDetails(entity);
        assertFalse((Boolean) details.get(IS_DB_REACHABLE));
        assertEquals("UP", details.get(SERVICE_STATUS).toString());
    }

    @Test
    public void health_should_return_503_with_unhealthy_service() throws Exception {
        DatabaseInstanceCloudant databaseInstanceMock = mock(DatabaseInstanceCloudant.class);
        when(databaseInstanceMock.checkIfDbExists(anyString())).thenReturn(true);
        when(restHealthIndicatorMock.makeDatabaseInstance()).thenReturn(databaseInstanceMock);

        Health health = new Health()
                .setStatus(Status.DOWN)
                .setDetails(Collections.singletonMap("sw360db", "The database 'sw360db' does not exist."));
        when(healthServiceMock.getHealth()).thenReturn(health);

        ResponseEntity<Map> entity = getMapResponseEntityForHealthEndpointRequest("/health");

        then(entity.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        LinkedHashMap details = getSW360RestDetails(entity);
        assertTrue((Boolean) details.get(IS_DB_REACHABLE));
        assertEquals("DOWN", details.get(SERVICE_STATUS).toString());
        assertNotNull(details.get("db.sw360db"));
    }

    @Test
    public void health_should_return_503_with_unreachable_service() throws Exception {
        DatabaseInstanceCloudant databaseInstanceMock = mock(DatabaseInstanceCloudant.class);
        when(databaseInstanceMock.checkIfDbExists(anyString())).thenReturn(true);
        when(restHealthIndicatorMock.makeDatabaseInstance()).thenReturn(databaseInstanceMock);

        when(healthServiceMock.getHealth()).thenThrow(new RuntimeException("service unavailable"));

        ResponseEntity<Map> entity = getMapResponseEntityForHealthEndpointRequest("/health");

        then(entity.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        LinkedHashMap details = getSW360RestDetails(entity);
        assertTrue((Boolean) details.get(IS_DB_REACHABLE));
        assertEquals("UNREACHABLE", details.get(SERVICE_STATUS));
        assertNotNull(details.get(ERROR));
    }

    @Test
    public void health_should_return_503_with_throwable() throws Exception {
        DatabaseInstanceCloudant databaseInstanceMock = mock(DatabaseInstanceCloudant.class);
        when(databaseInstanceMock.checkIfDbExists(anyString())).thenThrow(new RuntimeException());
        when(restHealthIndicatorMock.makeDatabaseInstance()).thenReturn(databaseInstanceMock);

        Health health = new Health().setStatus(Status.UP);
        when(healthServiceMock.getHealth()).thenReturn(health);

        ResponseEntity<Map> entity = getMapResponseEntityForHealthEndpointRequest("/health");

        then(entity.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        LinkedHashMap details = getSW360RestDetails(entity);
        assertFalse((Boolean) details.get(IS_DB_REACHABLE));
    }

    @Test
    public void health_should_return_200_when_healthy() throws Exception {
        DatabaseInstanceCloudant databaseInstanceMock = mock(DatabaseInstanceCloudant.class);
        when(databaseInstanceMock.checkIfDbExists(anyString())).thenReturn(true);
        when(restHealthIndicatorMock.makeDatabaseInstance()).thenReturn(databaseInstanceMock);

        Health health = new Health().setStatus(Status.UP);
        when(healthServiceMock.getHealth()).thenReturn(health);

        ResponseEntity<Map> entity = getMapResponseEntityForHealthEndpointRequest("/health");

        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        LinkedHashMap details = getSW360RestDetails(entity);
        assertTrue((Boolean) details.get(IS_DB_REACHABLE));
        assertEquals("UP", details.get(SERVICE_STATUS).toString());
    }
}
