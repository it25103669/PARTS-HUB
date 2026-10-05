package com.partshub.reporting.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single data row in the report results table.
 * Columns are stored as an ordered list of string values.
 * The column headers are defined separately in ReportSummaryDTO
 * or passed alongside the row list from the service.
 */
public class ReportRowDTO {

    private List<String> columns;

    public ReportRowDTO() {
        this.columns = new ArrayList<>();
    }

    public ReportRowDTO(List<String> columns) {
        this.columns = columns;
    }

    public List<String> getColumns() { return columns; }
    public List<String> getValues() { return columns; }
    public void setColumns(List<String> columns) { this.columns = columns; }


    /** Convenience method to add a single cell value. */
    public ReportRowDTO addColumn(String value) {
        this.columns.add(value);
        return this;
    }
}
