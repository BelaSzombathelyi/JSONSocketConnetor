package com.jsonconnector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for reconnection functionality
 */
public class ReconnectTest {
    
    private TestServer testServer;
    private SocketClient socketClient;
    private AtomicBoolean connected;
    private AtomicInteger connectionCount;
    private String lastError;
    
    @BeforeEach
    void setUp() {
        testServer = new TestServer();
        connected = new AtomicBoolean(false);
        connectionCount = new AtomicInteger(0);
        lastError = null;
        
        socketClient = new SocketClient(
            message -> {
                // Handle received messages
            },
            (isConnected, error) -> {
                connected.set(isConnected);
                if (isConnected) {
                    connectionCount.incrementAndGet();
                }
                lastError = error;
            }
        );
    }
    
    @AfterEach
    void tearDown() throws IOException {
        if (socketClient != null) {
            socketClient.disconnect();
        }
        if (testServer != null) {
            testServer.stop();
        }
    }
    
    @Test
    void testReconnectFunctionality() throws Exception {
        // Start test server
        Thread serverThread = new Thread(() -> {
            try {
                testServer.start(60201); // Use different port to avoid conflicts
            } catch (IOException e) {
                fail("Failed to start test server: " + e.getMessage());
            }
        });
        serverThread.start();
        
        // Wait for server to start
        Thread.sleep(500);
        
        // First connection
        CountDownLatch firstConnectionLatch = new CountDownLatch(1);
        socketClient.connect("localhost", 60201);
        
        // Wait for connection to complete
        Thread.sleep(1000);
        assertTrue(socketClient.isConnected(), "Should be connected after first connection");
        
        // Simulate reconnection - this should disconnect and reconnect
        CountDownLatch reconnectionLatch = new CountDownLatch(1);
        socketClient.connect("localhost", 60201);
        
        // Wait for reconnection to complete
        Thread.sleep(1000);
        assertTrue(socketClient.isConnected(), "Should be connected after reconnection");
        
        // Test with invalid port to ensure error handling
        socketClient.connect("localhost", 99999);
        Thread.sleep(1000); // Wait for connection attempt to fail
        assertFalse(socketClient.isConnected(), "Should not be connected to invalid port");
    }
    
    @Test
    void testDisconnectBeforeReconnect() throws Exception {
        // Start test server
        Thread serverThread = new Thread(() -> {
            try {
                testServer.start(60202); // Use different port
            } catch (IOException e) {
                fail("Failed to start test server: " + e.getMessage());
            }
        });
        serverThread.start();
        
        // Wait for server to start
        Thread.sleep(500);
        
        // Connect
        socketClient.connect("localhost", 60202);
        Thread.sleep(1000); // Wait for connection
        assertTrue(socketClient.isConnected(), "Socket should report as connected");
        
        // Disconnect manually
        socketClient.disconnect();
        assertFalse(socketClient.isConnected(), "Socket should report as disconnected after manual disconnect");
        
        // Reconnect should work
        socketClient.connect("localhost", 60202);
        Thread.sleep(1000); // Wait for reconnection
        assertTrue(socketClient.isConnected(), "Socket should report as connected after reconnection");
    }
}