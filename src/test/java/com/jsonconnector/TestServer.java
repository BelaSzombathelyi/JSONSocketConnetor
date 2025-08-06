package com.jsonconnector;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple TCP server for testing the JSON Socket Connector
 */
public class TestServer {
    
    private ServerSocket serverSocket;
    private ExecutorService executor;
    private volatile boolean running = false;
    
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        executor = Executors.newCachedThreadPool();
        running = true;
        
        System.out.println("Test server started on port " + port);
        
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> handleClient(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client: " + e.getMessage());
                }
            }
        }
    }
    
    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());
            
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);
                
                // Handle specific commands
                if (inputLine.contains("GetProcessId")) {
                    out.println("{\"succeeded\": true, \"result\": {\"processId\": 11480}}");
                } else if (inputLine.contains("GetCommands")) {
                    out.println("{\"succeeded\": true, \"result\": {\"commands\": [\"API.CloneProjectMapItemToViewMap\", \"API.GetProjectMapItems\", \"ACUserInterface.GetTransparentNotifications\", \"Utility.GetArchicadLocation\"]}}");
                } else if (inputLine.contains("GetCommandParameters")) {
                    out.println("{\"succeeded\": true, \"result\": {\"parameters\": [{\"name\": \"sourceMapName\", \"type\": \"string\"}, {\"name\": \"targetMapName\", \"type\": \"string\"}]}}");
                } else {
                    // Echo back the message
                    out.println("{\"received\": \"" + inputLine.replace("\"", "\\\"") + "\"}");
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Ignore
            }
            System.out.println("Client disconnected");
        }
    }
    
    public void stop() throws IOException {
        running = false;
        if (serverSocket != null) {
            serverSocket.close();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }
    
    public static void main(String[] args) {
        TestServer server = new TestServer();
        try {
            server.start(60200);
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}