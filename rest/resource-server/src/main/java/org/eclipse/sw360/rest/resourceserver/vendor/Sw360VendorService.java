/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Vendor.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.rest.resourceserver.vendor;

import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.eclipse.sw360.datahandler.common.CommonUtils;
import org.eclipse.sw360.datahandler.resourcelists.ResourceClassNotFoundException;
import org.eclipse.sw360.datahandler.thrift.AddDocumentRequestStatus;
import org.eclipse.sw360.datahandler.thrift.AddDocumentRequestSummary;
import org.eclipse.sw360.datahandler.thrift.PaginationData;
import org.eclipse.sw360.datahandler.thrift.RequestStatus;
import org.eclipse.sw360.datahandler.thrift.ThriftClients;
import org.eclipse.sw360.datahandler.thrift.users.RequestedAction;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.datahandler.thrift.vendors.Vendor;
import org.eclipse.sw360.datahandler.thrift.components.ComponentService;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.vendors.VendorSortColumn;
import org.eclipse.sw360.rest.resourceserver.core.BadRequestClientException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Sw360VendorService {

    @Value("${sw360.vendors-service-url:http://localhost:8080/vendors}")
    private String vendorServiceUrl;

    private final RestTemplate vendorRestTemplate = new RestTemplate();

    public Map<PaginationData, List<Vendor>> getVendors(Pageable pageable) {
        PaginationData pageData = pageableToPaginationData(pageable,
                VendorSortColumn.BY_FULLNAME, true);

        String url = vendorServiceUrl + "/api/vendors?page={page}&size={size}&sort={sort}&ascending={ascending}";
        Map<String, Object> params = buildPaginationParams(pageData);

        ResponseEntity<PaginatedVendorResponse> response =
                vendorRestTemplate.exchange(url, HttpMethod.GET, null,
                        new ParameterizedTypeReference<>() {}, params);

        return convertPaginatedResponse(response.getBody());
    }

    public Map<PaginationData, List<Vendor>> searchVendors(String searchText, Pageable pageable) {
        PaginationData pageData = pageableToPaginationData(pageable,
                VendorSortColumn.BY_FULLNAME, true);

        String url = vendorServiceUrl + "/api/vendors/search?q={q}&page={page}&size={size}&sort={sort}&ascending={ascending}";
        Map<String, Object> params = buildPaginationParams(pageData);
        params.put("q", searchText);

        ResponseEntity<PaginatedVendorResponse> response =
                vendorRestTemplate.exchange(url, HttpMethod.GET, null,
                        new ParameterizedTypeReference<>() {}, params);

        return convertPaginatedResponse(response.getBody());
    }

    public Vendor getVendorById(String vendorId) {
        String url = vendorServiceUrl + "/api/vendors/{id}";
        org.eclipse.sw360.services.vendor.Vendor pojo =
                vendorRestTemplate.getForObject(url, org.eclipse.sw360.services.vendor.Vendor.class, vendorId);
        return toThriftVendor(pojo);
    }

    public Vendor getVendorByFullName(String fullName) {
        List<Vendor> allVendors = getAllVendorList();
        for (Vendor vendor : allVendors) {
            if (fullName.equals(vendor.getFullname())) {
                return vendor;
            }
        }
        return null;
    }

    public Vendor createVendor(Vendor vendor) {
        if (CommonUtils.isNullEmptyOrWhitespace(vendor.getFullname()) || CommonUtils.isNullEmptyOrWhitespace(vendor.getShortname())
                || CommonUtils.isNullEmptyOrWhitespace(vendor.getUrl())) {
            throw new BadRequestClientException("A Vendor cannot have null or empty 'Full Name' or 'Short Name' or 'URL'!");
        }

        org.eclipse.sw360.services.vendor.Vendor pojoVendor = fromThriftVendor(vendor);
        String url = vendorServiceUrl + "/api/vendors";
        ResponseEntity<org.eclipse.sw360.services.common.AddDocumentRequestSummary> response =
                vendorRestTemplate.postForEntity(url,
                        pojoVendor,
                        org.eclipse.sw360.services.common.AddDocumentRequestSummary.class);

        org.eclipse.sw360.services.common.AddDocumentRequestSummary summary = response.getBody();
        if (summary != null && summary.getRequestStatus() ==
                org.eclipse.sw360.services.common.AddDocumentRequestStatus.SUCCESS) {
            vendor.setId(summary.getId());
            return vendor;
        } else if (summary != null && summary.getRequestStatus() ==
                org.eclipse.sw360.services.common.AddDocumentRequestStatus.DUPLICATE) {
            throw new DataIntegrityViolationException("A Vendor with same full name '" + vendor.getFullname() + "' and URL already exists!");
        } else if (summary != null && summary.getRequestStatus() ==
                org.eclipse.sw360.services.common.AddDocumentRequestStatus.FAILURE) {
            throw new BadRequestClientException(summary.getMessage());
        }
        return null;
    }

    public RequestStatus vendorUpdate(Vendor vendor, User sw360User, String id) {
        Vendor existingVendor = getVendorById(id);
        if (existingVendor != null) {
            if (vendor.getShortname() != null) {
                existingVendor.setShortname(vendor.getShortname());
            }
            if (vendor.getFullname() != null) {
                existingVendor.setFullname(vendor.getFullname());
            }
            if (vendor.getUrl() != null) {
                existingVendor.setUrl(vendor.getUrl());
            }
        }

        org.eclipse.sw360.services.vendor.Vendor pojoVendor = fromThriftVendor(existingVendor);
        String url = vendorServiceUrl + "/api/vendors/{id}";
        HttpHeaders headers = new HttpHeaders();
        if (sw360User != null && sw360User.getEmail() != null) {
            headers.set("X-SW360-User", sw360User.getEmail());
        }
        HttpEntity<org.eclipse.sw360.services.vendor.Vendor> entity = new HttpEntity<>(pojoVendor, headers);
        ResponseEntity<org.eclipse.sw360.services.common.RequestStatus> response =
                vendorRestTemplate.exchange(url, HttpMethod.PUT, entity,
                        org.eclipse.sw360.services.common.RequestStatus.class, id);

        org.eclipse.sw360.services.common.RequestStatus pojoStatus = response.getBody();
        return pojoStatus != null ? RequestStatus.findByValue(pojoStatus.getValue()) : RequestStatus.FAILURE;
    }

    public RequestStatus deleteVendorByid(String vendorId, User sw360User) throws TException {
        try {
            ComponentService.Iface componentClient = getThriftComponentClient();
            List<Release> releases = componentClient.getReleasesFromVendorId(vendorId, sw360User);

            if (releases.stream().anyMatch(release -> !release.getPermissions().get(RequestedAction.WRITE))) {
                throw new AccessDeniedException("You do not have permission to delete vendor with id " + vendorId);
            }

            for (Release release : releases) {
                if (release.isSetVendorId()) release.unsetVendorId();
                if (release.isSetVendor()) release.unsetVendor();
                RequestStatus status = componentClient.updateRelease(release, sw360User);
                if (status != RequestStatus.SUCCESS) return status;
            }

            String url = vendorServiceUrl + "/api/vendors/{id}";
            HttpHeaders headers = new HttpHeaders();
            if (sw360User != null && sw360User.getEmail() != null) {
                headers.set("X-SW360-User", sw360User.getEmail());
            }
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<org.eclipse.sw360.services.common.RequestStatus> response =
                    vendorRestTemplate.exchange(url, HttpMethod.DELETE, entity,
                            org.eclipse.sw360.services.common.RequestStatus.class, vendorId);

            org.eclipse.sw360.services.common.RequestStatus pojoStatus = response.getBody();
            return pojoStatus != null ? RequestStatus.findByValue(pojoStatus.getValue()) : RequestStatus.FAILURE;
        } catch (TException e) {
            throw new TException(e);
        }
    }

    public void deleteAllVendors(User sw360User) {
        List<Vendor> vendors = getAllVendorList();
        for (Vendor vendor : vendors) {
            String url = vendorServiceUrl + "/api/vendors/{id}";
            HttpHeaders headers = new HttpHeaders();
            if (sw360User != null && sw360User.getEmail() != null) {
                headers.set("X-SW360-User", sw360User.getEmail());
            }
            HttpEntity<?> entity = new HttpEntity<>(headers);
            vendorRestTemplate.exchange(url, HttpMethod.DELETE, entity,
                    org.eclipse.sw360.services.common.RequestStatus.class, vendor.getId());
        }
    }

    public ComponentService.Iface getThriftComponentClient() throws TTransportException {
        return new ThriftClients().makeComponentClient();
    }

    public ByteBuffer exportExcel() throws TException {
        String url = vendorServiceUrl + "/api/vendors/report";
        byte[] data = vendorRestTemplate.getForObject(url, byte[].class);
        return data != null ? ByteBuffer.wrap(data) : ByteBuffer.allocate(0);
    }

    private List<Vendor> getAllVendorList() {
        String url = vendorServiceUrl + "/api/vendors?page=0&size=10000&sort=0&ascending=true";
        ResponseEntity<PaginatedVendorResponse> response =
                vendorRestTemplate.exchange(url, HttpMethod.GET, null,
                        new ParameterizedTypeReference<>() {});

        PaginatedVendorResponse body = response.getBody();
        if (body == null || body.data == null) return List.of();

        return body.data.stream().map(this::toThriftVendor).collect(Collectors.toList());
    }

    public Set<Release> getAllReleaseList(String vendorId) throws TException {
        ComponentService.Iface componentsClient = getThriftComponentClient();
        return componentsClient.getReleasesByVendorId(vendorId);
    }

    public RequestStatus mergeVendors(String vendorTargetId, String vendorSourceId,
                                       Vendor vendorSelection, User user)
            throws TException, ResourceClassNotFoundException {
        org.eclipse.sw360.services.vendor.Vendor pojoSelection = fromThriftVendor(vendorSelection);
        String url = vendorServiceUrl + "/api/vendors/merge?targetId={targetId}&sourceId={sourceId}";
        HttpHeaders headers = new HttpHeaders();
        if (user != null && user.getEmail() != null) {
            headers.set("X-SW360-User", user.getEmail());
        }
        HttpEntity<org.eclipse.sw360.services.vendor.Vendor> entity = new HttpEntity<>(pojoSelection, headers);
        ResponseEntity<org.eclipse.sw360.services.common.RequestStatus> response =
                vendorRestTemplate.exchange(url, HttpMethod.POST, entity,
                        org.eclipse.sw360.services.common.RequestStatus.class,
                        vendorTargetId, vendorSourceId);

        org.eclipse.sw360.services.common.RequestStatus pojoStatus = response.getBody();
        RequestStatus requestStatus = pojoStatus != null ? RequestStatus.findByValue(pojoStatus.getValue()) : RequestStatus.FAILURE;

        if (requestStatus == RequestStatus.IN_USE) {
            throw new BadRequestClientException("Vendor used as source or target has an open MR");
        } else if (requestStatus == RequestStatus.FAILURE) {
            throw new ResourceClassNotFoundException("Internal server error while merging the vendors");
        } else if (requestStatus == RequestStatus.ACCESS_DENIED) {
            throw new AccessDeniedException("Access denied");
        }

        return requestStatus;
    }

    private static PaginationData pageableToPaginationData(
            @NotNull Pageable pageable, VendorSortColumn defaultSort, Boolean defaultAscending
    ) {
        VendorSortColumn column = VendorSortColumn.BY_FULLNAME;
        boolean ascending = true;

        if (pageable.getSort().isSorted()) {
            Sort.Order order = pageable.getSort().iterator().next();
            String property = order.getProperty();
            column = switch (property) {
                case "fullname" -> VendorSortColumn.BY_FULLNAME;
                case "shortname" -> VendorSortColumn.BY_SHORTNAME;
                default -> column;
            };
            ascending = order.isAscending();
        } else {
            if (defaultSort != null) {
                column = defaultSort;
                if (defaultAscending != null) {
                    ascending = defaultAscending;
                }
            }
        }

        return new PaginationData().setDisplayStart((int) pageable.getOffset())
                .setRowsPerPage(pageable.getPageSize()).setSortColumnNumber(column.getValue()).setAscending(ascending);
    }

    private Map<String, Object> buildPaginationParams(PaginationData pageData) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", pageData.getDisplayStart() / Math.max(pageData.getRowsPerPage(), 1));
        params.put("size", pageData.getRowsPerPage());
        params.put("sort", pageData.getSortColumnNumber());
        params.put("ascending", pageData.isAscending());
        return params;
    }

    public static class PaginatedVendorResponse {
        public org.eclipse.sw360.services.common.PaginationData pagination;
        public List<org.eclipse.sw360.services.vendor.Vendor> data;
    }

    private Map<PaginationData, List<Vendor>> convertPaginatedResponse(PaginatedVendorResponse pojoResult) {
        if (pojoResult == null) return Map.of();

        Map<PaginationData, List<Vendor>> result = new HashMap<>();
        org.eclipse.sw360.services.common.PaginationData pojoPd = pojoResult.pagination;
        PaginationData thriftPd = pojoPd != null ? new PaginationData()
                .setRowsPerPage(pojoPd.getRowsPerPage())
                .setDisplayStart(pojoPd.getDisplayStart())
                .setAscending(pojoPd.isAscending())
                .setSortColumnNumber(pojoPd.getSortColumnNumber())
                .setTotalRowCount(pojoPd.getTotalRowCount())
                : new PaginationData();
        List<Vendor> thriftVendors = pojoResult.data != null
                ? pojoResult.data.stream().map(this::toThriftVendor).collect(Collectors.toList())
                : List.of();
        result.put(thriftPd, thriftVendors);
        return result;
    }

    private Vendor toThriftVendor(org.eclipse.sw360.services.vendor.Vendor pojo) {
        if (pojo == null) return null;
        Vendor thrift = new Vendor();
        if (pojo.getId() != null) thrift.setId(pojo.getId());
        if (pojo.getRevision() != null) thrift.setRevision(pojo.getRevision());
        thrift.setShortname(pojo.getShortname());
        thrift.setFullname(pojo.getFullname());
        thrift.setUrl(pojo.getUrl());
        if (pojo.getPermissions() != null) {
            Map<RequestedAction, Boolean> perms = new HashMap<>();
            pojo.getPermissions().forEach((k, v) -> {
                try {
                    perms.put(RequestedAction.valueOf(k), v);
                } catch (IllegalArgumentException ignored) {}
            });
            thrift.setPermissions(perms);
        }
        return thrift;
    }

    private org.eclipse.sw360.services.vendor.Vendor fromThriftVendor(Vendor thrift) {
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
}
