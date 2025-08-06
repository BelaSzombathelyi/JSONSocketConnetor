package com.jsonconnector;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * TCP Socket client for JSON communication
 */
public class SocketClient {
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ExecutorService executor;
    private volatile boolean connected = false;
    
    private final Consumer<String> messageHandler;
    private final BiConsumer<Boolean, String> statusHandler;
    
    public SocketClient(Consumer<String> messageHandler, BiConsumer<Boolean, String> statusHandler) {
        this.messageHandler = messageHandler;
        this.statusHandler = statusHandler;
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "SocketClient-Thread");
            t.setDaemon(true);
            return t;
        });
    }
    
    public void connect(String hostname, int port) {
        disconnect(); // Close any existing connection
        
        executor.submit(() -> {
            try {
                socket = new Socket(hostname, port);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                connected = true;
                
                statusHandler.accept(true, null);
                
                // Start listening for messages
                listenForMessages();
                
            } catch (IOException e) {
                connected = false;
                statusHandler.accept(false, "Connection failed: " + e.getMessage());
            }
        });
    }
    
    public void disconnect() {
        connected = false;
        
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignore close errors
        }
    }
    
    public void sendMessage(String message) {
        if (!connected || out == null) {
            statusHandler.accept(false, "Not connected to server");
            return;
        }
        
        executor.submit(() -> {
            try {
                out.println(message);
                if (out.checkError()) {
                    connected = false;
                    statusHandler.accept(false, "Connection lost");
                }
            } catch (Exception e) {
                connected = false;
                statusHandler.accept(false, "Send failed: " + e.getMessage());
            }
        });
    }
    
    private void listenForMessages() {
        executor.submit(() -> {
            try {
                String line;
                while (connected && (line = in.readLine()) != null) {
                    final String message = line;
                    messageHandler.accept(message);
                }
            } catch (IOException e) {
                if (connected) {
                    connected = false;
                    statusHandler.accept(false, "Connection lost: " + e.getMessage());
                }
            }
        });
    }
    
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}