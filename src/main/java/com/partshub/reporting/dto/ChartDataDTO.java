package com.partshub.reporting.dto;

import java.util.List;

/**
 * Generic chart data container.
 * The controller serializes this to JSON and injects it into the template
 * via a data attribute. JavaScript reads it and passes it to Chart.js.
 */
public class ChartDataDTO {

    private String chartType;               // "line", "bar", "doughnut"
    private List<String> labels;            // X-axis labels or segment names
    private List<String> datasetLabels;     // Legend label for each dataset
    private List<List<Number>> datasets;    // Each inner list = one dataset's data points
    private List<String> backgroundColors;  // Per-dataset or per-segment colours

    public ChartDataDTO() {}

    public String getChartType() { return chartType; }
    public void setChartType(String chartType) { this.chartType = chartType; }

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }

    public List<String> getDatasetLabels() { return datasetLabels; }
    public void setDatasetLabels(List<String> datasetLabels) { this.datasetLabels = datasetLabels; }

    public List<List<Number>> getDatasets() { return datasets; }
    public void setDatasets(List<List<Number>> datasets) { this.datasets = datasets; }

    public List<String> getBackgroundColors() { return backgroundColors; }
    public void setBackgroundColors(List<String> backgroundColors) {
        this.backgroundColors = backgroundColors;
    }
}
