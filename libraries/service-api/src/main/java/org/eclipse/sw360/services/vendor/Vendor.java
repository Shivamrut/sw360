/*
 * Copyright Shivamrut<gshivamrut@gmail.com>, 2026. Part of the SW360 Portal Project.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.services.vendor;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Vendor {

    private String id;
    private String revision;
    private String type = "vendor";
    private String shortname;
    private String fullname;
    private String url;
    private Map<String, Boolean> permissions;

    public Vendor() {}

    public String getId() { return id; }
    public Vendor setId(String id) { this.id = id; return this; }

    public String getRevision() { return revision; }
    public Vendor setRevision(String revision) { this.revision = revision; return this; }

    public String getType() { return type; }
    public Vendor setType(String type) { this.type = type; return this; }

    public String getShortname() { return shortname; }
    public Vendor setShortname(String shortname) { this.shortname = shortname; return this; }

    public String getFullname() { return fullname; }
    public Vendor setFullname(String fullname) { this.fullname = fullname; return this; }

    public String getUrl() { return url; }
    public Vendor setUrl(String url) { this.url = url; return this; }

    public Map<String, Boolean> getPermissions() { return permissions; }
    public Vendor setPermissions(Map<String, Boolean> permissions) { this.permissions = permissions; return this; }
}
