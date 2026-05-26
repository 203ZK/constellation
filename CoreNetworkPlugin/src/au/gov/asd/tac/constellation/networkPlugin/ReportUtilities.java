/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.Record;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.utilities.json.JsonUtilities;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.AuthenticationException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Report Utilities.
 */
public class ReportUtilities {
    
    private static final String BASE_URL = "http://localhost:8080";
    private static final String PATH_FETCH_REPORT_IDS = "/reportIds";
    private static final String PATH_FETCH_REPORTS = "/reports";
    
    // These fields have to be aligned with the server
    private static final String INTERNAL_USER_ID = "internal_user_id";
    private static final String REPORT_ID = "report_id";
    private static final String SOURCE_OTHER_ATTRIBUTES = "source.attributes";
    private static final String DESTINATION_OTHER_ATTRIBUTES = "destination.attributes";
    // Anything that isn't a source/destination attribute is considered a transaction attribute
    private static final String TRANSACTION_ATTRIBUTES = "transaction.attributes";
    
    
    public static List<String> getReportIds(
            final String userId, final String apiKey
    ) throws AuthenticationException, ParseException, IOException, InterruptedException {
        String response = fetchReportIds(userId, apiKey);
        return parseReportIds(response);
    }
    
    private static List<String> parseReportIds(final String reportIdsString) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONArray reportIds = (JSONArray) parser.parse(reportIdsString);
        
        List<String> parsedReportIds = new ArrayList<>();
        
        for (int i = 0; i < reportIds.size(); i++) {
            String reportId = (String) reportIds.get(i);
            parsedReportIds.add(reportId);
        }
        
