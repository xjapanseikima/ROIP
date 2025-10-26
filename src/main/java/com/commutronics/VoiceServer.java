package com.commutronics;

import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class VoiceServer {

    private static final int PORT = 5555;

    // Keep track of connected clients
    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        new VoiceServer().startServer();
    }

    public void startServer() {
        System.out.println("🎤 Voice streaming server starting on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("✅ Server listening on " + serverSocket.getInetAddress() + ":" + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("🔌 New client connected: " + socket.getInetAddress());

                try {
                    ClientHandler client = new ClientHandler(socket, this);
                    clients.add(client);

                    Thread thread = new Thread(client);
                    thread.start();
                } catch (IOException e) {
                    System.err.println("⚠️ Failed to create client handler: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Server error: " + e.getMessage());
        }
    }

    // Send audio data to all clients except sender
    public void broadcast(byte[] audioData, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendAudio(audioData);
            }
        }
    }

    // Remove disconnected client
    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("🛑 Client removed. Connected clients: " + clients.size());
    }
}
