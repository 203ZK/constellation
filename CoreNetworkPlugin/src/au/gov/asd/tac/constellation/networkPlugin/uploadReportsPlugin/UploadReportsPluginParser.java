/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin.uploadReportsPlugin;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.networkPlugin.Report;
import au.gov.asd.tac.constellation.networkPlugin.ReportConcept;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Intern-1003972
 */
public class UploadReportsPluginParser {
    
    private static final String INTERNAL_USER_ID = "internal_user_id";
    private static final String REPORT_ID = "report_id";
    private static final String REPORT_NAME = "report_name";
    
    private static final String PREFIX_SOURCE = "source_";
    private static final String PREFIX_DESTINATION = "destination_";
    
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
    
    private static String getFieldValueFromRow(final Map<String, Integer> headerMap, final String[] row, final String field) {
        return row[headerMap.get(field)];
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
    
}
