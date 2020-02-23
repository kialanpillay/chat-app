package src;

import java.io.*;
import java.net.*;

public class Client {

    private static Socket socket;
    private static int port;
    private static String operation;
    private static Protocol protocol;
    private static PrintStream os;
    private static BufferedReader in = null;

    public static void main(String[] args) throws IOException {

        if(args.length < 3){
            System.out.println("Incorrect number of arguments!");
        }
        else{
            port = Integer.parseInt(args[1]);
            operation = args[2];
            try {
                //socket = new Socket("localhost", port);
                InetAddress address = InetAddress.getByName(args[0]);
                socket = new Socket(address, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                os = new PrintStream(socket.getOutputStream())
                protocol = new Protocol(socket, in, os);
            } catch (Exception e) {
                System.err.println("Cannot connect to the server, try again later.");
                System.exit(1);
            }

            ;


            System.out.println("FileShare Application");
            System.out.println("=====================");

            if(!operation.equals("-l")){
                String fileName = args[3];
                switch (operation) {
                    case "-u":
                    //Request r = new Request()
                            os.println("1"); //os.printlin(r.getHeader())
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

    
}