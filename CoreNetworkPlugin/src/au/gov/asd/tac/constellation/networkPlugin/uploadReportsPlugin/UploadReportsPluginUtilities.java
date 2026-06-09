/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin.uploadReportsPlugin;

import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.networkPlugin.Report;
import static au.gov.asd.tac.constellation.networkPlugin.importReportsPlugin.ImportReportsPluginUtilities.addReportToRecord;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.ImportFileParser;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.InputSource;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Utilities for the Upload Network Reports Plugin.
 */
public class UploadReportsPluginUtilities {
    
    public static ReportFile processFileData(final File file, final ImportFileParser parser) throws MissingHeadersException, IOException {
        final List<String[]> contents = parser.parse(new InputSource(file), null);
        final ReportFile reportFile = new ReportFile(file.getName(), contents);
        reportFile.verifyHeaders();
        return reportFile;
    }
    
    public static void addFilesToRecord(final List<ReportFile> validReportFiles, final RecordStore record) {
        for (ReportFile reportFile : validReportFiles) {
            List<Report> reports = reportFile.mapToReportList();
            reports.forEach((report) -> addReportToRecord(report, record));
        }
    }
}
