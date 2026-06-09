/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Utilities for parsing report-related objects.
 */
public class ReportPluginParser {
    
    // These fields have to be aligned with the server
    private static final String INTERNAL_USER_ID = "internal_user_id";
    private static final String REPORT_ID = "report_id";
    private static final String REPORT_NAME = "report_name";
    private static final String ATTRIBUTES_LABEL = "Attributes";
    
    private static final String PREFIX_SOURCE = "source_";
    private static final String PREFIX_DESTINATION = "destination_";
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * A particular dropdown option containing both a report's name and its ID.
     */
    public static class ReportOption {
        private final String id, name;
        
        public ReportOption(String id, String name) { 
            this.id = id;
            this.name = name;
        }
        
        public String getDisplayName() {
            return this.name + " (ID: " + this.id + ")";
        }
    }
    
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
    
    public static Report parseReport(final Map<String, Integer> headerMap, final String[] row) {
        final String internalUserId = getFieldValueFromRow(headerMap, row, INTERNAL_USER_ID);
        final String reportId = getFieldValueFromRow(headerMap, row, REPORT_ID);
        final String reportName = getFieldValueFromRow(headerMap, row, REPORT_NAME);
        
        final String sourceIdentifier = getFieldValueFromRow(headerMap, row, GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER);
        final String sourceEntityType = getFieldValueFromRow(headerMap, row, GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE);
        final String sourceEntityId = getFieldValueFromRow(headerMap, row, GraphRecordStoreUtilities.SOURCE + ReportConcept.VertexAttribute.ENTITY_ID);
        
        final String destinationIdentifier = getFieldValueFromRow(headerMap, row, GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER);
        final String destinationEntityType = getFieldValueFromRow(headerMap, row, GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.TYPE);
        final String destinationEntityId = getFieldValueFromRow(headerMap, row, GraphRecordStoreUtilities.DESTINATION + ReportConcept.VertexAttribute.ENTITY_ID);
        
        final Map<String, Object> sourceOtherAttributes = retrieveSourceOtherAttributes(headerMap, row);
        final Map<String, Object> destinationOtherAttributes = retrieveDestinationOtherAttributes(headerMap, row);
        final Map<String, Object> transactionAttributes = retrieveTransactionAttributes(headerMap, row);
        
        return new Report(
                internalUserId, reportId, reportName, 
                sourceIdentifier, sourceEntityType, sourceEntityId, sourceOtherAttributes, 
                destinationIdentifier, destinationEntityType, destinationEntityId, destinationOtherAttributes, 
                transactionAttributes
        );
    }
    
    private static Map<String, Object> retrieveSourceOtherAttributes(final Map<String, Integer> headerMap, final String[] row) {
        final Map<String, Object> sourceOtherAttributes = new HashMap<>();
        for (String headerName : headerMap.keySet()) {
            if (headerName.startsWith(PREFIX_SOURCE)) {
                final String headerNameWithoutPrefix = headerName.substring(PREFIX_SOURCE.length());
                final Object value = getFieldValueFromRow(headerMap, row, headerName);
                sourceOtherAttributes.put(headerNameWithoutPrefix, value);
            }
        }
        return sourceOtherAttributes;
    }
    
    private static Map<String, Object> retrieveDestinationOtherAttributes(final Map<String, Integer> headerMap, final String[] row) {
        final Map<String, Object> destinationOtherAttributes = new HashMap<>();
        for (String headerName : headerMap.keySet()) {
            if (headerName.startsWith(PREFIX_DESTINATION)) {
                final String headerNameWithoutPrefix = headerName.substring(PREFIX_DESTINATION.length());
                final Object value = getFieldValueFromRow(headerMap, row, headerName);
                destinationOtherAttributes.put(headerNameWithoutPrefix, value);
            }
        }
        return destinationOtherAttributes;
    }
    
    private static Map<String, Object> retrieveTransactionAttributes(final Map<String, Integer> headerMap, final String[] row) {
        final Map<String, Object> transactionAttributes = new HashMap<>();
        for (String headerName : headerMap.keySet()) {
            if (!headerName.startsWith(PREFIX_DESTINATION) && !headerName.startsWith(PREFIX_DESTINATION)) {
                final Object value = getFieldValueFromRow(headerMap, row, headerName);
                transactionAttributes.put(headerName, value);
            }
        }
        return transactionAttributes;
    }
    
    private static String getFieldValueFromRow(final Map<String, Integer> headerMap, final String[] row, final String field) {
        return row[headerMap.get(field)];
    }
    
    /**
     * Retrieves the required headers that are missing from the file.
     */
    public static List<String> verifyHeaders(final String[] headers) {
        List<String> requiredHeaders = new ArrayList<>(Arrays.asList(
                INTERNAL_USER_ID, REPORT_ID, REPORT_NAME, 
                GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, 
                GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, 
                GraphRecordStoreUtilities.SOURCE + ReportConcept.VertexAttribute.ENTITY_ID, 
                GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, 
                GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.TYPE, 
                GraphRecordStoreUtilities.DESTINATION + ReportConcept.VertexAttribute.ENTITY_ID
        ));
        
        requiredHeaders.removeAll(new HashSet<>(Arrays.asList(headers)));
        return requiredHeaders;
    }
    
    /**
     * Maps the required headers from their column index to their title.
     */
    public static Map<String, Integer> mapHeaders(final String[] headers) {
        Map<String, Integer> headerMap = new HashMap<>();
        for (int colIdx = 0; colIdx < headers.length; colIdx++) {
            headerMap.put(headers[colIdx], colIdx);
        }
        return headerMap;
    }
    
}
