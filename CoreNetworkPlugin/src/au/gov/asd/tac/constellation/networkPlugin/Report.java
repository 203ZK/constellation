/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin;

import java.lang.reflect.Field;
import java.util.Map;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 *
 * @author Intern-1003972
 */
public class Report {
    
    private final String internalUserId;
    private final String reportId;
    
    private final String sourceIdentifier;
    private final String sourceType;
    private final String sourceEntityId;
    private final Map<String, Object> sourceOtherAttributes;
    
    private final String destinationIdentifier;
    private final String destinationType;
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
        this.sourceType = sourceType;
        this.sourceEntityId = sourceEntityId;
        this.sourceOtherAttributes = sourceOtherAttributes;
        this.destinationIdentifier = destinationIdentifier;
        this.destinationType = destinationType;
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
    
    public String getSourceType() {
        return this.sourceType;
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
    
    public String getDestinationType() {
        return this.destinationType;
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
    
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
    
}
