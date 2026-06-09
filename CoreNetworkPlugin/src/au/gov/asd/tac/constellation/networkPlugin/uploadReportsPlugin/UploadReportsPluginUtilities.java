/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin.uploadReportsPlugin;

import au.gov.asd.tac.constellation.networkPlugin.importReportsPlugin.ImportReportsPluginParser;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.networkPlugin.Report;
import au.gov.asd.tac.constellation.networkPlugin.ReportConcept;
import static au.gov.asd.tac.constellation.networkPlugin.importReportsPlugin.ImportReportsPluginUtilities.addReportToRecord;
import static au.gov.asd.tac.constellation.networkPlugin.uploadReportsPlugin.UploadReportsPluginParser.parseReport;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.ImportFileParser;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.InputSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Intern-1003972
 */
public class UploadReportsPluginUtilities {
    
    // These fields have to be aligned with the server
    private static final String INTERNAL_USER_ID = "internal_user_id";
    private static final String REPORT_ID = "report_id";
    private static final String REPORT_NAME = "report_name";
    
    public static List<String[]> processFileData(final File file, final ImportFileParser parser) throws MissingHeadersException, IOException {
        List<String[]> data = parser.parse(new InputSource(file), null);
        verifyHeaders(file.getPath(), data);
        return data;
    }
    
    /**
     * Retrieves the required headers that are missing from the file.
     */
    public static List<String> verifyHeaders(final String[] headers) {
        List<String> requiredHeaders = new ArrayList<>(Arrays.asList(
                INTERNAL_USER_ID, REPORT_ID, REPORT_NAME, 
                GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, 
                GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, 
                GraphRecordStoreUtilities.SOURCE + ReportConcept.VertexAttribute.ENTITY_ID, 
                GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, 
                GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.TYPE, 
                GraphRecordStoreUtilities.DESTINATION + ReportConcept.VertexAttribute.ENTITY_ID
        ));
        
        requiredHeaders.removeAll(new HashSet<>(Arrays.asList(headers)));
        return requiredHeaders;
    }
    
    /**
     * Maps the required headers from their column index to their title.
     */
    public static Map<String, Integer> mapHeaders(final String[] headers) {
        Map<String, Integer> headerMap = new HashMap<>();
        for (int colIdx = 0; colIdx < headers.length; colIdx++) {
            headerMap.put(headers[colIdx], colIdx);
        }
        return headerMap;
    }
    
    public static void verifyHeaders(String fileName, List<String[]> data) throws MissingHeadersException {
        String[] headers = data.get(0);
        List<String> missingHeaders = verifyHeaders(headers);
        if (!missingHeaders.isEmpty()) {
            throw new MissingHeadersException(fileName, missingHeaders);
        }
    }
    
    protected static List<Report> mapFileToReports(final List<String[]> fileData) {
        final String[] headers = fileData.get(0);
        Map<String, Integer> headerMap = mapHeaders(headers);
        
        List<Report> parsedReports = new ArrayList<>();
        for (int rowIdx = 1; rowIdx < fileData.size(); rowIdx++) {
            String[] dataRow = fileData.get(rowIdx);
            Report report = parseReport(headerMap, dataRow);
            parsedReports.add(report);
        }
        
        return parsedReports;
    }
    
    public static void addFilesToRecord(final List<List<String[]>> validFileData, final RecordStore record) {
        for (List<String[]> fileData : validFileData) {
            List<Report> reports = mapFileToReports(fileData);
            reports.forEach((report) -> addReportToRecord(report, record));
        }
    }
    
}
