/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin.importReportsPlugin;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.DoubleAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.Record;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.networkPlugin.Report;
import au.gov.asd.tac.constellation.networkPlugin.ReportConcept;
import static au.gov.asd.tac.constellation.networkPlugin.importReportsPlugin.ApiServerUtilities.callReportIdsApi;
import static au.gov.asd.tac.constellation.networkPlugin.importReportsPlugin.ApiServerUtilities.callReportsApi;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.AnalyticIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import javax.naming.AuthenticationException;
import static au.gov.asd.tac.constellation.networkPlugin.importReportsPlugin.ImportReportsPluginParser.parseReportOptions;
import static au.gov.asd.tac.constellation.networkPlugin.importReportsPlugin.ImportReportsPluginParser.parseReports;

/**
 * Report Plugin Utilities.
 */
public class ImportReportsPluginUtilities {
    
    public static Map<String, String> getReportOptions(
            final String userId, final String apiKey
    ) throws NoReportsFoundException, AuthenticationException, IOException, InterruptedException {
        
        String jsonResponse = callReportIdsApi(userId, apiKey);
        Map<String, String> reportOptions = parseReportOptions(jsonResponse);
        
        if (reportOptions.isEmpty()) {
            throw new NoReportsFoundException(userId);
        }
        
        return reportOptions;
    }
    
    public static List<Report> getReports(final String userId, final List<String> reportIds) throws IOException, InterruptedException {
        String jsonResponse = callReportsApi(userId, reportIds);
        return parseReports(jsonResponse);
    }
    
    /**
     * Adds the attributes of a Report instance to the current row of a RecordStore, specified as a Record.
     * Each Report contains both source and destination vertices to be added to the RecordStore.
     *
     * @param report the Report instance to be added.
     * @param record the Record to add the Report to.
     */
    public static void addReportToRecord(final Report report, final RecordStore record) {
        record.add();
        
        addSourceVertexToRecord(report, record);
        setAttributesInRecord(record, report.getSourceOtherAttributes(), GraphRecordStoreUtilities.SOURCE);
        
        addDestinationVertexToRecord(report, record);
        setAttributesInRecord(record, report.getDestinationOtherAttributes(), GraphRecordStoreUtilities.DESTINATION);
        
        addTransactionToRecord(report, record);
        setAttributesInRecord(record, report.getTransactionAttributes(), GraphRecordStoreUtilities.TRANSACTION);
    }
    
    private static void addNetworkEntityProperties(final String vertexType, final Record record) {
        record.set(vertexType + VisualConcept.VertexAttribute.COLOR, ConstellationColor.NAVY);
        record.set(vertexType + VisualConcept.VertexAttribute.FOREGROUND_ICON, AnalyticIconProvider.DESKTOP);
        record.set(vertexType + VisualConcept.VertexAttribute.BACKGROUND_ICON, IconManager.getIcon("Flat Circle"));
        record.set(vertexType + VisualConcept.VertexAttribute.SELECTED, true);
    }
    
    private static void addSourceVertexToRecord(final Report report, final Record record) {
        final String sourceVertexType = GraphRecordStoreUtilities.SOURCE;
        
        record.set(sourceVertexType + VisualConcept.VertexAttribute.IDENTIFIER, report.getSourceIdentifier());
        record.set(sourceVertexType + AnalyticConcept.VertexAttribute.TYPE, report.getSourceEntityType());
        record.set(sourceVertexType + ReportConcept.VertexAttribute.ENTITY_ID, report.getSourceEntityId());
        
        addNetworkEntityProperties(sourceVertexType, record);
    }
    
    private static void addDestinationVertexToRecord(final Report report, final Record record) { 
        final String destinationVertexType = GraphRecordStoreUtilities.DESTINATION;
        
        record.set(destinationVertexType + VisualConcept.VertexAttribute.IDENTIFIER, report.getDestinationIdentifier());
        record.set(destinationVertexType + AnalyticConcept.VertexAttribute.TYPE, report.getDestinationEntityType());
        record.set(destinationVertexType + ReportConcept.VertexAttribute.ENTITY_ID, report.getDestinationEntityId());
        
        addNetworkEntityProperties(destinationVertexType, record);
    }
    
    private static void addTransactionToRecord(final Report report, final Record record) {
        record.set(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.IDENTIFIER, report.getReportId());
        record.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.TYPE, ReportConcept.TransactionType.COMMUNICATION);
        record.set(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.SELECTED, true);
    }
    
    private static void setAttributesInRecord(final Record record, Map<String, Object> attributeMap, String attributeType) {
        GraphElementType elementType = attributeType.equals(GraphRecordStoreUtilities.TRANSACTION) 
                ? GraphElementType.TRANSACTION : GraphElementType.VERTEX;
        
        for (String field : attributeMap.keySet()) {
            SchemaAttribute attribute = createSchemaAttribute(attributeMap, field, elementType);
            String attributeNameWithKey = getSchemaAttributeNameWithKey(attribute);
            record.set(attributeType + attributeNameWithKey, attributeMap.get(field));
        }
    }
    
    private static SchemaAttribute createSchemaAttribute(Map<String, Object> attributeMap, String field, GraphElementType category) {
        Object attributeValue = attributeMap.get(field);
        String attributeType = getValueType(attributeValue);
        return new SchemaAttribute.Builder(category, attributeType, field).create().build();
    }
    
    private static String getSchemaAttributeNameWithKey(SchemaAttribute attribute) {
        String attributeType = attribute.getAttributeType();
        return attribute.getName() + "<" + attributeType + ">";
    }
    
    private static String getValueType(Object value) {
        if (value == null) {
            return StringAttributeDescription.ATTRIBUTE_NAME; // ????
        } else if (isDateTime(value)) {
           return ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME;
        } else if (value instanceof Boolean) {
            return BooleanAttributeDescription.ATTRIBUTE_NAME;
        } else if (value instanceof Double) {
            return DoubleAttributeDescription.ATTRIBUTE_NAME;
        } else if (value instanceof Integer) {
            return IntegerAttributeDescription.ATTRIBUTE_NAME;
        } else {
            return StringAttributeDescription.ATTRIBUTE_NAME;
        }
    }
    
    private static boolean isDateTime(Object value) {
        try {
            Instant.parse(value.toString());
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
}
 