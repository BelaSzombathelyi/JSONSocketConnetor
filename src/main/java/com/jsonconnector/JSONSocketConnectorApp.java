package com.jsonconnector;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.embed.swing.SwingNode;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Map;

/**
 * JSON Socket Connector - A JavaFX application for sending and receiving JSON messages via TCP socket
 */
public class JSONSocketConnectorApp extends Application {
    
    private TextField hostnameField;
    private TextField portField;
    private Button reconnectButton;
    private Button sendButton;
    private Button favoritesButton;
    private Label statusLabel;
    private RSyntaxTextArea sendTextArea;
    private RSyntaxTextArea receiveTextArea;
    
    private SocketClient socketClient;
    private CommandManager commandManager;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JSON Socket Connector");
        
        // Initialize components
        initializeComponents();
        
        // Create layout
        VBox root = createMainLayout();
        
        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);
        
        // Initialize socket client and command manager
        socketClient = new SocketClient(this::onMessageReceived, this::onConnectionStatusChanged);
        commandManager = new CommandManager();
        
        // Auto-connect on startup
        Platform.runLater(this::connectToServer);
        
        primaryStage.show();
    }
    
    private void initializeComponents() {
        // Connection controls
        hostnameField = new TextField("localhost");
        hostnameField.setPrefWidth(150);
        
        portField = new TextField("60200");
        portField.setPrefWidth(80);
        portField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*")) {
                portField.setText(oldText);
            }
        });
        
        reconnectButton = new Button("Reconnect");
        reconnectButton.setOnAction(e -> connectToServer());
        
        // Action buttons
        sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendMessage());
        sendButton.setDisable(true);
        
        favoritesButton = new Button("★");
        favoritesButton.setOnAction(e -> showFavoritesModal());
        favoritesButton.setDisable(true);
        
        // Status label
        statusLabel = new Label("Disconnected");
        statusLabel.setTextFill(Color.RED);
        
        // Text areas with JSON syntax highlighting
        initializeTextAreas();
    }
    
    private void initializeTextAreas() {
        sendTextArea = new RSyntaxTextArea(20, 60);
        sendTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        sendTextArea.setCodeFoldingEnabled(true);
        sendTextArea.setAntiAliasingEnabled(true);
        sendTextArea.setText("{\n  \"command\": \"GetProcessId\"\n}");
        
        // Listen for text changes to enable/disable send button
        sendTextArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateJson(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateJson(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateJson(); }
        });
        
        receiveTextArea = new RSyntaxTextArea(20, 60);
        receiveTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        receiveTextArea.setCodeFoldingEnabled(true);
        receiveTextArea.setAntiAliasingEnabled(true);
        receiveTextArea.setEditable(false);
    }
    
    private VBox createMainLayout() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        
        // Top panel with connection controls
        HBox topPanel = createTopPanel();
        
        // Middle panel with text areas
        HBox middlePanel = createMiddlePanel();
        
        // Bottom panel with status
        HBox bottomPanel = createBottomPanel();
        
        root.getChildren().addAll(topPanel, middlePanel, bottomPanel);
        VBox.setVgrow(middlePanel, Priority.ALWAYS);
        
        return root;
    }
    
    private HBox createTopPanel() {
        HBox topPanel = new HBox(10);
        topPanel.setAlignment(Pos.CENTER_LEFT);
        
        topPanel.getChildren().addAll(
            new Label("Hostname:"), hostnameField,
            new Label("Port:"), portField,
            reconnectButton
        );
        
        return topPanel;
    }
    
    private HBox createMiddlePanel() {
        HBox middlePanel = new HBox(10);
        
        // Left side - Send panel
        VBox leftPanel = createSendPanel();
        
        // Right side - Receive panel
        VBox rightPanel = createReceivePanel();
        
        middlePanel.getChildren().addAll(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        
        return middlePanel;
    }
    
    private VBox createSendPanel() {
        VBox sendPanel = new VBox(5);
        
        HBox sendHeader = new HBox(10);
        sendHeader.setAlignment(Pos.CENTER_LEFT);
        sendHeader.getChildren().addAll(new Label("Send JSON:"), favoritesButton, sendButton);
        
        SwingNode sendSwingNode = new SwingNode();
        SwingUtilities.invokeLater(() -> {
            RTextScrollPane sendScrollPane = new RTextScrollPane(sendTextArea);
            sendSwingNode.setContent(sendScrollPane);
        });
        
        sendPanel.getChildren().addAll(sendHeader, sendSwingNode);
        VBox.setVgrow(sendSwingNode, Priority.ALWAYS);
        
        return sendPanel;
    }
    
    private VBox createReceivePanel() {
        VBox receivePanel = new VBox(5);
        
        receivePanel.getChildren().addAll(new Label("Received JSON:"));
        
        SwingNode receiveSwingNode = new SwingNode();
        SwingUtilities.invokeLater(() -> {
            RTextScrollPane receiveScrollPane = new RTextScrollPane(receiveTextArea);
            receiveSwingNode.setContent(receiveScrollPane);
        });
        
        receivePanel.getChildren().add(receiveSwingNode);
        VBox.setVgrow(receiveSwingNode, Priority.ALWAYS);
        
        return receivePanel;
    }
    
    private HBox createBottomPanel() {
        HBox bottomPanel = new HBox();
        bottomPanel.setAlignment(Pos.CENTER_LEFT);
        bottomPanel.getChildren().add(new Label("Status: "));
        bottomPanel.getChildren().add(statusLabel);
        
        return bottomPanel;
    }
    
    private void connectToServer() {
        String hostname = hostnameField.getText().trim();
        String portText = portField.getText().trim();
        
        if (hostname.isEmpty() || portText.isEmpty()) {
            showError("Hostname and port are required");
            return;
        }
        
        try {
            int port = Integer.parseInt(portText);
            socketClient.connect(hostname, port);
        } catch (NumberFormatException e) {
            showError("Invalid port number");
        }
    }
    
    private void sendMessage() {
        String json = sendTextArea.getText().trim();
        if (json.isEmpty()) {
            showError("Please enter a JSON message");
            return;
        }
        
        if (!JsonValidator.isValidJson(json)) {
            showError("Invalid JSON format");
            return;
        }
        
        socketClient.sendMessage(json);
    }
    
    private void validateJson() {
        Platform.runLater(() -> {
            String json = sendTextArea.getText().trim();
            boolean isValid = !json.isEmpty() && JsonValidator.isValidJson(json);
            sendButton.setDisable(!isValid || !socketClient.isConnected());
        });
    }
    
    private void showFavoritesModal() {
        List<String> commands = commandManager.getCommands();
        if (commands.isEmpty()) {
            showError("No commands available. Please connect to server first.");
            return;
        }
        
        FavoritesModal modal = new FavoritesModal(commands, this::onCommandSelected);
        modal.show();
    }
    
    private void onCommandSelected(String command) {
        // Request command parameters
        String json = String.format("{\"command\": \"GetCommandParameters\", \"parameters\": {\"command\": \"%s\"}}", command);
        socketClient.sendMessage(json);
    }
    
    private void onMessageReceived(String message) {
        Platform.runLater(() -> {
            String formattedJson = JsonFormatter.format(message);
            receiveTextArea.setText(formattedJson);
            
            // Handle special responses
            handleSpecialResponses(message);
        });
    }
    
    private void handleSpecialResponses(String message) {
        try {
            Map<String, Object> response = JsonFormatter.parseToMap(message);
            
            if (response.containsKey("result")) {
                Map<String, Object> result = (Map<String, Object>) response.get("result");
                
                // Handle GetProcessId response
                if (result.containsKey("processId")) {
                    double processId = (Double) result.get("processId");
                    Platform.runLater(() -> {
                        statusLabel.setText(String.format("Connected to Archicad.exe (pid: %.0f)", processId));
                        statusLabel.setTextFill(Color.GREEN);
                    });
                }
                
                // Handle GetCommands response
                if (result.containsKey("commands")) {
                    List<String> commands = (List<String>) result.get("commands");
                    commandManager.setCommands(commands);
                    Platform.runLater(() -> favoritesButton.setDisable(false));
                }
                
                // Handle GetCommandParameters response
                if (result.containsKey("parameters")) {
                    String command = extractCommandFromParameters(message);
                    if (command != null) {
                        Platform.runLater(() -> {
                            sendTextArea.setText(JsonFormatter.format(message));
                        });
                    }
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors for non-special responses
        }
    }
    
    private String extractCommandFromParameters(String message) {
        // This would need to extract the original command from the response
        // For now, return null to indicate no command extraction
        return null;
    }
    
    private void onConnectionStatusChanged(boolean connected, String error) {
        Platform.runLater(() -> {
            if (connected) {
                statusLabel.setText("Connected");
                statusLabel.setTextFill(Color.GREEN);
                sendButton.setDisable(!JsonValidator.isValidJson(sendTextArea.getText()));
                
                // Auto-send GetProcessId and GetCommands
                socketClient.sendMessage("{\"command\": \"GetProcessId\"}");
                socketClient.sendMessage("{\"command\": \"GetCommands\"}");
            } else {
                statusLabel.setText(error != null ? error : "Disconnected");
                statusLabel.setTextFill(Color.RED);
                sendButton.setDisable(true);
                favoritesButton.setDisable(true);
                commandManager.clearCommands();
            }
        });
    }
    
    private void showError(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.setTextFill(Color.RED);
        });
    }
    
    @Override
    public void stop() {
        if (socketClient != null) {
            socketClient.disconnect();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}