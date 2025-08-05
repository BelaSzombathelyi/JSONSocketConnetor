const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const net = require('net');
const path = require('path');

class JSONSocketConnectorServer {
    constructor() {
        this.app = express();
        this.server = http.createServer(this.app);
        this.io = socketIo(this.server);
        this.tcpConnections = new Map(); // Map to store TCP connections per socket
        
        this.setupMiddleware();
        this.setupRoutes();
        this.setupSocketHandlers();
    }

    setupMiddleware() {
        // Serve static files
        this.app.use(express.static(path.join(__dirname)));
        this.app.use(express.json());
    }

    setupRoutes() {
        // Serve the main page
        this.app.get('/', (req, res) => {
            res.sendFile(path.join(__dirname, 'index.html'));
        });

        // Health check endpoint
        this.app.get('/health', (req, res) => {
            res.json({ 
                status: 'ok', 
                timestamp: new Date().toISOString(),
                connections: this.tcpConnections.size
            });
        });
    }

    setupSocketHandlers() {
        this.io.on('connection', (socket) => {
            console.log(`Client connected: ${socket.id}`);

            // Handle TCP connection request
            socket.on('connect-tcp', (data) => {
                this.connectToTCP(socket, data);
            });

            // Handle message sending to TCP server
            socket.on('send-tcp-message', (data) => {
                this.sendTCPMessage(socket, data);
            });

            // Handle client disconnect
            socket.on('disconnect', () => {
                console.log(`Client disconnected: ${socket.id}`);
                this.closeTCPConnection(socket);
            });

            // Handle errors
            socket.on('error', (error) => {
                console.error(`Socket error for ${socket.id}:`, error);
                this.closeTCPConnection(socket);
            });
        });
    }

    connectToTCP(socket, { host, port }) {
        console.log(`Attempting TCP connection to ${host}:${port} for client ${socket.id}`);
        
        // Close existing connection if any
        this.closeTCPConnection(socket);

        const tcpSocket = new net.Socket();
        this.tcpConnections.set(socket.id, tcpSocket);

        // Set timeout for connection (60 seconds for demo purposes)
        tcpSocket.setTimeout(60000);

        tcpSocket.connect(port, host, () => {
            console.log(`TCP connected to ${host}:${port} for client ${socket.id}`);
            socket.emit('tcp-connected', { host, port });
        });

        tcpSocket.on('data', (data) => {
            const message = data.toString().trim();
            console.log(`Received from TCP ${host}:${port}:`, message);
            
            try {
                // Try to parse as JSON for validation
                JSON.parse(message);
                socket.emit('tcp-message', { message });
            } catch (error) {
                // If not valid JSON, still send it but mark as raw data
                socket.emit('tcp-message', { 
                    message, 
                    type: 'raw',
                    note: 'Not valid JSON' 
                });
            }
        });

        tcpSocket.on('close', () => {
            console.log(`TCP connection closed for client ${socket.id}`);
            socket.emit('tcp-disconnected', { reason: 'Connection closed by server' });
            this.tcpConnections.delete(socket.id);
        });

        tcpSocket.on('error', (error) => {
            console.error(`TCP error for client ${socket.id}:`, error.message);
            socket.emit('tcp-error', { 
                message: error.message,
                code: error.code 
            });
            this.tcpConnections.delete(socket.id);
        });

        tcpSocket.on('timeout', () => {
            console.log(`TCP connection timeout for client ${socket.id}`);
            tcpSocket.destroy();
            socket.emit('tcp-error', { 
                message: 'Connection timeout',
                code: 'TIMEOUT' 
            });
            this.tcpConnections.delete(socket.id);
        });
    }

    sendTCPMessage(socket, { message }) {
        const tcpSocket = this.tcpConnections.get(socket.id);
        
        if (!tcpSocket || tcpSocket.destroyed) {
            socket.emit('tcp-error', { 
                message: 'No active TCP connection',
                code: 'NO_CONNECTION' 
            });
            return;
        }

        try {
            // Validate JSON before sending
            JSON.parse(message);
            
            // Send the message (add newline if not present)
            const messageToSend = message.endsWith('\n') ? message : message + '\n';
            tcpSocket.write(messageToSend, 'utf8');
            
            console.log(`Sent to TCP for client ${socket.id}:`, message);
            
        } catch (error) {
            socket.emit('tcp-error', { 
                message: `Invalid JSON: ${error.message}`,
                code: 'INVALID_JSON' 
            });
        }
    }

