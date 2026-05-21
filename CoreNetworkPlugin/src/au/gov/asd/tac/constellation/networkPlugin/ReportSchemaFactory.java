/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept.ConstellationViewsConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Intern-1003972
 */
@ServiceProvider(service = SchemaFactory.class, position = 0)
public class ReportSchemaFactory extends AnalyticSchemaFactory {
    
    public static final String NAME = 
            "au.gov.asd.tac.constellation.networkPlugin.schema.ReportSchemaFactory";
    
    @Override
    public String getName() {
        return NAME;
    }
    
    @Override
    public String getLabel() {
        return "Network Report Graph";
    }

    @Override
    public String getDescription() {
        return "This graph provides support for analysing network reports";
    }
    
    @Override
    public Set<Class<? extends SchemaConcept>> getRegisteredConcepts() {
        final Set<Class<? extends SchemaConcept>> registeredConcepts = new HashSet<>();
        registeredConcepts.add(ConstellationViewsConcept.class);
        registeredConcepts.add(VisualConcept.class);
        registeredConcepts.add(AnalyticConcept.class);
        return Collections.unmodifiableSet(registeredConcepts);
    }
    
    @Override
    public Schema createSchema() {
        return new ReportSchema(this);
    }
    
    protected class ReportSchema extends AnalyticSchema {
        public ReportSchema(final SchemaFactory factory) {
            super(factory);
        }
        
        @Override
        public void completeVertex(final GraphWriteMethods graph, final int vertex) {
            final int typeAttributeId = AnalyticConcept.VertexAttribute.TYPE.get(graph);
            graph.setStringValue(typeAttributeId, vertex, "Network Entity");
            super.completeVertex(graph, vertex);
        }
    }
    
}
