package com.jsonconnector;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Modal dialog for selecting commands from favorites
 */
public class FavoritesModal {
    
    private final List<String> allCommands;
    private final Consumer<String> commandSelector;
    private Stage stage;
    private ListView<String> domainList;
    private ListView<String> commandList;
    private TextField filterField;
    
    public FavoritesModal(List<String> commands, Consumer<String> commandSelector) {
        this.allCommands = commands;
        this.commandSelector = commandSelector;
        createModal();
    }
    
    private void createModal() {
        stage = new Stage();
        stage.setTitle("Select Command");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setWidth(600);
        stage.setHeight(400);
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        
        // Filter section
        HBox filterBox = createFilterSection();
        
        // Main content with two lists
        HBox mainContent = createMainContent();
        
        // Buttons
        HBox buttonBox = createButtonSection();
        
        root.getChildren().addAll(filterBox, mainContent, buttonBox);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        
        // Initialize data
        populateDomainList();
    }
    
    private HBox createFilterSection() {
        HBox filterBox = new HBox(10);
        filterBox.getChildren().addAll(
            new Label("Filter:"),
            filterField = new TextField()
        );
        
        Button clearButton = new Button("✕");
        clearButton.setOnAction(e -> filterField.clear());
        
        filterBox.getChildren().add(clearButton);
        HBox.setHgrow(filterField, Priority.ALWAYS);
        
        // Add filter listener
        filterField.textProperty().addListener((obs, oldText, newText) -> updateCommandList());
        
        return filterBox;
    }
    
    private HBox createMainContent() {
        HBox mainContent = new HBox(10);
        
        // Domain list (left side)
        VBox leftPanel = new VBox(5);
        leftPanel.getChildren().addAll(
            new Label("Domains:"),
            domainList = new ListView<>()
        );
        
        // Command list (right side)
        VBox rightPanel = new VBox(5);
        rightPanel.getChildren().addAll(
            new Label("Commands:"),
            commandList = new ListView<>()
        );
        
        // Setup list selection handlers
        domainList.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> updateCommandList()
        );
        
        commandList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedCommand = commandList.getSelectionModel().getSelectedItem();
                if (selectedCommand != null) {
                    selectCommand(selectedCommand);
                }
            }
        });
        
        mainContent.getChildren().addAll(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        
        return mainContent;
    }
    
    private HBox createButtonSection() {
        HBox buttonBox = new HBox(10);
        
        Button selectButton = new Button("Select");
        selectButton.setOnAction(e -> {
            String selectedCommand = commandList.getSelectionModel().getSelectedItem();
            if (selectedCommand != null) {
                selectCommand(selectedCommand);
            }
        });
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> stage.close());
        
        buttonBox.getChildren().addAll(selectButton, cancelButton);
        
        return buttonBox;
    }
    
    private void populateDomainList() {
        Set<String> domains = allCommands.stream()
                .map(this::extractDomain)
                .collect(Collectors.toSet());
        
        domainList.getItems().clear();
        domainList.getItems().add("All");
        domainList.getItems().addAll(domains.stream().sorted().collect(Collectors.toList()));
        
        // Select "All" by default
        domainList.getSelectionModel().selectFirst();
    }
    
    private void updateCommandList() {
        String selectedDomain = domainList.getSelectionModel().getSelectedItem();
        String filter = filterField.getText();
        
        List<String> filteredCommands = allCommands.stream()
                .filter(cmd -> selectedDomain == null || "All".equals(selectedDomain) || 
                         extractDomain(cmd).equals(selectedDomain))
                .filter(cmd -> filter == null || filter.trim().isEmpty() || 
                         cmd.toLowerCase().contains(filter.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
        
        commandList.getItems().clear();
        commandList.getItems().addAll(filteredCommands);
    }
    
    private String extractDomain(String command) {
        int dotIndex = command.indexOf('.');
        return dotIndex > 0 ? command.substring(0, dotIndex) : "Other";
    }
    
    private void selectCommand(String command) {
        commandSelector.accept(command);
        stage.close();
    }
    
    public void show() {
        stage.show();
    }
}