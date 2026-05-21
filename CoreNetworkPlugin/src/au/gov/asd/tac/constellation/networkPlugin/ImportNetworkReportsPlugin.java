/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.networkPlugin.ReportConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
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
    private static final String REPORT_ID_PARAMETER_ID = 
            PluginParameter.buildId(ImportNetworkReportsPlugin.class, "reportId");
    
    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();
        
        final PluginParameter<StringParameterValue> userIdParameter = StringParameterType.build(USER_ID_PARAMETER_ID);
        userIdParameter.setName("User ID");
        userIdParameter.setDescription("Enter a user ID to show reports");
        StringParameterType.setLines(userIdParameter, 1);
        
        final PluginParameter<StringParameterValue> reportIdParameter = StringParameterType.build(REPORT_ID_PARAMETER_ID);
        reportIdParameter.setName("Report ID");
        reportIdParameter.setDescription("Enter a report ID");
        StringParameterType.setLines(reportIdParameter, 1);
        
        parameters.addParameter(userIdParameter);
        parameters.addParameter(reportIdParameter);
        
        return parameters;
    }
    
    @Override
    protected RecordStore query(RecordStore query, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        List<Report> reports = new ArrayList<>();
        final String userIdParameterString = parameters.getStringValue(USER_ID_PARAMETER_ID);
        final String reportIdParameterString = parameters.getStringValue(REPORT_ID_PARAMETER_ID);
        
        try {
            reports = ReportUtilities.getReports(userIdParameterString, reportIdParameterString);
        } catch (ParseException | IOException e) {
            throw new PluginException(PluginNotificationLevel.WARNING, e.getMessage());
        }
        
        int currentStep = 0;
        final RecordStore result = new GraphRecordStore();
//        ReportConcept.resetSchemaAttributes();
        
        for (Report report : reports) {
            result.add();
            ReportUtilities.addReportToRecord(report, result);
            interaction.setProgress(currentStep++, reports.size(), "Processing: " + report.getReportId(), true);
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
