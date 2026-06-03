/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.networkPlugin.ReportUtilities.NoReportsFoundException;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
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
    
    private static final AuthenticationState authState = new AuthenticationState();
    
    private static final String ERROR_REACHING_SERVER = "Error reaching the server.";
    private static final String ERROR_AUTHENTICATION = "Authentication failed.";
    
    
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
        
        final PluginParameter reportIdParameter = MultiChoiceParameterType.build(REPORT_ID_PARAMETER_ID);
        reportIdParameter.setName(REPORT_ID_PARAMETER_LABEL);
        reportIdParameter.setDescription(REPORT_ID_PARAMETER_DESCRIPTION);
        reportIdParameter.setVisible(false);
        parameters.addParameter(reportIdParameter);
        
        return parameters;
    }
    
    @Override
    protected RecordStore query(RecordStore query, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        final String userId = parameters.getStringValue(USER_ID_PARAMETER_ID).trim();
        
        if (userId.isBlank()) {
            throw new PluginException(PluginNotificationLevel.ERROR, "User ID cannot be blank.");
        }
        
        if (!authState.isAuthenticated(userId)) {
            if (!launchAuthDialog(parameters)) {
                return null;
            } 
        }

        final RecordStore result = new GraphRecordStore();
        launchReportsDialog(userId, result, interaction, parameters);
        
        return result;
    }
    
    private boolean launchAuthDialog(PluginParameters parameters) throws InterruptedException, PluginException {
        final PluginParameters dlgParams = new PluginParameters();
        final PluginParameter<StringParameterValue> apiKeyParameter = 
                (PluginParameter<StringParameterValue>) parameters.getParameters().get(API_KEY_PARAMETER_ID);
        
        apiKeyParameter.setVisible(true);
        dlgParams.addParameter(apiKeyParameter);
        
        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog("Input API Key", dlgParams);
        dialog.showAndWait();
        final boolean isOk = PluginParametersDialog.OK.equals(dialog.getResult());
        
        boolean continueImporting = false;
        
        if (isOk) {
            final String userId = parameters.getStringValue(USER_ID_PARAMETER_ID).trim();
            final String apiKey = dlgParams.getStringValue(API_KEY_PARAMETER_ID).trim();
            processAuth(userId, apiKey);
            continueImporting = true;
        }
        
        return continueImporting;
    }
    
    private void processAuth(final String userId, final String apiKey) throws NoReportsFoundException, InterruptedException, PluginException {
        try {
            Map<String, String> reportOptions = ReportUtilities.getReportOptions(userId, apiKey);
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
    
    private void launchReportsDialog(String userId, RecordStore result, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        final PluginParameters dlgParams = new PluginParameters();
        final PluginParameter<MultiChoiceParameterType.MultiChoiceParameterValue> reportIdParam = 
                    (PluginParameter<MultiChoiceParameterType.MultiChoiceParameterValue>) parameters.getParameters().get(REPORT_ID_PARAMETER_ID);

        MultiChoiceParameterType.setOptions(reportIdParam, authState.getOptions());
        
        reportIdParam.setVisible(true);
        dlgParams.addParameter(reportIdParam);
        
        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog("Select report(s)", dlgParams);
        dialog.showAndWait();
        final boolean isOk = PluginParametersDialog.OK.equals(dialog.getResult());
        
        if (isOk) {
            List<String> selectedReportIds = MultiChoiceParameterType.getChoices(reportIdParam)
                    .stream()
                    .map(choice -> choice.trim())
                    .map(authState::getReportId)
                    .filter(Objects::nonNull)
                    .toList();
            
            processReports(userId, selectedReportIds, result, interaction);
        }
    }
    
    private void processReports(String userId, List<String> reportIds, RecordStore result, PluginInteraction interaction) throws InterruptedException, PluginException {        
        final List<Report> reports;
        
        try {
            reports = ReportUtilities.getReports(userId, reportIds);
        } catch (IOException e) {
            throw new PluginException(PluginNotificationLevel.ERROR, ERROR_REACHING_SERVER);
        }

        int currentStep = 0;
        final int numReports = reports.size();

        for (Report report : reports) {
            result.add();
            ReportUtilities.addReportToRecord(report, result);
            final String progressString = "Processing report " + (currentStep + 1) + "/" + numReports;
            interaction.setProgress(currentStep++, numReports, progressString, true);
        }
    }

    @Override
    public String getType() {
        return DataAccessPluginCoreType.EXPERIMENTAL;
    }

    @Override
    public int getPosition() {
        return 0;
    }
    
}
