/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin;

import au.gov.asd.tac.constellation.networkPlugin.importReportsPlugin.ImportReportsPluginParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Intern-1003972
 */
public class ReportPluginParserNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }
    
    @Test
    public void testParseReportOptions_correctFormat() throws Exception {
        final String testReportOptionsString = """
                [
                    {
                        "report_id": "r12345",
                        "report_name": "Report #12345"
                    },
                    {
                        "report_id": "r12346",
                        "report_name": "Report #12346"
                    }
                ]
                """;
        
        final Map<String, String> actual = 
                ImportReportsPluginParser.parseReportOptions(testReportOptionsString);
        
        final Map<String, String> expected = new HashMap<>();
        expected.put("Report #12345 (ID: r12345)", "r12345");
        expected.put("Report #12346 (ID: r12346)", "r12346");
        
        assertEquals(actual, expected);
    }
    
    @Test
    public void testParseReportOptions_wrongFormat() throws Exception {
        final String testReportOptionsString = """
                [
                    {
                        "report_id": "r12345",
                        "report_name": "Report #12345"
                    },
                    {
                        "report_id": "r12346",
                        "report_name": "Report #12346"
                    },
                ]
                """;
        
        Assert.assertThrows(JsonProcessingException.class,
                () -> ImportReportsPluginParser.parseReportOptions(testReportOptionsString)
        );
    }
    
    @Test
    public void testParseReportOptions_noReportName() throws Exception {
        final String testReportOptionsString = """
                [
                    {
                        "report_id": "r12345"
                    }
                ]
                """;
        
        final Map<String, String> actual = 
                ImportReportsPluginParser.parseReportOptions(testReportOptionsString);
        
        final Map<String, String> expected = new HashMap<>();
        expected.put("null (ID: r12345)", "r12345");
        
        assertEquals(actual, expected);
    }
    
    @Test
    public void testParseReportOptions_noReportId() throws Exception {
        final String testReportOptionsString = """
                [
                    {
                        "report_name": "Report #12345"
                    }
                ]
                """;
        
        final Map<String, String> actual = 
                ImportReportsPluginParser.parseReportOptions(testReportOptionsString);
        
        final Map<String, String> expected = new HashMap<>();
        expected.put("Report #12345 (ID: null)", null);
        
        assertEquals(actual, expected);
    }
    
    @Test
    public void testParseReportOptions_extraFields() throws Exception {
        final String testReportOptionsString = """
                [
                    {
                        "report_id": "r12345",
                        "report_name": "Report #12345",
                        "extra_field": 123
                    }
                ]
                """;
        
        final Map<String, String> actual = 
                ImportReportsPluginParser.parseReportOptions(testReportOptionsString);
        
        final Map<String, String> expected = new HashMap<>();
        expected.put("Report #12345 (ID: r12345)", "r12345");
        
        assertEquals(actual, expected);
    }
    
    @Test
    public void testParseReport_normalReport() throws Exception {
        final String testReportsString = """
                [
                    {
                        "internal_user_id": "d000",
                        "report_id": "r12345",
                        "report_name": "Report #12345",
                        "source.Identifier": "123.123.123.123",
                        "source.Type": "user_endpoint",
                        "source.EntityId": "user_ABC",
                        "destination.Identifier": "234.234.234.234",
                        "destination.Type": "server",
                        "destination.EntityId": "server_ABC",
                        "source.Attributes": {
                            "ExtraField1": "extra_value",
                            "ExtraField2": 100,
                            "ExtraField3": true,
                            "ExtraField4": null
                        },
                        "destination.Attributes": {
                            "ExtraField1": "extra_value",
                            "ExtraField2": 100,
                            "ExtraField3": true,
                            "ExtraField4": null
                        },
                        "transaction.Attributes": {
                            "ExtraField1": "extra_value",
                            "ExtraField2": 100,
                            "ExtraField3": true,
                            "ExtraField4": null
                        }
                    }
                ]
                """;
       
        final List<Report> actual = ImportReportsPluginParser.parseReports(testReportsString);
        
        assertEquals(1, actual.size());
        
        final Report actualReport = actual.get(0);
        
        assertEquals(actualReport.getInternalUserId(), "d000");
        assertEquals(actualReport.getReportId(), "r12345");
        assertEquals(actualReport.getReportName(), "Report #12345");
        
        assertEquals(actualReport.getSourceIdentifier(), "123.123.123.123");
        assertEquals(actualReport.getSourceEntityType(), "user_endpoint");
        assertEquals(actualReport.getSourceEntityId(), "user_ABC");
        assertEquals(actualReport.getDestinationIdentifier(), "234.234.234.234");
        assertEquals(actualReport.getDestinationEntityType(), "server");
        assertEquals(actualReport.getDestinationEntityId(), "server_ABC");
        
        final Map<String, Object> transactionAttributes = actualReport.getTransactionAttributes();
        assertEquals(transactionAttributes.get("ExtraField1"), "extra_value");
        assertEquals(transactionAttributes.get("ExtraField2"), 100);
        assertEquals(transactionAttributes.get("ExtraField3"), true);
        assertEquals(transactionAttributes.get("ExtraField4"), null);
    }
    
    @Test
    public void testParseReport_noSourceAttributes() throws Exception {
        final String testReportsString = """
                [
                    {
                        "internal_user_id": "d000",
                        "report_id": "r12345",
                        "report_name": "Report #12345",
                        "source.Identifier": "123.123.123.123",
                        "source.Type": "user_endpoint",
                        "source.EntityId": "user_ABC",
                        "destination.Identifier": "234.234.234.234",
                        "destination.Type": "server",
                        "destination.EntityId": "server_ABC",
                        "source.Attributes": {},
                        "destination.Attributes": {},
                        "transaction.Attributes": {}
                    }
                ]
                """;
       
        final List<Report> actual = ImportReportsPluginParser.parseReports(testReportsString);
        final Report actualReport = actual.get(0);
        
        assertTrue(actualReport.getSourceOtherAttributes().isEmpty());
        assertTrue(actualReport.getDestinationOtherAttributes().isEmpty());
        assertTrue(actualReport.getTransactionAttributes().isEmpty());
    }
    
    @Test
    public void testParseReport_noEntityTypeAndId() throws Exception {
        final String testReportsString = """
                [
                    {
                        "internal_user_id": "d000",
                        "report_id": "r12345",
                        "report_name": "Report #12345",
                        "source.Identifier": "123.123.123.123",
                        "source.Type": null,
                        "source.EntityId": null,
                        "destination.Identifier": "234.234.234.234",
                        "destination.Type": null,
                        "destination.EntityId": null,
                        "source.Attributes": {},
                        "destination.Attributes": {},
                        "transaction.Attributes": {}
                    }
                ]
                """;
       
        final List<Report> actual = ImportReportsPluginParser.parseReports(testReportsString);
        final Report actualReport = actual.get(0);
        
        assertEquals(actualReport.getSourceEntityType(), null);
        assertEquals(actualReport.getSourceEntityId(), null);
        assertEquals(actualReport.getDestinationEntityType(), null);
        assertEquals(actualReport.getDestinationEntityId(), null);
    }
    
}
