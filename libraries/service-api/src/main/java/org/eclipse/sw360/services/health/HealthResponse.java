/*
 * Copyright Shivamrut<gshivamrut@gmail.com>, 2026. Part of the SW360 Portal Project.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.services.health;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Replaces the Thrift-generated {@code org.eclipse.sw360.datahandler.thrift.health.Health} struct.
 * Plain Java POJO — no Thrift dependency, JSON-serializable via Jackson.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthResponse {

    private HealthStatus status;
    private Map<String, String> details;

    public HealthResponse() {
        this.status = HealthStatus.UNKNOWN;
    }

    public HealthStatus getStatus() {
        return status;
    }

    public HealthResponse setStatus(HealthStatus status) {
        this.status = status;
        return this;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public HealthResponse setDetails(Map<String, String> details) {
        this.details = details;
        return this;
    }
}
