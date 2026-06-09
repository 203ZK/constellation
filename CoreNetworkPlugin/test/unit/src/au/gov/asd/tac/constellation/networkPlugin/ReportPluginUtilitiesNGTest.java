/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Intern-1003972
 */
public class ReportPluginUtilitiesNGTest {
    
    private final RecordStore record = new GraphRecordStore();
    
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
        record.reset();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }
    
    @Test
    public void testGetValueType_nullAttributeValue() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("ExtraField1", null);
        
        final Report testReport = new Report(
                "d000", "r12345", "Report #12345", 
                "123.123.123.123", "user_endpoint", "user_ABC", attributes, 
                "234.234.234.234", "server", "server_ABC", attributes, 
                attributes
        );
        
        ReportPluginUtilities.addReportToRecord(testReport, record);

        assertEquals(record.get("source.ExtraField1<string>"), null);
    }
    
    @Test
    public void testGetValueType_stringAttributeValue() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("ExtraField1", "extra_value");
        
        final Report testReport = new Report(
                "d000", "r12345", "Report #12345", 
                "123.123.123.123", "user_endpoint", "user_ABC", attributes, 
                "234.234.234.234", "server", "server_ABC", attributes, 
                attributes
        );
        
        ReportPluginUtilities.addReportToRecord(testReport, record);

        assertEquals(record.get("source.ExtraField1<string>"), "extra_value");
    }
    
    @Test
    public void testGetValueType_booleanAttributeValue() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("ExtraField1", false);
        
        final Report testReport = new Report(
                "d000", "r12345", "Report #12345", 
                "123.123.123.123", "user_endpoint", "user_ABC", attributes, 
                "234.234.234.234", "server", "server_ABC", attributes, 
                attributes
        );
        
        ReportPluginUtilities.addReportToRecord(testReport, record);

        assertEquals(record.get("source.ExtraField1<boolean>"), "false");
    }
    
    @Test
    public void testGetValueType_integerAttributeValue() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("ExtraField1", 1);
        
        final Report testReport = new Report(
                "d000", "r12345", "Report #12345", 
                "123.123.123.123", "user_endpoint", "user_ABC", attributes, 
                "234.234.234.234", "server", "server_ABC", attributes, 
                attributes
        );
        
        ReportPluginUtilities.addReportToRecord(testReport, record);

        assertEquals(record.get("source.ExtraField1<integer>"), "1");
    }
    
    @Test
    public void testGetValueType_doubleAttributeValue() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("ExtraField1", -1.0);
        
        final Report testReport = new Report(
                "d000", "r12345", "Report #12345", 
                "123.123.123.123", "user_endpoint", "user_ABC", attributes, 
                "234.234.234.234", "server", "server_ABC", attributes, 
                attributes
        );
        
        ReportPluginUtilities.addReportToRecord(testReport, record);

        assertEquals(record.get("source.ExtraField1<double>"), "-1.0");
    }
    
    @Test
    public void testGetValueType_datetimeAttributeValue() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("ExtraField1", "2026-06-08T16:41:00.123Z");
        
        final Report testReport = new Report(
                "d000", "r12345", "Report #12345", 
                "123.123.123.123", "user_endpoint", "user_ABC", attributes, 
                "234.234.234.234", "server", "server_ABC", attributes, 
                attributes
        );
        
        ReportPluginUtilities.addReportToRecord(testReport, record);

        assertEquals(record.get("source.ExtraField1<datetime>"), "2026-06-08T16:41:00.123Z");
    }
    
    @Test
    public void testAddReport_noOtherAttributes() {
        final Map<String, Object> attributes = new HashMap<>();
        
        final Report testReport = new Report(
                "d000", "r12345", "Report #12345", 
                "123.123.123.123", "user_endpoint", "user_ABC", attributes, 
                "234.234.234.234", "server", "server_ABC", attributes, 
                attributes
        );
        
        try {
            ReportPluginUtilities.addReportToRecord(testReport, record);
        } catch (Exception e) {
            Assert.fail("Expected no exception but got: " + e.getMessage());
        }
    }
    
    @Test
    public void testAddReport_normalReport() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("ExtraField1", "extra_value");
        attributes.put("ExtraField2", 100);
        attributes.put("ExtraField3", 5.0);
        attributes.put("ExtraField4", true);
        attributes.put("ExtraField5", "2026-06-08T16:41:00.123Z");
        
        final Report testReport = new Report(
                "d000", "r12345", "Report #12345", 
                "123.123.123.123", "user_endpoint", "user_ABC", attributes, 
                "234.234.234.234", "server", "server_ABC", attributes, 
                attributes
        );
        
        ReportPluginUtilities.addReportToRecord(testReport, record);
        
        assertEquals(record.get("source.Identifier"), "123.123.123.123");
        assertEquals(record.get("source.Type"), "user_endpoint");
        assertEquals(record.get("destination.Identifier"), "234.234.234.234");
        assertEquals(record.get("destination.Type"), "server");
        
        assertEquals(record.get("source.ExtraField1<string>"), "extra_value");
        assertEquals(record.get("source.ExtraField2<integer>"), "100");
        assertEquals(record.get("source.ExtraField3<double>"), "5.0");
        assertEquals(record.get("source.ExtraField4<boolean>"), "true");
        assertEquals(record.get("source.ExtraField5<datetime>"), "2026-06-08T16:41:00.123Z");
    }
    
}
