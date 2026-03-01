/*
 * Copyright Bosch.IO GmbH 2020, 2025. Part of the SW360 Portal Project.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.services.health;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
public class Health {

    private Status status;
    private Map<String, String> details;

    public Health() {
        this.status = Status.UNKNOWN;
        this.details = new HashMap<>();
    }
}
