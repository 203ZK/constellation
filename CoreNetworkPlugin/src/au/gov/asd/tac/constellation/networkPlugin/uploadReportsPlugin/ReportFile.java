/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin.uploadReportsPlugin;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.networkPlugin.Report;
import au.gov.asd.tac.constellation.networkPlugin.ReportConcept;
import static au.gov.asd.tac.constellation.networkPlugin.uploadReportsPlugin.UploadReportsPluginParser.parseReport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Represents an uploaded file that has been parsed by an ImportFileParser.
 */
public class ReportFile {
    
    private static final String INTERNAL_USER_ID = "internal_user_id";
    private static final String REPORT_ID = "report_id";
    private static final String REPORT_NAME = "report_name";
    
    private final String fileName;
    private final Map<String, Integer> headerMap;
    private final List<String[]> data;
    
    public ReportFile(String fileName, List<String[]> contents) {
        this.fileName = fileName;
        this.headerMap = mapHeaders(contents.getFirst());
        this.data = contents.subList(1, contents.size());
    }
    
    /**
     * Maps the required headerMap from their column index to their title.
     */
    private Map<String, Integer> mapHeaders(final String[] headers) {
        Map<String, Integer> result = new HashMap<>();
        for (int colIdx = 0; colIdx < headers.length; colIdx++) {
            result.put(headers[colIdx], colIdx);
        }
        return result;
    }
    
    /**
     * Retrieves the required headerMap that are missing from the file.
     */
    public void verifyHeaders() throws MissingHeadersException {
        List<String> requiredHeaders = new ArrayList<>(Arrays.asList(
                INTERNAL_USER_ID, REPORT_ID, REPORT_NAME, 
                GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, 
                GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, 
                GraphRecordStoreUtilities.SOURCE + ReportConcept.VertexAttribute.ENTITY_ID, 
                GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, 
                GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.TYPE, 
                GraphRecordStoreUtilities.DESTINATION + ReportConcept.VertexAttribute.ENTITY_ID
        ));
        
        List<String> missingHeaders = requiredHeaders.stream()
                .filter(requiredHeader -> !this.headerMap.containsKey(requiredHeader))
                .toList();
        
        if (!missingHeaders.isEmpty()) {
            throw new MissingHeadersException(this.fileName, missingHeaders);
        }
    }
    
    public List<Report> mapToReportList() {
        List<Report> parsedReports = new ArrayList<>();
        
        for (String[] row : this.data) {
            Report report = parseReport(this.headerMap, row);
            parsedReports.add(report);
        }
        
        return parsedReports;
    }
}
