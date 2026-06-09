/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin.importReportsPlugin;

import au.gov.asd.tac.constellation.networkPlugin.NetworkPluginPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.json.JsonUtilities;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.AuthenticationException;
import org.json.JSONArray;

/**
 *
 * @author Intern-1003972
 */
public class ApiServerUtilities {
    
    private static final String AUTHENTICATION_FAILED_MESSAGE = "Wrong API key!";
    
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    
    public static String callReportIdsApi(final String userId, final String apiKey) throws AuthenticationException, IOException, InterruptedException {
        HttpRequest responseIdsRequest = constructReportIdsRequest(userId, apiKey);
        HttpResponse<String> response = client.send(responseIdsRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == HttpURLConnection.HTTP_FORBIDDEN) {
            throw new AuthenticationException(AUTHENTICATION_FAILED_MESSAGE);
        }
        return response.body();
    }
    
    private static HttpRequest constructReportIdsRequest(final String userId, final String apiKey) {
        String baseUrl = NetworkPluginPreferenceKeys.DB_SERVER_BASE_ENDPOINT;
        String endpoint = String.format(NetworkPluginPreferenceKeys.PATH_FETCH_REPORT_IDS, userId);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("X-API-KEY", apiKey)
                .GET()
                .build();
        
        return request;
    }
    
    public static String callReportsApi(final String userId, final List<String> reportIds) throws IOException, InterruptedException {
        HttpRequest reportsRequest = constructReportsRequest(userId, reportIds);
        HttpResponse<String> response = client.send(reportsRequest, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    
    private static HttpRequest constructReportsRequest(final String userId, final List<String> reportIds) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("reportIds", new JSONArray(reportIds));
        String requestBody = JsonUtilities.getMapAsString(bodyMap);
        
        String baseUrl = NetworkPluginPreferenceKeys.DB_SERVER_BASE_ENDPOINT;
        String endpoint = String.format(NetworkPluginPreferenceKeys.PATH_FETCH_REPORTS, userId);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        return request;
    }
    
}
