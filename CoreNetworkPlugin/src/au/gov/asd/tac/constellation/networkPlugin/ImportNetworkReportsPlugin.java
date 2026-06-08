/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.networkPlugin.ReportPluginUtilities.NoReportsFoundException;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.templates.RecordStoreQueryPlugin;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.naming.AuthenticationException;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Import Network Reports Plugin.
 */
@ServiceProviders({
    @ServiceProvider(service=DataAccessPlugin.class),
    @ServiceProvider(service=Plugin.class)
})
@NbBundle.Messages("ImportNetworkReportsPlugin=Import Network Reports")
public class ImportNetworkReportsPlugin extends RecordStoreQueryPlugin implements DataAccessPlugin {

    private static final String USER_ID_PARAMETER_ID = 
            PluginParameter.buildId(ImportNetworkReportsPlugin.class, "userId");
    private static final String USER_ID_PARAMETER_LABEL = "User ID";
    private static final String USER_ID_PARAMETER_DESCRIPTION = "Enter your user ID to show reports";
    
    private static final String API_KEY_PARAMETER_ID = 
            PluginParameter.buildId(ImportNetworkReportsPlugin.class, "apiKey");
    private static final String API_KEY_PARAMETER_LABEL = "API Key";
    private static final String API_KEY_PARAMETER_DESCRIPTION = "Enter your API key for authentication";
    
    private static final String REPORT_ID_PARAMETER_ID = 
            PluginParameter.buildId(ImportNetworkReportsPlugin.class, "reportId");
    private static final String REPORT_ID_PARAMETER_LABEL = "Report ID";
    private static final String REPORT_ID_PARAMETER_DESCRIPTION = "Select the report ID to be visualised";
    
    private static final String INFO_IMPORT_CANCELLED = "Import cancelled by the user.";
    private static final String ERROR_REACHING_SERVER = "Error reaching the server.";
    private static final String ERROR_AUTHENTICATION = "Authentication failed.";
    
    private static final AuthenticationState authState = new AuthenticationState();
    
    @Override
    public String getType() {
        return DataAccessPluginCoreType.NETWORK_PLUGINS;
    }

    @Override
    public int getPosition() {
        return 0;
    }
    
    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();
        
        final PluginParameter<StringParameterValue> userIdParameter = StringParameterType.build(USER_ID_PARAMETER_ID);
        userIdParameter.setName(USER_ID_PARAMETER_LABEL);
        userIdParameter.setDescription(USER_ID_PARAMETER_DESCRIPTION);
        userIdParameter.setStringValue("");
        StringParameterType.setLines(userIdParameter, 1);
        parameters.addParameter(userIdParameter);
        
        final PluginParameter<StringParameterValue> apiKeyParameter = StringParameterType.build(API_KEY_PARAMETER_ID);
        apiKeyParameter.setName(API_KEY_PARAMETER_LABEL);
        apiKeyParameter.setDescription(API_KEY_PARAMETER_DESCRIPTION);
        apiKeyParameter.setStringValue("");
        apiKeyParameter.setVisible(false);
        parameters.addParameter(apiKeyParameter);
        
        final PluginParameter<MultiChoiceParameterType.MultiChoiceParameterValue> reportIdParameter = MultiChoiceParameterType.build(REPORT_ID_PARAMETER_ID);
        reportIdParameter.setName(REPORT_ID_PARAMETER_LABEL);
        reportIdParameter.setDescription(REPORT_ID_PARAMETER_DESCRIPTION);
        reportIdParameter.setVisible(false);
        parameters.addParameter(reportIdParameter);
        
