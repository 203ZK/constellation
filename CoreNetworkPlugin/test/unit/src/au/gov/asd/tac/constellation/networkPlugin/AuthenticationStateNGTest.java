/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
public class AuthenticationStateNGTest {
    
    private AuthenticationState authState;
    
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
        authState = new AuthenticationState();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }
    
    @Test
    public void testConstructor() {
        assertFalse(authState.checkIfAuthenticated("anyUserId"));
        assertTrue(authState.getOptions().isEmpty());
    }
    
    @Test
    public void testSetState() {
        final String userId = "testUserId";
        final Map<String, String> options = new HashMap<>();
        options.put("field2", "value2");
        options.put("field1", "value1");
        
        authState.setState(userId, options);
        
        final List<String> expectedOptions = List.of("field1", "field2");
        
        assertTrue(authState.checkIfAuthenticated(userId));
        assertEquals(authState.getOptions(), expectedOptions);
    }
    
    @Test
    public void testClearState() {
        final String userId = "testUserId";
        final Map<String, String> options = new HashMap<>();
        options.put("field", "value");
        
        authState.setState(userId, options);
        authState.clearState();
        
        assertTrue(authState.checkIfAuthenticated(""));
        assertTrue(authState.getOptions().isEmpty());
    }
    
    @Test
    public void testGetReportId() {
        final String userId = "testUserId";
        final Map<String, String> options = new HashMap<>();
        options.put("field", "value");
        
        authState.setState(userId, options);
        
        assertEquals(authState.getReportId("field"), "value");
        assertEquals(authState.getReportId("notFound"), null);
    }
    
}
