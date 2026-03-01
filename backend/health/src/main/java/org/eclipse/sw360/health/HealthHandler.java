/*
 * Copyright Bosch.IO 2020.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.health;

import com.ibm.cloud.cloudant.v1.Cloudant;
import org.eclipse.sw360.datahandler.common.DatabaseSettings;
import org.eclipse.sw360.health.db.HealthDatabaseHandler;
import org.eclipse.sw360.services.health.Health;
import org.eclipse.sw360.services.health.HealthService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.Set;

@Service
public class HealthHandler implements HealthService{

    private final HealthDatabaseHandler handler;

    public HealthHandler() throws MalformedURLException {
        handler = new HealthDatabaseHandler(DatabaseSettings.getConfiguredClient());
    }

    public HealthHandler(Cloudant client) throws MalformedURLException {
        handler = new HealthDatabaseHandler(client);
    }

    @Override
    public Health getHealth() {
        return handler.getHealth();
    }

    @Override
    public Health getHealthOfSpecificDbs(Set<String> dbsToCheck){
        return handler.getHealthOfSpecificDbs(dbsToCheck);
    }
}
