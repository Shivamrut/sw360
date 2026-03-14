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

public enum AddDocumentRequestStatus {
    SUCCESS(0),
    DUPLICATE(1),
    FAILURE(2),
    NAMINGERROR(3),
    INVALID_INPUT(4),
    INVALID_SOURCE_CODE_URL(5);

    private final int value;

    AddDocumentRequestStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AddDocumentRequestStatus findByValue(int value) {
        for (AddDocumentRequestStatus s : values()) {
            if (s.value == value) return s;
        }
        return null;
    }
}
