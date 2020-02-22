package src;

import java.io.*;
import java.net.*;

public class Server{
    
    private final static int PORT = 8080;
    private static Socket clientSocket = null;
    private static ServerSocket serverSocket = null;
    public static void main (String [] args ) throws IOException {
      
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("FileShare Server started");
        } catch (Exception e) {
            System.err.println("Port already in use.");
            System.exit(1);
        }

        while (true) {
            try {
                clientSocket = serverSocket.accept();
                System.out.println("Accepted connection : " + clientSocket);

                Thread t = new Thread(new Connection(clientSocket));

                t.start();

            } catch (Exception e) {
                System.err.println("Error in connection attempt.");
            }
        }
    }
}