package com.excelian.mache.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class PortCheck {
    public static boolean portAccessible(String hostName, int port) {
        SocketAddress sockaddr = new InetSocketAddress(hostName, port);

        Socket socket = new Socket();
        boolean online = false;

        // Connect with 10 s timeout
        try {
            socket.connect(sockaddr, 10000);
            online = true;
        } catch (IOException ex) {
            // NOOP
        } finally {
            // As the close() operation can also throw an IOException
            // it must caught here
            try {
                socket.close();
            } catch (IOException ex) {
                // NOOP
            }
        }

        return online;
    }
}
