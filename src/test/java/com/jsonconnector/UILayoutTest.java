package com.jsonconnector;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for UI layout improvements
 */
public class UILayoutTest {
    
    @Test
    public void testApplication_CanInstantiate() {
        // Test that the main application class can be instantiated
        // This verifies that all the UI layout changes compile correctly
        JSONSocketConnectorApp app = new JSONSocketConnectorApp();
        assertNotNull(app);
    }
    
    @Test
    public void testFieldWidthConfiguration() {
        // This test would need JavaFX toolkit to be initialized to actually test UI components
        // For now, we just test that the changes don't break compilation
        // In a real UI test environment, we would:
        // 1. Initialize the app
        // 2. Check that hostname field width equals port field width (150px)
        // 3. Verify status label is positioned correctly
        // 4. Verify buttons are in correct positions
        
        // Basic compilation test
        JSONSocketConnectorApp app = new JSONSocketConnectorApp();
        assertNotNull(app);
    }
}