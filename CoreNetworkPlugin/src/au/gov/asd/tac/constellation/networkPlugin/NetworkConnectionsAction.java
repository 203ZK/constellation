package au.gov.asd.tac.constellation.networkPlugin;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.SimplePluginAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * Network Connections Visualization Action.
 */
@ActionID(category = "Tools", id = "au.gov.asd.tac.constellation.networkPlugin.NetworkConnectionsAction")
@ActionRegistration(displayName = "#CTL_NetworkConnectionsAction", iconBase = "", surviveFocusChange = true)
@NbBundle.Messages("CTL_NetworkConnectionsAction=Network Connections")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 0),
    @ActionReference(path = "Shortcuts", name = "C-N")
})
public class NetworkConnectionsAction extends SimplePluginAction {
    
    public NetworkConnectionsAction(final GraphNode context) {
        super(context, NetworkPluginRegistry.NETWORK_CONNECTIONS, true);
    }
    
}
