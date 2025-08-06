# JSON Socket Connector

A JavaFX application for sending and receiving JSON messages via TCP socket communication.

## Features

- **Graphical User Interface**: Built with JavaFX for cross-platform compatibility
- **TCP Socket Communication**: Connect to any TCP server and exchange JSON messages
- **JSON Syntax Highlighting**: Both send and receive areas support JSON syntax highlighting using RSyntaxTextArea
- **JSON Validation**: Real-time validation ensures only valid JSON is sent
- **Connection Management**: Easy connection/reconnection with hostname and port controls
- **Command Management**: Special support for command-based protocols with favorites modal
- **Status Display**: Clear visual feedback on connection status and errors

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

## Building the Application

```bash
# Clone the repository
git clone <repository-url>
cd JSONSocketConnetor

# Compile the application
mvn clean compile

# Run tests
mvn test

# Package the application
mvn package
```

## Running the Application

### Using Maven Plugin (Recommended)

```bash
mvn javafx:run
```

### Using Java directly

```bash
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml,javafx.swing \
     -cp target/classes:target/dependency/* com.jsonconnector.JSONSocketConnectorApp
```

## Usage

### Basic Connection

1. **Hostname**: Enter the target server hostname (default: "localhost")
2. **Port**: Enter the target server port (default: 60200)
3. **Connect**: Click "Reconnect" to establish connection

### Sending Messages

1. **Compose JSON**: Enter valid JSON in the left text area
2. **Validate**: The "Send" button is enabled only when JSON is valid
3. **Send**: Click "Send" to transmit the message

### Receiving Messages

- Received messages appear in the right text area with syntax highlighting
- Messages are automatically formatted for readability

### Command Features

- **Auto-commands**: On connection, automatically sends `GetProcessId` and `GetCommands`
- **Favorites**: Click the "★" button to open command selection modal
- **Command Parameters**: Double-click commands to request parameter information

## Protocol Support

The application includes special handling for these commands:

- `GetProcessId`: Displays process information in status
- `GetCommands`: Populates the favorites command list
- `GetCommandParameters`: Shows parameter details for selected commands

## Testing

A test server is included for development and testing:

```bash
# Run the test server
java -cp target/test-classes:target/classes com.jsonconnector.TestServer
```

The test server:
- Listens on port 60200
- Responds to GetProcessId and GetCommands
- Echoes other JSON messages

## Architecture

- **JSONSocketConnectorApp**: Main JavaFX application class
- **SocketClient**: TCP socket communication handler
- **JsonValidator**: JSON validation utilities
- **JsonFormatter**: JSON formatting and parsing
- **CommandManager**: Command list management
- **FavoritesModal**: Command selection dialog

## Dependencies

- **JavaFX 17.0.2**: User interface framework
- **Gson 2.10.1**: JSON processing library
- **RSyntaxTextArea 3.3.3**: Syntax highlighting component
- **JUnit 5.9.2**: Testing framework

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is open source. Please refer to the license file for details.