/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.templates.RecordStoreQueryPlugin;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    
    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();
        
        final PluginParameter<StringParameterValue> userIdParameter = StringParameterType.build(USER_ID_PARAMETER_ID);
        userIdParameter.setName(USER_ID_PARAMETER_LABEL);
        userIdParameter.setDescription(USER_ID_PARAMETER_DESCRIPTION);
        StringParameterType.setLines(userIdParameter, 1);
        parameters.addParameter(userIdParameter);
        
        final PluginParameter<StringParameterValue> reportIdParameter = StringParameterType.build(REPORT_ID_PARAMETER_ID);
        reportIdParameter.setName(REPORT_ID_PARAMETER_LABEL);
        reportIdParameter.setDescription(REPORT_ID_PARAMETER_DESCRIPTION);
        StringParameterType.setLines(reportIdParameter, 1);
        parameters.addParameter(reportIdParameter);
        
        return parameters;
    }
    
    @Override
    protected RecordStore query(RecordStore query, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        final PluginParameters dlgParams = new PluginParameters();
        final PluginParameter<StringParameterValue> apiKeyParameter = StringParameterType.build(API_KEY_PARAMETER_ID);
        apiKeyParameter.setName(API_KEY_PARAMETER_LABEL);
        apiKeyParameter.setDescription(API_KEY_PARAMETER_DESCRIPTION);
        dlgParams.addParameter(apiKeyParameter);
        
        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog("Input API Key", dlgParams);
        dialog.showAndWait();
        final boolean isOk = PluginParametersDialog.OK.equals(dialog.getResult());
        
        final RecordStore result = new GraphRecordStore();
        
        if (isOk) {
            final String apiKey = dlgParams.getStringValue(API_KEY_PARAMETER_ID);
            final String userId = parameters.getStringValue(USER_ID_PARAMETER_ID);
            final String reportId = parameters.getStringValue(REPORT_ID_PARAMETER_ID);
            
            List<Report> reports = new ArrayList<>();
        
            try {
                reports = ReportUtilities.getReports(userId, apiKey, reportId);
            } catch (AuthenticationException e) {
                throw new PluginException(PluginNotificationLevel.ERROR, e.getExplanation());
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