        return parsedReportIds;
    }
    
    private static String fetchReportIds(final String userId, final String apiKey) throws AuthenticationException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        
        Map<String, String> bodyMap = new HashMap();
        bodyMap.put("userId", userId);
        String requestBody = JsonUtilities.getMapAsString(bodyMap);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_FETCH_REPORT_IDS))
                .header("Content-Type", "application/json")
                .header("X-API-KEY", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == HttpURLConnection.HTTP_FORBIDDEN) {
            throw new AuthenticationException("Wrong API key!");
        }
        
        return response.body();
    }
    
    public static List<Report> getReports(final String userId, final String reportId) throws ParseException, IOException, InterruptedException {
        String response = fetchReports(userId, reportId);
        return parseReports(response);
    }
    
    private static String fetchReports(final String userId, final String reportId) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        
        Map<String, String> bodyMap = new HashMap();
        bodyMap.put("userId", userId);
        bodyMap.put("reportId", reportId);
        String requestBody = JsonUtilities.getMapAsString(bodyMap);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PATH_FETCH_REPORTS))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return response.body();
    }
    
    private static List<Report> parseReports(final String reportsString) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONArray jsonReports = (JSONArray) parser.parse(reportsString);
        
        List<Report> parsedReports = new ArrayList<>();
        
        for (int i = 0; i < jsonReports.size(); i++) {
            JSONObject report = (JSONObject) jsonReports.get(i);
            Report parsedReport = parseReport(report);
            if (parsedReport != null) {
                parsedReports.add(parsedReport);
            }
        }
        
        return parsedReports;
    }
    
    /**
     * Each Report must have the following fields.
     * - Metadata fields: internal_user_id, report_id, 
     * - Source/destination identifier, (entity) type, entity ID,
     * - Source/destination additional attributes
     * - Transaction attributes
     */
    private static Report parseReport(final JSONObject reportJson) {
        String internalUserId = (String) reportJson.get(INTERNAL_USER_ID);
        String reportId = (String) reportJson.get(REPORT_ID);
        
        String sourceIdentifier = (String) reportJson.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER);
        String sourceType = (String) reportJson.get(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE);
        String sourceEntityId = (String) reportJson.get(GraphRecordStoreUtilities.SOURCE + ReportConcept.VertexAttribute.ENTITY_ID);
        
        JSONObject sourceOtherAttributesJson = (JSONObject) reportJson.get(SOURCE_OTHER_ATTRIBUTES);
        Map<String, Object> sourceOtherAttributes = parseAttributes(sourceOtherAttributesJson);
        
        String destinationIdentifier = (String) reportJson.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER);
        String destinationType = (String) reportJson.get(GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.TYPE);
        String destinationEntityId = (String) reportJson.get(GraphRecordStoreUtilities.DESTINATION + ReportConcept.VertexAttribute.ENTITY_ID);
        
        JSONObject destinationOtherAttributesJson = (JSONObject) reportJson.get(DESTINATION_OTHER_ATTRIBUTES);
        Map<String, Object> destinationOtherAttributes = parseAttributes(destinationOtherAttributesJson);
        
        JSONObject transactionAttributesJson = (JSONObject) reportJson.get(TRANSACTION_ATTRIBUTES);
        Map<String, Object> transactionAttributes = parseAttributes(transactionAttributesJson);
        
        return new Report(
                internalUserId, reportId, 
                sourceIdentifier, sourceType, sourceEntityId, sourceOtherAttributes,
                destinationIdentifier, destinationType, destinationEntityId, destinationOtherAttributes,
                transactionAttributes
        );
    }
    
    /**
     * Parses a JSON map as a map of attributes.
    */
    private static Map<String, Object> parseAttributes(final JSONObject attributesJson) {
        Map<String, Object> attributes = new HashMap<>();
        for (Object key : attributesJson.keySet()) {
            Object value = attributesJson.get(key);
            attributes.put((String) key, value);
        }
        return attributes;
    }
    
    /**
     * Adds the attributes of a Report instance to the current row of a
     * RecordStore, specified as a Record. Each Report contains both source and destination
     * vertices to be added to the RecordStore.
     *
     * @param report the Report instance to be added.
     * @param record the Record to add the Report to.
     */
    public static void addReportToRecord(final Report report, final Record record) {
        record.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, report.getSourceIdentifier());
        record.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, ReportConcept.VertexType.NETWORK_ENTITY);
        record.set(GraphRecordStoreUtilities.SOURCE + ReportConcept.VertexAttribute.ENTITY_ID, report.getSourceEntityId());
        
        record.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, report.getDestinationIdentifier());
        record.set(GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.TYPE, ReportConcept.VertexType.NETWORK_ENTITY);
        record.set(GraphRecordStoreUtilities.DESTINATION + ReportConcept.VertexAttribute.ENTITY_ID, report.getDestinationEntityId());

        record.set(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.IDENTIFIER, report.getReportId());
        record.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.TYPE, ReportConcept.TransactionType.COMMUNICATION);
    
        setAttributesInRecord(record, report.getSourceOtherAttributes(), GraphRecordStoreUtilities.SOURCE);
        setAttributesInRecord(record, report.getDestinationOtherAttributes(), GraphRecordStoreUtilities.DESTINATION);
        setAttributesInRecord(record, report.getTransactionAttributes(), GraphRecordStoreUtilities.TRANSACTION);
    }
    
    private static void setAttributesInRecord(final Record record, Map<String, Object> attributes, String attributeType) {
        GraphElementType elementType = attributeType.equals(GraphRecordStoreUtilities.TRANSACTION) ? GraphElementType.TRANSACTION : GraphElementType.VERTEX;
        for (String field : attributes.keySet()) {
            SchemaAttribute attribute = createSchemaAttribute(attributes, field, elementType);
            String attributeNameWithKey = getSchemaAttributeNameWithKey(attribute);
            record.set(attributeType + attributeNameWithKey, attributes.get(field));
        }
    }
    
    private static SchemaAttribute createSchemaAttribute(Map<String, Object> attributeMap, String field, GraphElementType category) {
        Object value = attributeMap.get(field);
        String type = getValueType(value);
        
        SchemaAttribute attribute = new SchemaAttribute.Builder(category, type, field)
                .create()
                .build();
        
        return attribute;
    }
    
    private static String getSchemaAttributeNameWithKey(SchemaAttribute attribute) {
        String attributeType = attribute.getAttributeType();
        return attribute.getName() + "<" + attributeType + ">";
    }
    
    private static String getValueType(Object value) {
        try {
            Instant.parse(value.toString());
            return ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME;
        } catch (DateTimeParseException e) { // Continue checking other types
        }
        
        if (value instanceof Boolean) {
            return BooleanAttributeDescription.ATTRIBUTE_NAME;
        } else if (value instanceof Float) {
            return FloatAttributeDescription.ATTRIBUTE_NAME;
        } else if (value instanceof Integer) {
            return IntegerAttributeDescription.ATTRIBUTE_NAME;
        } else {
            return StringAttributeDescription.ATTRIBUTE_NAME;
        }
    }
    
}
 