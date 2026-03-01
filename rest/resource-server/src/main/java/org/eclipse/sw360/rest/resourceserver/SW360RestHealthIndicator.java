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
import org.eclipse.sw360.services.health.HealthService;
import org.eclipse.sw360.services.health.Status;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SW360RestHealthIndicator implements HealthIndicator {
    private final HealthService healthService;

    public SW360RestHealthIndicator(HealthService healthService) {
        this.healthService = healthService;
    }

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
        restState.isServiceReachable = isServiceReachable(exception);
        return restState;
    }

    private boolean isServiceReachable(List<Exception> exception) {
        try {
            final org.eclipse.sw360.services.health.Health serviceHealth = healthService.getHealth();
            if (serviceHealth.getStatus().equals(Status.UP)) {
                return true;
            } else {
                exception.add(
                        new Exception(serviceHealth.getStatus().toString(),
                                new Throwable(serviceHealth.getDetails().toString())));
                return false;
            }
        } catch (Exception e) {
            exception.add(e);
            return false;
        }
    }

    public static class RestState {
        @JsonInclude
        public boolean isServiceReachable;

        boolean isUp() {
            return isServiceReachable;
        }
    }
}
