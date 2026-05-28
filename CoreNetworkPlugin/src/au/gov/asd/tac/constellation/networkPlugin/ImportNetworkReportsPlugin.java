/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.networkPlugin.ReportUtilities.ReportOption;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.templates.RecordStoreQueryPlugin;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.naming.AuthenticationException;
import org.json.simple.parser.ParseException;
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
    
    // Store the most recently authenticated user ID and the corresponding report IDs that were fetched
    private static String prevUserId = "";
    private static Map<String, String> reportOptions = new HashMap<>();
    
    
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
        userIdParameter.setStringValue("");
        apiKeyParameter.setVisible(false);
        parameters.addParameter(apiKeyParameter);
        
        final PluginParameter reportIdParameter = SingleChoiceParameterType.build(REPORT_ID_PARAMETER_ID);
        reportIdParameter.setName(REPORT_ID_PARAMETER_LABEL);
        reportIdParameter.setDescription(REPORT_ID_PARAMETER_DESCRIPTION);
        reportIdParameter.setVisible(false);
        parameters.addParameter(reportIdParameter);
        
        return parameters;
    }
    
    @Override
    protected RecordStore query(RecordStore query, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        final String userId = parameters.getStringValue(USER_ID_PARAMETER_ID).trim();
        final RecordStore result = new GraphRecordStore();
        
        if (userId.isBlank()) {
            throw new PluginException(PluginNotificationLevel.ERROR, "User ID cannot be blank.");
        } else if (!userId.equals(prevUserId)) {
            launchAuthDialog(result, interaction, parameters);
        } else {
            launchReportsDialog(result, interaction, parameters);
        }
        
        return result;
    }
    
    private void launchAuthDialog(RecordStore result, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        final PluginParameters dlgParams = new PluginParameters();
        final PluginParameter<StringParameterValue> apiKeyParameter = 
                (PluginParameter<StringParameterValue>) parameters.getParameters().get(API_KEY_PARAMETER_ID);
        apiKeyParameter.setVisible(true);
        dlgParams.addParameter(apiKeyParameter);
        
        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog("Input API Key", dlgParams);
        dialog.showAndWait();
        final boolean isOk = PluginParametersDialog.OK.equals(dialog.getResult());
        
        if (isOk) {
            final String userId = parameters.getStringValue(USER_ID_PARAMETER_ID).trim();
            final String apiKey = dlgParams.getStringValue(API_KEY_PARAMETER_ID).trim();
        
            try {
                reportOptions = ReportUtilities.getReportOptions(userId, apiKey);
                prevUserId = userId;
                launchReportsDialog(result, interaction, parameters);
                
            } catch (AuthenticationException e) {
                // If authentication error, reset report ID list
                reportOptions = new HashMap<>();
                prevUserId = "";
                throw new PluginException(PluginNotificationLevel.ERROR, e.getExplanation());
                
            } catch (ParseException | IOException e) {
                throw new PluginException(PluginNotificationLevel.WARNING, e.getMessage());
            }
        }
    }
    
    private RecordStore launchReportsDialog(RecordStore result, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        final PluginParameters dlgParams = new PluginParameters();
        final PluginParameter<SingleChoiceParameterType.SingleChoiceParameterValue> reportIdParam = 
                    (PluginParameter<SingleChoiceParameterType.SingleChoiceParameterValue>) parameters.getParameters().get(REPORT_ID_PARAMETER_ID);
        
        List<String> optionStrings = reportOptions.keySet()
                .stream()
                .collect(Collectors.toList());
        SingleChoiceParameterType.setOptions(reportIdParam, optionStrings);
        
        reportIdParam.setVisible(true);
        dlgParams.addParameter(reportIdParam);
        
        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog("Select report(s)", dlgParams);
        dialog.showAndWait();
        final boolean isOk = PluginParametersDialog.OK.equals(dialog.getResult());
        
        if (isOk) {
            final String reportOption = dlgParams.getStringValue(REPORT_ID_PARAMETER_ID);
            final String trimmedOption = reportOption == null ? "" : reportOption.trim();
            final String reportId = reportOptions.getOrDefault(trimmedOption, "");
            
            final String userId = parameters.getStringValue(USER_ID_PARAMETER_ID).trim();
            
            List<Report> reports = new ArrayList<>();
        
            try {
                reports = ReportUtilities.getReports(userId, reportId);
            } catch (ParseException | IOException e) {
                throw new PluginException(PluginNotificationLevel.WARNING, e.getMessage());
            }
            
            int currentStep = 0;
            final int numReports = reports.size();
            
            for (Report report : reports) {
                result.add();
                ReportUtilities.addReportToRecord(report, result);
                interaction.setProgress(
                        currentStep++, numReports, 
                        "Processing report " + (currentStep + 1) + "/" + numReports, 
                        true);
            }
        }
        
        return result;
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
