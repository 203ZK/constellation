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
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 *
 * @author Intern-1003972
 */
public class Report {
    
    private final String internalUserId;
    private final String reportId;
    
    private final String sourceIdentifier;
    private final String sourceEntityType;
    private final String sourceEntityId;
    private final Map<String, Object> sourceOtherAttributes;
    
    private final String destinationIdentifier;
    private final String destinationEntityType;
    private final String destinationEntityId;
    private final Map<String, Object> destinationOtherAttributes;
    
    private final Map<String, Object> transactionAttributes;
    
    public Report(
            String internalUserId, String reportId,
            String sourceIdentifier, String sourceType, 
            String sourceEntityId, Map<String, Object> sourceOtherAttributes,
            String destinationIdentifier, String destinationType,
            String destinationEntityId, Map<String, Object> destinationOtherAttributes,
            Map<String, Object> transactionAttributes
    ) {
        this.internalUserId = internalUserId;
        this.reportId = reportId;
        this.sourceIdentifier = sourceIdentifier;
        this.sourceEntityType = sourceType;
        this.sourceEntityId = sourceEntityId;
        this.sourceOtherAttributes = sourceOtherAttributes;
        this.destinationIdentifier = destinationIdentifier;
        this.destinationEntityType = destinationType;
        this.destinationEntityId = destinationEntityId;
        this.destinationOtherAttributes = destinationOtherAttributes;
        this.transactionAttributes = transactionAttributes;
    }
    
    public String getInternalUserId() {
        return this.internalUserId;
    }
    
    public String getReportId() {
        return this.reportId;
    }
    
    public String getSourceIdentifier() {
        return this.sourceIdentifier;
    }
    
    public String getSourceEntityType() {
        return this.sourceEntityType;
    }
    
    public String getSourceEntityId() {
        return this.sourceEntityId;
    }
    
    public Map<String, Object> getSourceOtherAttributes() {
        return this.sourceOtherAttributes;
    }
    
    public String getDestinationIdentifier() {
        return this.destinationIdentifier;
    }
    
    public String getDestinationEntityType() {
        return this.destinationEntityType;
    }
    
    public String getDestinationEntityId() {
        return this.destinationEntityId;
    }
    
    public Map<String, Object> getDestinationOtherAttributes() {
        return this.destinationOtherAttributes;
    }
    
    public Map<String, Object> getTransactionAttributes() {
        return this.transactionAttributes;
    }
    
    private String getValueType(Object value) {
//        final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX")
//            .withResolverStyle(ResolverStyle.STRICT);
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
    
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
    
}
