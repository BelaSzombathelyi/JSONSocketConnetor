package com.jsonconnector;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Manages available commands from the server
 */
public class CommandManager {
    
    private List<String> commands = new ArrayList<>();
    
    public void setCommands(List<String> commands) {
        this.commands = new ArrayList<>(commands);
    }
    
    public List<String> getCommands() {
        return new ArrayList<>(commands);
    }
    
    public void clearCommands() {
        commands.clear();
    }
    
    public Map<String, List<String>> getGroupedCommands() {
        return commands.stream()
                .collect(Collectors.groupingBy(this::extractDomain));
    }
    
    private String extractDomain(String command) {
        int dotIndex = command.indexOf('.');
        return dotIndex > 0 ? command.substring(0, dotIndex) : "Other";
    }
    
    public List<String> filterCommands(String filter, String domain) {
        return commands.stream()
                .filter(cmd -> domain == null || extractDomain(cmd).equals(domain))
                .filter(cmd -> filter == null || filter.isEmpty() || 
                         cmd.toLowerCase().contains(filter.toLowerCase()))
                .collect(Collectors.toList());
    }
}