        return parameters;
    }
    
    @Override
    protected RecordStore query(RecordStore query, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        final String userId = parameters.getStringValue(USER_ID_PARAMETER_ID).trim();
        verifyNonBlankUserId(userId);
        
        boolean isAuthenticated = authState.checkIfAuthenticated(userId);
        if (!isAuthenticated) {
            launchAuthDialog(parameters);
            authenticateAndFetchReportOptions(parameters);
        }
        
        launchReportsDialog(parameters);
        
        List<Report> reports = fetchReports(parameters);
        final RecordStore result = createRecordWithReports(reports, interaction);
        
        return result;
    }
    
    // ------------- General utils -------------
    
    private void verifyNonBlankUserId(final String userId) throws PluginException {
        if (userId.isBlank()) {
            throw new PluginException(PluginNotificationLevel.ERROR, "User ID cannot be blank.");
        }
    }
    
    private boolean launchDialogAndAwaitResponse(final String dialogLabel, final PluginParameters dialogParams) {
        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(dialogLabel, dialogParams);
        dialog.showAndWait();
        return dialog.isAccepted();
    }
    
    // ------------- Handling auth + fetching report options -------------
    
    private void launchAuthDialog(PluginParameters parameters) throws PluginException {
        final PluginParameters dialogParams = createAuthDialogParameters(parameters);
        final boolean willProceed = launchDialogAndAwaitResponse("Input API Key", dialogParams);
        if (!willProceed) {
            throw new PluginException(PluginNotificationLevel.INFO, INFO_IMPORT_CANCELLED);
        }
    }
    
    private PluginParameters createAuthDialogParameters(final PluginParameters parameters) {
        final PluginParameters dialogParams = new PluginParameters();
        final PluginParameter<StringParameterValue> apiKeyParameter = 
                (PluginParameter<StringParameterValue>) parameters.getParameters().get(API_KEY_PARAMETER_ID);
        
        apiKeyParameter.setVisible(true);
        dialogParams.addParameter(apiKeyParameter);
        return dialogParams;
    }
    
    private void authenticateAndFetchReportOptions(final PluginParameters parameters) throws InterruptedException, PluginException {
        final String userId = parameters.getStringValue(USER_ID_PARAMETER_ID).trim();
        final String apiKey = parameters.getStringValue(API_KEY_PARAMETER_ID).trim();
        
        try {
            Map<String, String> reportOptions = ReportPluginUtilities.getReportOptions(userId, apiKey);
            authState.setState(userId, reportOptions);
        } catch (NoReportsFoundException e) {
            throw new PluginException(PluginNotificationLevel.INFO, e.getMessage());
        } catch (IOException e) {
            throw new PluginException(PluginNotificationLevel.ERROR, ERROR_REACHING_SERVER);
        } catch (AuthenticationException e) {
            authState.clearState();
            throw new PluginException(PluginNotificationLevel.ERROR, ERROR_AUTHENTICATION);
        }
    }
    
    // ------------- Handling selection of reports to visualise -------------
    
    private PluginParameters createReportsDialogParameters(final PluginParameters parameters) {
        final PluginParameters dialogParams = new PluginParameters();
        final PluginParameter<MultiChoiceParameterType.MultiChoiceParameterValue> reportIdParam = 
                    (PluginParameter<MultiChoiceParameterType.MultiChoiceParameterValue>) parameters.getParameters().get(REPORT_ID_PARAMETER_ID);

        MultiChoiceParameterType.setOptions(reportIdParam, authState.getOptions());
        
        reportIdParam.setVisible(true);
        dialogParams.addParameter(reportIdParam);
        
        return dialogParams;
    }
    
    private void launchReportsDialog(PluginParameters parameters) throws PluginException {
        final PluginParameters dialogParams = createReportsDialogParameters(parameters);
        final boolean willProceed = launchDialogAndAwaitResponse("Select report(s)", dialogParams);
        if (!willProceed) {
            throw new PluginException(PluginNotificationLevel.INFO, INFO_IMPORT_CANCELLED);
        }
    }
    
    private List<Report> fetchReports(final PluginParameters parameters) throws InterruptedException, PluginException {
        final List<String> selectedReportIds = parseUserSelectedReportIds(parameters);
        
        List<Report> reports = new ArrayList<>();
        try {
            final String userId = parameters.getStringValue(USER_ID_PARAMETER_ID).trim();
            reports = ReportPluginUtilities.getReports(userId, selectedReportIds);
        } catch (IOException e) {
            throw new PluginException(PluginNotificationLevel.ERROR, ERROR_REACHING_SERVER);
        }
        
        return reports;
    }
    
    private List<String> parseUserSelectedReportIds(final PluginParameters parameters) {
        final PluginParameter<MultiChoiceParameterType.MultiChoiceParameterValue> reportIdParam = 
                    (PluginParameter<MultiChoiceParameterType.MultiChoiceParameterValue>) parameters.getParameters().get(REPORT_ID_PARAMETER_ID);
        
        List<String> selectedReportIds = MultiChoiceParameterType.getChoices(reportIdParam).stream()
                .map(choice -> choice.trim())
                .map(authState::getReportId)
                .filter(Objects::nonNull).toList();
       
        return selectedReportIds;
    }
    
    private RecordStore createRecordWithReports(List<Report> reports, PluginInteraction interaction) throws InterruptedException {
        final RecordStore result = new GraphRecordStore();
        
        final int numReports = reports.size();
        int currentStep = 0;
        
        for (Report report : reports) {
            result.add();
            ReportPluginUtilities.addReportToRecord(report, result);
            
            final String progressString = "Processing report " + (currentStep + 1) + "/" + numReports;
            interaction.setProgress(currentStep++, numReports, progressString, true);
        }
        
        return result;
    }
    
}
