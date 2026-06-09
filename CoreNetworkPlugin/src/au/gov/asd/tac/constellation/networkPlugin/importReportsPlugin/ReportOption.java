/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin.importReportsPlugin;

import java.util.List;

/**
* A particular dropdown option containing both a report's name and its ID.
*/
public class ReportOption {
    private static final String LABEL_TEMPLATE = "%s (ID: %s)";
    
    private final String reportId, reportName;

    public ReportOption(String reportId, String reportName) { 
        this.reportId = reportId;
        this.reportName = reportName;
    }
    
    public String getReportId() {
        return this.reportId;
    }
    
    public boolean isSelected(List<String> selectedLabels) {
        return selectedLabels.contains(this.getDisplayName());
    }
   
    public String getDisplayName() {
         return String.format(LABEL_TEMPLATE, this.reportName, reportId);
    }
}
