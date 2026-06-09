/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin.importReportsPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintains the authentication state of the plugin, which comprises the previously 
 * authenticated user ID, and the list of report options for that particular user.
 */
public class AuthenticationState {
    private String prevUserId;
    private List<ReportOption> reportOptions;

    public AuthenticationState() {
        this.prevUserId = "";
        this.reportOptions = new ArrayList<>();
    }

    public void setState(String authUserId, List<ReportOption> options) {
        this.prevUserId = authUserId;
        this.reportOptions = options;
    }

    public void clearState() {
        this.prevUserId = "";
        this.reportOptions.clear();
    }

    public boolean checkIfAuthenticated(String newUserId) {
        return this.prevUserId.equals(newUserId);
    }
    
    public List<String> getAllOptionLabels() {
        return this.reportOptions.stream()
                .map(ReportOption::getDisplayName)
                .sorted()
                .toList();
    }

    public List<String> getSelectedReportIds(final List<String> selectedLabels) {
        return this.reportOptions.stream()
                .filter((option) -> option.isSelected(selectedLabels))
                .map(ReportOption::getReportId)
                .sorted()
                .toList();
    }
}
