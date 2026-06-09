/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Intern-1003972
 */
@ServiceProvider(service = SchemaConcept.class)
public class ReportConcept extends SchemaConcept {

    private static final String NAME = "Report";
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getParents() {
        final Set<Class<? extends SchemaConcept>> parentSet = new HashSet<>();
        parentSet.add(AnalyticConcept.class);
        return Collections.unmodifiableSet(parentSet);
    }
    
    public static class VertexAttribute {
        public static final SchemaAttribute ENTITY_ID = 
                new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "EntityId")
                        .setDescription("The readable identifier of the entity (e.g., user_ABC123)")
                        .create()
                        .build();
    }
    
    @Override
    public Collection<SchemaAttribute> getSchemaAttributes() {
        final List<SchemaAttribute> schemaAttributes = new ArrayList<>();
        schemaAttributes.add(VertexAttribute.ENTITY_ID);
        return Collections.unmodifiableCollection(schemaAttributes);
    }
    
    public static class TransactionType {
        public static final SchemaTransactionType COMMUNICATION = new SchemaTransactionType.Builder("Communication")
                .setDescription("A communication session between two network nodes.")
                .setColor(ConstellationColor.MAGENTA)
                .build();
    }
    
    @Override
    public List<SchemaTransactionType> getSchemaTransactionTypes() {
        final List<SchemaTransactionType> schemaTransactionTypes = new ArrayList<>();
        schemaTransactionTypes.add(TransactionType.COMMUNICATION);
        return Collections.unmodifiableList(schemaTransactionTypes);
    }
    
}
