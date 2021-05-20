package com.company;

import java.io.IOException;

public interface eListener {
    void onConnectionReady(Connection connection) throws IOException;
    void onReceiveString(Connection connection, String string) throws IOException;
    void onDisconnect(Connection connection) throws IOException;
}
