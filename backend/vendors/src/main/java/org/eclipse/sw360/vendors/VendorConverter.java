/*
 * Copyright Shivamrut<gshivamrut@gmail.com>, 2026. Part of the SW360 Portal Project.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.vendors;

import org.eclipse.sw360.services.common.AddDocumentRequestStatus;
import org.eclipse.sw360.services.common.AddDocumentRequestSummary;
import org.eclipse.sw360.services.common.PaginationData;
import org.eclipse.sw360.services.common.RequestStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class VendorConverter {

    private VendorConverter() {}

    public static org.eclipse.sw360.services.vendor.Vendor fromThrift(
            org.eclipse.sw360.datahandler.thrift.vendors.Vendor thrift) {
        if (thrift == null) return null;
        org.eclipse.sw360.services.vendor.Vendor pojo = new org.eclipse.sw360.services.vendor.Vendor()
                .setId(thrift.getId())
                .setRevision(thrift.getRevision())
                .setType(thrift.getType())
                .setShortname(thrift.getShortname())
                .setFullname(thrift.getFullname())
                .setUrl(thrift.getUrl());
        if (thrift.getPermissions() != null) {
            Map<String, Boolean> perms = new HashMap<>();
            thrift.getPermissions().forEach((k, v) -> perms.put(k.name(), v));
            pojo.setPermissions(perms);
        }
        return pojo;
    }

    public static org.eclipse.sw360.datahandler.thrift.vendors.Vendor toThrift(
            org.eclipse.sw360.services.vendor.Vendor pojo) {
        if (pojo == null) return null;
        org.eclipse.sw360.datahandler.thrift.vendors.Vendor thrift =
                new org.eclipse.sw360.datahandler.thrift.vendors.Vendor();
        if (pojo.getId() != null) thrift.setId(pojo.getId());
        if (pojo.getRevision() != null) thrift.setRevision(pojo.getRevision());
        thrift.setShortname(pojo.getShortname());
        thrift.setFullname(pojo.getFullname());
        thrift.setUrl(pojo.getUrl());
        return thrift;
    }

    public static List<org.eclipse.sw360.services.vendor.Vendor> fromThriftList(
            List<org.eclipse.sw360.datahandler.thrift.vendors.Vendor> thriftList) {
        if (thriftList == null) return null;
        return thriftList.stream().map(VendorConverter::fromThrift).collect(Collectors.toList());
    }

    public static RequestStatus fromThriftRequestStatus(
            org.eclipse.sw360.datahandler.thrift.RequestStatus thrift) {
        if (thrift == null) return null;
        return RequestStatus.findByValue(thrift.getValue());
    }

    public static AddDocumentRequestSummary fromThriftAddDocSummary(
            org.eclipse.sw360.datahandler.thrift.AddDocumentRequestSummary thrift) {
        if (thrift == null) return null;
        AddDocumentRequestSummary pojo = new AddDocumentRequestSummary();
        if (thrift.getRequestStatus() != null) {
            pojo.setRequestStatus(AddDocumentRequestStatus.findByValue(thrift.getRequestStatus().getValue()));
        }
        pojo.setId(thrift.getId());
        pojo.setMessage(thrift.getMessage());
        return pojo;
    }

    public static PaginationData fromThriftPaginationData(
            org.eclipse.sw360.datahandler.thrift.PaginationData thrift) {
        if (thrift == null) return null;
        return new PaginationData()
                .setRowsPerPage(thrift.getRowsPerPage())
                .setDisplayStart(thrift.getDisplayStart())
                .setAscending(thrift.isAscending())
                .setSortColumnNumber(thrift.getSortColumnNumber())
                .setTotalRowCount(thrift.getTotalRowCount());
    }

    public static org.eclipse.sw360.datahandler.thrift.PaginationData toThriftPaginationData(
            PaginationData pojo) {
        if (pojo == null) return null;
        return new org.eclipse.sw360.datahandler.thrift.PaginationData()
                .setRowsPerPage(pojo.getRowsPerPage())
                .setDisplayStart(pojo.getDisplayStart())
                .setAscending(pojo.isAscending())
                .setSortColumnNumber(pojo.getSortColumnNumber())
                .setTotalRowCount(pojo.getTotalRowCount());
    }

    public static Set<String> fromThriftVendorNames(Set<String> names) {
        return names;
    }
}
