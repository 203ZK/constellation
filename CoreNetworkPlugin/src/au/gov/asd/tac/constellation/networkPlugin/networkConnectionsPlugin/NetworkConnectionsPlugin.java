/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin.networkConnectionsPlugin;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
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
        final String nodeIdentifier = parameters.getStringValue(NODE_PARAMETER_ID);
        
        final Set<Integer> vertexElements = new HashSet<>();
        final Set<Integer> transactionElements = new HashSet<>();
        
        selectElements(graph, vertexElements, transactionElements, nodeIdentifier);
        copyElements(graph, vertexElements, transactionElements);
        
        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.COPY_TO_NEW_GRAPH).executeNow(graph);
        clearElements(graph, vertexElements, transactionElements);
    }
    
    private void selectElements(final GraphWriteMethods graph, final Set<Integer> vertexElements, final Set<Integer> transactionElements, final String nodeIdentifier) {
        final int vertexIdentifierId = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);
        
        for (int vertexPosition = 0; vertexPosition < graph.getVertexCount(); vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            final String vertexIdentifier = graph.getStringValue(vertexIdentifierId, vertexId);
            
            if (!nodeIdentifier.equals(vertexIdentifier)) { continue; }
            
            final int neighbourCount = graph.getVertexNeighbourCount(vertexId);
            for (int neighbourPosition = 0; neighbourPosition < neighbourCount; neighbourPosition++) {
                final int neighbourId = graph.getVertexNeighbour(vertexId, neighbourPosition);
                final int neighbourLink = graph.getLink(vertexId, neighbourId);
                final int transactionCount = graph.getLinkTransactionCount(neighbourLink);
                
                for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                    final int transactionId = graph.getLinkTransaction(neighbourLink, transactionPosition);
                    
                    vertexElements.add(vertexId);
                    vertexElements.add(neighbourId);
                    transactionElements.add(transactionId);
                }
            }
        }
    }
    
    private void copyElements(final GraphWriteMethods graph, final Set<Integer> vertexElements, final Set<Integer> transactionElements) {
        final int vertexSelectedId = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int transactionSelectedId = VisualConcept.TransactionAttribute.SELECTED.get(graph);
        
        for (int vertexElement : vertexElements) {
            graph.setBooleanValue(vertexSelectedId, vertexElement, true);
        }
        
        for (int transactionElement : transactionElements) {
            graph.setBooleanValue(transactionSelectedId, transactionElement, true);
        }
    }
    
    private void clearElements(final GraphWriteMethods graph, final Set<Integer> vertexElements, final Set<Integer> transactionElements) {
        final int vertexSelectedId = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int transactionSelectedId = VisualConcept.TransactionAttribute.SELECTED.get(graph);
        
        for (int vertexElement : vertexElements) {
            graph.setBooleanValue(vertexSelectedId, vertexElement, false);
        }
        
        for (int transactionElement : transactionElements) {
            graph.setBooleanValue(transactionSelectedId, transactionElement, false);
        }
    }
    
}
