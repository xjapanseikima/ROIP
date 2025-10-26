package com.commutronics;

import javax.sound.sampled.*;
import java.io.*;
import java.net.*;

public class VoiceClient {
    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 5555;

        Socket socket = new Socket(host, port);
        OutputStream out = socket.getOutputStream();

        AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, false);
        TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
        microphone.open(format);
        microphone.start();

        byte[] buffer = new byte[4096];
        System.out.println("🎙️ Streaming audio to server...");

        while (true) {
            int bytesRead = microphone.read(buffer, 0, buffer.length);
            out.write(buffer, 0, bytesRead);
        }
    }
}