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

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddDocumentRequestSummary {

    private AddDocumentRequestStatus requestStatus;
    private String id;
    private String message;

    public AddDocumentRequestSummary() {}

    public AddDocumentRequestStatus getRequestStatus() { return requestStatus; }
    public AddDocumentRequestSummary setRequestStatus(AddDocumentRequestStatus requestStatus) {
        this.requestStatus = requestStatus;
        return this;
    }

    public String getId() { return id; }
    public AddDocumentRequestSummary setId(String id) { this.id = id; return this; }

    public String getMessage() { return message; }
    public AddDocumentRequestSummary setMessage(String message) { this.message = message; return this; }
}
