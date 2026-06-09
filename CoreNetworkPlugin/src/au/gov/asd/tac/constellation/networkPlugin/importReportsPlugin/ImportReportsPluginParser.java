/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin.importReportsPlugin;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.networkPlugin.Report;
import au.gov.asd.tac.constellation.networkPlugin.ReportConcept;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for parsing report-related objects.
 */
public class ImportReportsPluginParser {
    
    // These fields have to be aligned with the server
    private static final String INTERNAL_USER_ID = "internal_user_id";
    private static final String REPORT_ID = "report_id";
    private static final String REPORT_NAME = "report_name";
    private static final String ATTRIBUTES_LABEL = "Attributes";
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static Map<String, String> parseReportOptions(final String reportOptionsString) throws JsonProcessingException {
        List<Map<String, Object>> options = mapper.readValue(reportOptionsString, new TypeReference<List<Map<String, Object>>>(){});
        
        Map<String, String> parsedOptions = new HashMap<>();
        
        for (Map<String, Object> option : options) {
            String reportId = (String) option.get(REPORT_ID);
            String reportName = (String) option.get(REPORT_NAME);
            parsedOptions.put(
                    new ReportOption(reportId, reportName).getDisplayName(), 
                    reportId
            );
        }
        
        return parsedOptions;
    }
    
    public static List<Report> parseReports(final String reportsString) throws JsonProcessingException {
        List<Map<String, Object>> reports = mapper.readValue(reportsString, new TypeReference<List<Map<String, Object>>>(){});
        
        List<Report> parsedReports = new ArrayList<>();
        
        for (Map<String, Object> report : reports) {
            Report parsedReport = parseReport(report);
            parsedReports.add(parsedReport);
        }
        
        return parsedReports;
    }
    
    /**
     * Each Report must have the following fields.
     * - Metadata fields: internal_user_id, report_id, 
     * - Source/destination identifier, (entity) type, entity ID,
     * - Source/destination additional attributes,
     * - Transaction attributes
     */
    private static Report parseReport(final Map<String, Object> reportJson) {
        String internalUserId = (String) reportJson.get(INTERNAL_USER_ID);
        String reportId = (String) reportJson.get(REPORT_ID);
        String reportName = (String) reportJson.get(REPORT_NAME);
        
        String sourceIdentifier = (String) reportJson.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER);
        String sourceType = (String) reportJson.get(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE);
        String sourceEntityId = (String) reportJson.get(GraphRecordStoreUtilities.SOURCE + ReportConcept.VertexAttribute.ENTITY_ID);
        
        String destinationIdentifier = (String) reportJson.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER);
        String destinationType = (String) reportJson.get(GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.TYPE);
        String destinationEntityId = (String) reportJson.get(GraphRecordStoreUtilities.DESTINATION + ReportConcept.VertexAttribute.ENTITY_ID);
        
        // Server will guarantee JSON objects
        @SuppressWarnings("unchecked")
        Map<String, Object> sourceOtherAttributes = (Map<String, Object>) reportJson.get(GraphRecordStoreUtilities.SOURCE + ATTRIBUTES_LABEL);
        @SuppressWarnings("unchecked")
        Map<String, Object> destinationOtherAttributes = (Map<String, Object>) reportJson.get(GraphRecordStoreUtilities.DESTINATION + ATTRIBUTES_LABEL);
        @SuppressWarnings("unchecked")
        Map<String, Object> transactionAttributes = (Map<String, Object>) reportJson.get(GraphRecordStoreUtilities.TRANSACTION + ATTRIBUTES_LABEL);
        
        return new Report(
                internalUserId, reportId, reportName,  
                sourceIdentifier, sourceType, sourceEntityId, sourceOtherAttributes,
                destinationIdentifier, destinationType, destinationEntityId, destinationOtherAttributes,
                transactionAttributes
        );
    }
    
}
