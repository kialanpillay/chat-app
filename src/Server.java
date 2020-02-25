package src;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server{
    
    private static int port;
    private static Socket clientSocket = null;
    private static ServerSocket serverSocket = null;
    public static ArrayList<String> fileNames = new ArrayList<>();
    public static ArrayList<String> permissions = new ArrayList<>();
    public static ArrayList<String> keys = new ArrayList<>();
    public static void main (String [] args ) throws IOException {
        if(args.length < 1){
            System.out.println("Incorrect number of arguments!");
            System.exit(1);
        }
        else{
        port = Integer.parseInt(args[0]);
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("FileShare Server started at port " + port);

            File file = new File("server");
            file.mkdir();
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
                break;
            }
        }
        //TODO
        serverSocket.close();
        System.out.println("FileShare Server stopped");
        System.exit(0);
    }

    }
}