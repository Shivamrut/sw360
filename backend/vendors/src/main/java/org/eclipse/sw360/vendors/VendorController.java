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

import org.eclipse.sw360.services.common.AddDocumentRequestSummary;
import org.eclipse.sw360.services.common.AddDocumentRequestStatus;
import org.eclipse.sw360.services.common.PaginationData;
import org.eclipse.sw360.services.common.RequestStatus;
import org.eclipse.sw360.services.vendor.Vendor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaginatedVendorResponse {
        private PaginationData pagination;
        private List<Vendor> data;

        public PaginatedVendorResponse() {}
        public PaginatedVendorResponse(PaginationData pagination, List<Vendor> data) {
            this.pagination = pagination;
            this.data = data;
        }
        public PaginationData getPagination() { return pagination; }
        public void setPagination(PaginationData pagination) { this.pagination = pagination; }
        public List<Vendor> getData() { return data; }
        public void setData(List<Vendor> data) { this.data = data; }
    }

    private final VendorHandler vendorHandler;

    public VendorController(VendorHandler vendorHandler) {
        this.vendorHandler = vendorHandler;
    }

    private PaginatedVendorResponse toPaginatedResponse(Map<PaginationData, List<Vendor>> result) {
        if (result == null || result.isEmpty()) {
            return new PaginatedVendorResponse(null, List.of());
        }
        var entry = result.entrySet().iterator().next();
        return new PaginatedVendorResponse(entry.getKey(), entry.getValue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vendor> getByID(@PathVariable String id) {
        Vendor vendor = vendorHandler.getByID(id);
        if (vendor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vendor);
    }

    @GetMapping
    public ResponseEntity<PaginatedVendorResponse> getAllVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") int sort,
            @RequestParam(defaultValue = "true") boolean ascending) {
        PaginationData pageData = new PaginationData()
                .setDisplayStart(page * size)
                .setRowsPerPage(size)
                .setSortColumnNumber(sort)
                .setAscending(ascending);
        Map<PaginationData, List<Vendor>> result = vendorHandler.getAllVendorsPaginated(pageData);
        return ResponseEntity.ok(toPaginatedResponse(result));
    }

    @GetMapping("/names")
    public ResponseEntity<Set<String>> getAllVendorNames() {
        return ResponseEntity.ok(vendorHandler.getAllVendorNames());
    }

    @GetMapping("/search")
    public ResponseEntity<PaginatedVendorResponse> searchVendors(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") int sort,
            @RequestParam(defaultValue = "true") boolean ascending) {
        PaginationData pageData = new PaginationData()
                .setDisplayStart(page * size)
                .setRowsPerPage(size)
                .setSortColumnNumber(sort)
                .setAscending(ascending);
        Map<PaginationData, List<Vendor>> result = vendorHandler.searchVendors(q, pageData);
        return ResponseEntity.ok(toPaginatedResponse(result));
    }

    @GetMapping("/search/ids")
    public ResponseEntity<List<String>> searchVendorIds(@RequestParam String q) {
        return ResponseEntity.ok(vendorHandler.searchVendorIds(q));
    }

    @PostMapping
    public ResponseEntity<AddDocumentRequestSummary> addVendor(@RequestBody Vendor vendor) {
        AddDocumentRequestSummary summary = vendorHandler.addVendor(vendor);
        if (summary.getRequestStatus() == AddDocumentRequestStatus.SUCCESS) {
            return ResponseEntity.status(HttpStatus.CREATED).body(summary);
        } else if (summary.getRequestStatus() == AddDocumentRequestStatus.DUPLICATE) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(summary);
        }
        return ResponseEntity.badRequest().body(summary);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RequestStatus> updateVendor(
            @PathVariable String id,
            @RequestBody Vendor vendor,
            @RequestHeader(value = "X-SW360-User", required = false) String userEmail) {
        vendor.setId(id);
        RequestStatus status = vendorHandler.updateVendor(vendor, userEmail);
        if (status == RequestStatus.SUCCESS) {
            return ResponseEntity.ok(status);
        } else if (status == RequestStatus.DUPLICATE) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(status);
        } else if (status == RequestStatus.ACCESS_DENIED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(status);
        }
        return ResponseEntity.badRequest().body(status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RequestStatus> deleteVendor(
            @PathVariable String id,
            @RequestHeader(value = "X-SW360-User", required = false) String userEmail) {
        RequestStatus status = vendorHandler.deleteVendor(id, userEmail);
        if (status == RequestStatus.SUCCESS) {
            return ResponseEntity.ok(status);
        } else if (status == RequestStatus.ACCESS_DENIED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(status);
        }
        return ResponseEntity.badRequest().body(status);
    }

    @PostMapping("/merge")
    public ResponseEntity<RequestStatus> mergeVendors(
            @RequestParam String targetId,
            @RequestParam String sourceId,
            @RequestBody Vendor mergeSelection,
            @RequestHeader(value = "X-SW360-User", required = false) String userEmail) {
        RequestStatus status = vendorHandler.mergeVendors(targetId, sourceId, mergeSelection, userEmail);
        if (status == RequestStatus.SUCCESS) {
            return ResponseEntity.ok(status);
        } else if (status == RequestStatus.ACCESS_DENIED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(status);
        } else if (status == RequestStatus.IN_USE) {
            return ResponseEntity.badRequest().body(status);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(status);
    }

    @GetMapping("/report")
    public ResponseEntity<byte[]> getVendorReport() {
        byte[] data = vendorHandler.getVendorReportDataStream();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "vendors.xlsx");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
