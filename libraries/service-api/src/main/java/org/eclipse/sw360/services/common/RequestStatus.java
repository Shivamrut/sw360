/*
 * Copyright Shivamrut<gshivamrut@gmail.com>, 2026. Part of the SW360 Portal Project.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.services.common;

public enum RequestStatus {
    SUCCESS(0),
    SENT_TO_MODERATOR(1),
    FAILURE(2),
    IN_USE(3),
    FAILED_SANITY_CHECK(4),
    DUPLICATE(5),
    DUPLICATE_ATTACHMENT(6),
    ACCESS_DENIED(7),
    CLOSED_UPDATE_NOT_ALLOWED(8),
    INVALID_INPUT(9),
    PROCESSING(10),
    NAMINGERROR(11),
    INVALID_SOURCE_CODE_URL(12);

    private final int value;

    RequestStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static RequestStatus findByValue(int value) {
        for (RequestStatus s : values()) {
            if (s.value == value) return s;
        }
        return null;
    }
}
