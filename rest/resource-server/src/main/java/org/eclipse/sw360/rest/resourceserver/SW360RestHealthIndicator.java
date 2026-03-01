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
import org.eclipse.sw360.datahandler.common.DatabaseSettings;
import org.eclipse.sw360.services.health.HealthService;
import org.eclipse.sw360.services.health.Status;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class SW360RestHealthIndicator implements HealthIndicator {

    private final HealthService healthService;

    public SW360RestHealthIndicator(HealthService healthService) {
        this.healthService = healthService;
    }

    @Override
    public Health health() {
        boolean isDbReachable = isDbReachable();

        org.eclipse.sw360.services.health.Health serviceHealth;
        try {
            serviceHealth = healthService.getHealth();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("isDbReachable", isDbReachable)
                    .withDetail("serviceStatus", "UNREACHABLE")
                    .withException(e)
                    .build();
        }

        Health.Builder builder = (isDbReachable && serviceHealth.getStatus() == Status.UP)
                ? Health.up()
                : Health.down();

        builder.withDetail("isDbReachable", isDbReachable);
        builder.withDetail("serviceStatus", serviceHealth.getStatus());

        serviceHealth.getDetails().forEach((db, message) ->
                builder.withDetail("db." + db, message));

        return builder.build();
    }

    private boolean isDbReachable() {
        try {
            return makeDatabaseInstance().checkIfDbExists(DatabaseSettings.COUCH_DB_ATTACHMENTS);
        } catch (Exception e) {
            return false;
        }
    }

    protected DatabaseInstanceCloudant makeDatabaseInstance() {
        return new DatabaseInstanceCloudant(DatabaseSettings.getConfiguredClient());
    }
}
