/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin;

import au.gov.asd.tac.constellation.networkPlugin.networkConnectionsPlugin.NetworkConnectionsPlugin;
import au.gov.asd.tac.constellation.networkPlugin.uploadReportsPlugin.UploadNetworkReportsPlugin;
import au.gov.asd.tac.constellation.networkPlugin.importReportsPlugin.ImportNetworkReportsPlugin;

/**
 * Network Plugin Registry.
 */
public class NetworkPluginRegistry {
    public static final String IMPORT_NETWORK_REPORTS = ImportNetworkReportsPlugin.class.getName();
    public static final String NETWORK_CONNECTIONS = NetworkConnectionsPlugin.class.getName();
    public static final String UPLOAD_NETWORK_REPORTS = UploadNetworkReportsPlugin.class.getName();
}
