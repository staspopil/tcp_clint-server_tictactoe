package com.company;

import java.io.*;
import java.net.Socket;

public class Connection {
    private final Socket socket;
    private final Thread th;
    private final eListener eListener;
    private final BufferedReader in;
    private final BufferedWriter out;

    public Connection(eListener listener,Socket socket) throws IOException {
        this.eListener = listener;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        th = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eListener.onConnectionReady(Connection.this);
                    while (!th.isInterrupted()) {
                        eListener.onReceiveString(Connection.this, in.readLine());
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    try {
                        eListener.onDisconnect(Connection.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        th.start();
    }

    public synchronized void sendMessage(String mes) throws IOException {
        out.write(mes+"\r\n");
        out.flush();
    }

    public synchronized void disconnect() throws IOException {
        th.interrupt();
        try {
            socket.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }



}

