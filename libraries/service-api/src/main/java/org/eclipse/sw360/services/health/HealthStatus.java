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

/**
 * Replaces the Thrift-generated {@code org.eclipse.sw360.datahandler.thrift.health.Status} enum.
 * Plain Java — no Thrift dependency.
 */
public enum HealthStatus {
    UP,
    UNKNOWN,
    DOWN,
    ERROR
}
