/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Network Connections Visualization Plugin.
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("NetworkConnectionsPlugin=Network Connections")
@PluginInfo(pluginType = PluginType.NONE, tags = {"NETWORK"})
public class NetworkConnectionsPlugin extends SimpleEditPlugin {

    public static final String NODE_PARAMETER_ID = PluginParameter.buildId(NetworkConnectionsPlugin.class, "node");
    private static final String NODE_PARAMETER_LABEL = "Network Node";

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();
        final PluginParameter nodeParameter = SingleChoiceParameterType.build(NODE_PARAMETER_ID);
        nodeParameter.setName(NODE_PARAMETER_LABEL);
        parameters.addParameter(nodeParameter);
        return parameters;
    }
    
    @Override
    public void updateParameters(final Graph graph, final PluginParameters parameters) {
        final Set<String> nodes = new HashSet<>();
        final ReadableGraph readableGraph = graph.getReadableGraph();
        
        try {
            final int nodeParameterId = VisualConcept.VertexAttribute.IDENTIFIER.get(readableGraph);
            for (int vertexPosition = 0; vertexPosition < readableGraph.getVertexCount(); vertexPosition++) {
                final int vertexId = readableGraph.getVertex(vertexPosition);
                final String identifier = readableGraph.getStringValue(nodeParameterId, vertexId);
                nodes.add(identifier);
            }
        } finally {
            readableGraph.release();
        }
        
        SingleChoiceParameterType.setOptions(
                (PluginParameter<SingleChoiceParameterType.SingleChoiceParameterValue>) parameters.getParameters().get(NODE_PARAMETER_ID), 
                new ArrayList<>(nodes));
    }
    
    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final String nodeName = parameters.getStringValue(NODE_PARAMETER_ID);
        final int vertexIdentifierId = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);
        final int vertexTypeId = AnalyticConcept.VertexAttribute.TYPE.get(graph);
        final int transactionIdentifierId = VisualConcept.TransactionAttribute.IDENTIFIER.get(graph);
        final int transactionTypeId = AnalyticConcept.TransactionAttribute.TYPE.get(graph);
                
        final RecordStore record = new GraphRecordStore();
        
        for (int vertexPosition = 0; vertexPosition < graph.getVertexCount(); vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            final String vertexIdentifier = graph.getStringValue(vertexIdentifierId, vertexId);
            final String vertexType = graph.getStringValue(vertexTypeId, vertexId);
            
            if (!nodeName.equals(vertexIdentifier)) { continue; }
            
            final int neighbourCount = graph.getVertexNeighbourCount(vertexId);
            for (int neighbourPosition = 0; neighbourPosition < neighbourCount; neighbourPosition++) {
                final int neighbourId = graph.getVertexNeighbour(vertexId, neighbourPosition);
                final String neighbourIdentifier = graph.getStringValue(vertexIdentifierId, neighbourId);
                final String neighbourType = graph.getStringValue(vertexTypeId, neighbourId);
                
                final int neighbourLink = graph.getLink(vertexId, neighbourId);
                final int transactionCount = graph.getLinkTransactionCount(neighbourLink);
                
                for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                    final int transactionId = graph.getLinkTransaction(neighbourLink, transactionPosition);
                    final String transactionIdentifier = graph.getStringValue(transactionIdentifierId, transactionId);
                    final String transactionType = graph.getStringValue(transactionTypeId, transactionId);
                    
                    record.add();
                    
                    record.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, vertexIdentifier);
                    record.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, vertexType);
                
                    record.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, neighbourIdentifier);
                    record.set(GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.TYPE, neighbourType);
                    
                    record.set(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.IDENTIFIER, transactionIdentifier);
                    record.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.TYPE, transactionType);
                }
            }
        }
            
        GraphRecordStoreUtilities.addRecordStoreToGraph(graph, record, false, true, null);
    }
    
}
