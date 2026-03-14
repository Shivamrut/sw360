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

import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.sw360.datahandler.cloudantclient.DatabaseInstanceCloudant;
import org.eclipse.sw360.datahandler.common.DatabaseSettings;
import org.eclipse.sw360.services.health.HealthResponse;
import org.eclipse.sw360.services.health.HealthStatus;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class SW360RestHealthIndicator implements HealthIndicator {

    private static final String HEALTH_SERVICE_URL = "http://localhost:8080/health/api/health";

    @Override
    public Health health() {
        List<Exception> exceptions = new ArrayList<>();
        RestState restState = check(exceptions);
        final String rest_state_detail = "Rest State";
        if (!restState.isUp()) {
            Health.Builder builderWithDetails = Health.down()
                    .withDetail(rest_state_detail, restState);
            for (Exception exception : exceptions) {
                builderWithDetails = builderWithDetails.withException(exception);
            }
            return builderWithDetails
                    .build();
        }
        return Health.up()
                .withDetail(rest_state_detail, restState)
                .build();
    }

    private RestState check(List<Exception> exception) {
        RestState restState = new RestState();
        restState.isDbReachable = isDbReachable(exception);
        restState.isHealthServiceReachable = isHealthServiceReachable(exception);
        return restState;
    }

    private boolean isDbReachable(List<Exception> exception) {
        DatabaseInstanceCloudant databaseInstance = makeDatabaseInstance();
        try {
            return databaseInstance.checkIfDbExists(DatabaseSettings.COUCH_DB_ATTACHMENTS);
        } catch (Exception e) {
            exception.add(e);
            return false;
        }
    }

    private boolean isHealthServiceReachable(List<Exception> exception) {
        try {
            RestTemplate restTemplate = createRestTemplate();
            HealthResponse response = restTemplate.getForObject(HEALTH_SERVICE_URL, HealthResponse.class);
            if (response != null && HealthStatus.UP.equals(response.getStatus())) {
                return true;
            } else {
                String details = response != null ? String.valueOf(response.getDetails()) : "null response";
                String status = response != null ? response.getStatus().toString() : "NO_RESPONSE";
                exception.add(new Exception(status, new Throwable(details)));
                return false;
            }
        } catch (Exception e) {
            exception.add(e);
            return false;
        }
    }

    protected RestTemplate createRestTemplate() {
        return new RestTemplate();
    }

    protected DatabaseInstanceCloudant makeDatabaseInstance() {
        return new DatabaseInstanceCloudant(DatabaseSettings.getConfiguredClient());
    }

    public static class RestState {
        @JsonInclude
        public boolean isDbReachable;

        @JsonInclude
        public boolean isHealthServiceReachable;

        boolean isUp() {
            return isDbReachable && isHealthServiceReachable;
        }
    }
}
