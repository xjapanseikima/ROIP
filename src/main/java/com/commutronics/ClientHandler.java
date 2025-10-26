package com.commutronics;

import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final VoiceServer server;
    private final DataInputStream input;
    private final DataOutputStream output;
    private static final int BUFFER_SIZE = 4096;

    private SourceDataLine speakers;

    public ClientHandler(Socket socket, VoiceServer server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());

        // Initialize audio output (speaker)
        try {
            AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            speakers = (SourceDataLine) AudioSystem.getLine(info);
            speakers.open(format);
            speakers.start();
        } catch (LineUnavailableException e) {
            System.err.println("⚠️ Could not open audio output line on server.");
        }
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                // Play audio on the server speaker
                if (speakers != null) {
                    speakers.write(buffer, 0, bytesRead);
                }

                // Forward to other clients
                server.broadcast(buffer, this);
            }

        } catch (IOException e) {
            System.out.println("❌ Client disconnected: " + socket.getInetAddress());
        } finally {
            server.removeClient(this);
            try {
                if (speakers != null) {
                    speakers.drain();
                    speakers.close();
                }
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    public void sendAudio(byte[] audioData) {
        try {
            output.write(audioData);
            output.flush();
        } catch (IOException e) {
            System.out.println("⚠️ Failed to send audio to client.");
        }
    }
}