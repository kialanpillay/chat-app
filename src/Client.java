package src;

import java.io.*;
import java.net.*;

public class Client {

    private static Socket socket;
    private static int port;
    private static String operation;
    private static Protocol protocol;
    private static PrintStream os;

    public static void main(String[] args) throws IOException {
        port = Integer.parseInt(args[1]);
        operation = args[2];
        try {
            //socket = new Socket("localhost", port);
            InetAddress address = InetAddress.getByName(args[0]);
            socket = new Socket("localhost", port);
            protocol = new Protocol(socket);
        } catch (Exception e) {
            System.err.println("Cannot connect to the server, try again later.");
            System.exit(1);
        }

        os = new PrintStream(socket.getOutputStream());


        System.out.println("FileShare Application");
        System.out.println("=====================");

        if(!operation.equals("-l")){
            String fileName = args[3];
            switch (operation) {
                case "-u":
                        os.println("1");
                        protocol.sendFile(new File(fileName));
                        break;
                case "-d":
                        os.println("2");
                        os.println(fileName);
                        protocol.receiveFile(fileName);
                        break;
            }
        }
        else{
            os.println("3");
            protocol.listFiles();
        }
        socket.close();
    }

    
}