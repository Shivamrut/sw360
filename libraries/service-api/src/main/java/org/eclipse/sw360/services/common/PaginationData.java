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
public class PaginationData {

    private int rowsPerPage;
    private int displayStart;
    private boolean ascending;
    private int sortColumnNumber;
    private long totalRowCount;

    public PaginationData() {}

    public int getRowsPerPage() { return rowsPerPage; }
    public PaginationData setRowsPerPage(int rowsPerPage) { this.rowsPerPage = rowsPerPage; return this; }

    public int getDisplayStart() { return displayStart; }
    public PaginationData setDisplayStart(int displayStart) { this.displayStart = displayStart; return this; }

    public boolean isAscending() { return ascending; }
    public PaginationData setAscending(boolean ascending) { this.ascending = ascending; return this; }

    public int getSortColumnNumber() { return sortColumnNumber; }
    public PaginationData setSortColumnNumber(int sortColumnNumber) { this.sortColumnNumber = sortColumnNumber; return this; }

    public long getTotalRowCount() { return totalRowCount; }
    public PaginationData setTotalRowCount(long totalRowCount) { this.totalRowCount = totalRowCount; return this; }
}
