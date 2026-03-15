/*
 * Copyright Siemens AG, 2013-2018. Part of the SW360 Portal Project.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.vendors;

import com.ibm.cloud.cloudant.v1.Cloudant;
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.cloudantclient.DatabaseConnectorCloudant;
import org.eclipse.sw360.datahandler.common.DatabaseSettings;
import org.eclipse.sw360.datahandler.db.UserRepository;
import org.eclipse.sw360.datahandler.db.VendorSearchHandler;
import org.eclipse.sw360.services.common.AddDocumentRequestSummary;
import org.eclipse.sw360.services.common.PaginationData;
import org.eclipse.sw360.services.common.RequestStatus;
import org.eclipse.sw360.services.vendor.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VendorHandler {

    private final VendorDatabaseHandler vendorDatabaseHandler;
    private final VendorSearchHandler vendorSearchHandler;
    private final UserRepository userRepository;

    @Autowired
    public VendorHandler(Cloudant client) throws IOException {
        DatabaseConnectorCloudant databaseConnector = new DatabaseConnectorCloudant(client, DatabaseSettings.COUCH_DB_DATABASE);
        vendorDatabaseHandler = new VendorDatabaseHandler(databaseConnector);
        vendorSearchHandler = new VendorSearchHandler(client, DatabaseSettings.COUCH_DB_DATABASE);
        DatabaseConnectorCloudant usersConnector = new DatabaseConnectorCloudant(client, DatabaseSettings.COUCH_DB_USERS);
        userRepository = new UserRepository(usersConnector);
    }

    public VendorHandler(Cloudant client, String dbName) throws IOException {
        DatabaseConnectorCloudant databaseConnector = new DatabaseConnectorCloudant(client, dbName);
        vendorDatabaseHandler = new VendorDatabaseHandler(databaseConnector);
        vendorSearchHandler = new VendorSearchHandler(client, dbName != null ? dbName : DatabaseSettings.COUCH_DB_DATABASE);
        DatabaseConnectorCloudant usersConnector = new DatabaseConnectorCloudant(client, DatabaseSettings.COUCH_DB_USERS);
        userRepository = new UserRepository(usersConnector);
    }

    public Vendor getByID(String id) {
        try {
            org.eclipse.sw360.datahandler.thrift.vendors.Vendor thriftVendor = vendorDatabaseHandler.getByID(id);
            return VendorConverter.fromThrift(thriftVendor);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Vendor> getAllVendors() {
        try {
            return VendorConverter.fromThriftList(vendorDatabaseHandler.getAllVendors());
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<PaginationData, List<Vendor>> getAllVendorsPaginated(PaginationData pageData) {
        try {
            org.eclipse.sw360.datahandler.thrift.PaginationData thriftPageData =
                    VendorConverter.toThriftPaginationData(pageData);
            Map<org.eclipse.sw360.datahandler.thrift.PaginationData,
                    List<org.eclipse.sw360.datahandler.thrift.vendors.Vendor>> thriftResult =
                    vendorDatabaseHandler.getAllVendors(thriftPageData);

            return thriftResult.entrySet().stream().collect(Collectors.toMap(
                    e -> VendorConverter.fromThriftPaginationData(e.getKey()),
                    e -> VendorConverter.fromThriftList(e.getValue())
            ));
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> getAllVendorNames() {
        HashSet<String> vendorNames = new HashSet<>();
        for (Vendor vendor : getAllVendors()) {
            vendorNames.add(vendor.getFullname());
            vendorNames.add(vendor.getShortname());
        }
        return vendorNames;
    }

    public Map<PaginationData, List<Vendor>> searchVendors(String searchText, PaginationData pageData) {
        org.eclipse.sw360.datahandler.thrift.PaginationData thriftPageData =
                VendorConverter.toThriftPaginationData(pageData);
        Map<org.eclipse.sw360.datahandler.thrift.PaginationData,
                List<org.eclipse.sw360.datahandler.thrift.vendors.Vendor>> thriftResult =
                vendorSearchHandler.search(searchText, thriftPageData);

        return thriftResult.entrySet().stream().collect(Collectors.toMap(
                e -> VendorConverter.fromThriftPaginationData(e.getKey()),
                e -> VendorConverter.fromThriftList(e.getValue())
        ));
    }

    public List<String> searchVendorIds(String searchText) {
        return vendorSearchHandler.searchIds(searchText);
    }

    public AddDocumentRequestSummary addVendor(Vendor vendor) {
        org.eclipse.sw360.datahandler.thrift.vendors.Vendor thriftVendor = VendorConverter.toThrift(vendor);
        return VendorConverter.fromThriftAddDocSummary(vendorDatabaseHandler.addVendor(thriftVendor));
    }

    private org.eclipse.sw360.datahandler.thrift.users.User resolveUser(String userEmail) {
        if (userEmail == null) {
            throw new RuntimeException("User email is required for this operation");
        }
        org.eclipse.sw360.datahandler.thrift.users.User user = userRepository.getByEmail(userEmail);
        if (user == null) {
            throw new RuntimeException("User not found: " + userEmail);
        }
        return user;
    }

    public RequestStatus deleteVendor(String id, String userEmail) {
        try {
            return VendorConverter.fromThriftRequestStatus(
                    vendorDatabaseHandler.deleteVendor(id, resolveUser(userEmail)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RequestStatus updateVendor(Vendor vendor, String userEmail) {
        try {
            org.eclipse.sw360.datahandler.thrift.vendors.Vendor thriftVendor = VendorConverter.toThrift(vendor);
            return VendorConverter.fromThriftRequestStatus(
                    vendorDatabaseHandler.updateVendor(thriftVendor, resolveUser(userEmail)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RequestStatus mergeVendors(String mergeTargetId, String mergeSourceId,
                                       Vendor mergeSelection, String userEmail) {
        try {
            org.eclipse.sw360.datahandler.thrift.vendors.Vendor thriftSelection = VendorConverter.toThrift(mergeSelection);
            return VendorConverter.fromThriftRequestStatus(
                    vendorDatabaseHandler.mergeVendors(mergeTargetId, mergeSourceId, thriftSelection,
                            resolveUser(userEmail)));
        } catch (Exception e) {
            throw new RuntimeException("Merge failed: " + e.getMessage(), e);
        }
    }

    public byte[] getVendorReportDataStream() {
        try {
            List<org.eclipse.sw360.datahandler.thrift.vendors.Vendor> thriftVendors = vendorDatabaseHandler.getAllVendors();
            java.nio.ByteBuffer buffer = vendorDatabaseHandler.getVendorReportDataStream(thriftVendors);
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            return data;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate vendor report: " + e.getMessage(), e);
        }
    }
}
