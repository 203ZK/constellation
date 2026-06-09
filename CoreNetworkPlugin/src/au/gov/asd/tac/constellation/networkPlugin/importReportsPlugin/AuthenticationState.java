/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin.importReportsPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintains the authentication state of the plugin, which comprises the previously 
 * authenticated user ID, and the list of report options for that particular user.
 */
public class AuthenticationState {
    private String prevUserId;
    private Map<String, String> reportOptions;

    public AuthenticationState() {
        this.prevUserId = "";
        this.reportOptions = new HashMap<>();
    }

    public void setState(String authUserId, Map<String, String> options) {
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

    public List<String> getOptions() {
        final List<String> options = new ArrayList<>(reportOptions.keySet());
        options.sort(Comparator.naturalOrder());
        return options;
    }
    
    public String getReportId(String option) {
        return this.reportOptions.get(option);
    }
}