    closeTCPConnection(socket) {
        const tcpSocket = this.tcpConnections.get(socket.id);
        if (tcpSocket && !tcpSocket.destroyed) {
            tcpSocket.destroy();
            this.tcpConnections.delete(socket.id);
            console.log(`Closed TCP connection for client ${socket.id}`);
        }
    }

    start(port = 3000) {
        this.server.listen(port, () => {
            console.log(`JSON Socket Connector Server running on port ${port}`);
            console.log(`Open http://localhost:${port} in your browser`);
            console.log(`Active TCP connections: ${this.tcpConnections.size}`);
        });

        // Graceful shutdown
        process.on('SIGINT', () => {
            console.log('\nShutting down gracefully...');
            
            // Close all TCP connections
            for (const [socketId, tcpSocket] of this.tcpConnections) {
                if (!tcpSocket.destroyed) {
                    tcpSocket.destroy();
                }
            }
            this.tcpConnections.clear();
            
            this.server.close(() => {
                console.log('Server closed');
                process.exit(0);
            });
        });
    }
}

// Demo TCP Server (for testing purposes)
class DemoTCPServer {
    constructor(port = 60200) {
        this.port = port;
        this.server = null;
        this.clients = new Set();
    }

    start() {
        this.server = net.createServer((socket) => {
            console.log(`Demo TCP client connected from ${socket.remoteAddress}:${socket.remotePort}`);
            this.clients.add(socket);

            // Send welcome message
            const welcomeMsg = {
                type: 'welcome',
                message: 'Connected to Demo TCP Server',
                timestamp: new Date().toISOString()
            };
            socket.write(JSON.stringify(welcomeMsg) + '\n');

            socket.on('data', (data) => {
                const message = data.toString().trim();
                console.log(`Demo TCP received:`, message);

                try {
                    const parsed = JSON.parse(message);
                    
                    // Echo back with timestamp
                    const response = {
                        type: 'echo',
                        original: parsed,
                        echo_timestamp: new Date().toISOString(),
                        server: 'Demo TCP Server'
                    };
                    
                    socket.write(JSON.stringify(response) + '\n');
                    
                } catch (error) {
                    // Send error response for invalid JSON
                    const errorResponse = {
                        type: 'error',
                        message: 'Invalid JSON received',
                        error: error.message,
                        received: message,
                        timestamp: new Date().toISOString()
                    };
                    socket.write(JSON.stringify(errorResponse) + '\n');
                }
            });

            socket.on('close', () => {
                console.log(`Demo TCP client disconnected`);
                this.clients.delete(socket);
            });

            socket.on('error', (error) => {
                console.error(`Demo TCP client error:`, error.message);
                this.clients.delete(socket);
            });
        });

        this.server.listen(this.port, () => {
            console.log(`Demo TCP Server listening on port ${this.port}`);
        });

        // Send periodic heartbeat messages
        setInterval(() => {
            if (this.clients.size > 0) {
                const heartbeat = {
                    type: 'heartbeat',
                    timestamp: new Date().toISOString(),
                    connected_clients: this.clients.size
                };
                
                for (const client of this.clients) {
                    if (!client.destroyed) {
                        client.write(JSON.stringify(heartbeat) + '\n');
                    }
                }
            }
        }, 30000); // Every 30 seconds
    }

    stop() {
        if (this.server) {
            for (const client of this.clients) {
                client.destroy();
            }
            this.clients.clear();
            this.server.close();
        }
    }
}

// Start the servers
if (require.main === module) {
    const webServerPort = process.env.PORT || 3000;
    const tcpServerPort = process.env.TCP_PORT || 60200;

    // Start demo TCP server
    const demoServer = new DemoTCPServer(tcpServerPort);
    demoServer.start();

    // Start web server
    const webServer = new JSONSocketConnectorServer();
    webServer.start(webServerPort);
}

module.exports = { JSONSocketConnectorServer, DemoTCPServer };