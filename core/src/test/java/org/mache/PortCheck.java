package org.mache;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

/**
 * Created on 11/08/2015.
 */
public class PortCheck
{
    public static boolean PortAccessible(String hostName, int port)
    {
        SocketAddress sockaddr = new InetSocketAddress(hostName, port);

        Socket socket = new Socket();
        boolean online = true;

        // Connect with 10 s timeout
        try {
            socket.connect(sockaddr, 10000);
        } catch (SocketTimeoutException stex) {
            // treating timeout errors separately from other io exceptions
            // may make sense
            online=false;
        } catch (IOException iOException) {
            online = false;
        } finally {
            // As the close() operation can also throw an IOException
            // it must caught here
            try {
                socket.close();
            } catch (IOException ex) {
                // feel free to do something moderately useful here, eg log the event
            }
        }

        return online;
    }
}
