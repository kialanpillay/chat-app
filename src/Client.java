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

        if (args.length < 3) {
            System.out.println("Incorrect number of arguments!");
        } else {
            port = Integer.parseInt(args[1]);
            operation = args[2];
            InetAddress address = InetAddress.getByName(args[0]);
            try {
                socket = new Socket(address, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                os = new PrintStream(socket.getOutputStream());
                protocol = new Protocol(socket, in, os);
            } catch (Exception e) {
                System.err.println("Cannot connect to the server, try again later.");
                System.exit(1);
            }


            System.out.println("FileShare Application");
            System.out.println("=====================");
            System.out.println("Server IP Address: " + address);
            System.out.println("Server Port: " + port);
            if(!operation.equals("-l")){
                String fileName = args[3];
                String permission=args[4];
                switch (operation) {
                    case "-u":
                            System.out.println("Upload Requested: " + fileName);
                            System.out.println("=====================");
                            sendMessage("CMD|1|" + socket.getInetAddress() + "|" + socket.getPort(),"INITIATE UPLOAD");                       
                            protocol.sendFile(new File(fileName));
                            sendMessage("DAT|1|" + socket.getInetAddress() + "|" + socket.getPort(),permission);
                            if(args.length>5){
                                String key = args[5];
                                sendMessage("DAT|1|" + socket.getInetAddress() + "|" + socket.getPort(),key);
                            }
                            break;
                    case "-d":
                            System.out.println("Download Requested: " + fileName);
                            System.out.println("=====================");
                            sendMessage("CMD|2|" + socket.getInetAddress() + "|" + socket.getPort(),"INITIATE DOWNLOAD");
                            sendMessage("DAT|2|" + socket.getInetAddress() + "|" + socket.getPort(),fileName);
                            if(args.length>4){
                                String key = args[4];
                                sendMessage("DAT|2|" + socket.getInetAddress() + "|" + socket.getPort(),key);    
                            }
                            protocol.receiveFile(fileName);
                            break;
                }
            }
            else{
                System.out.println("Server Query Requested: ");
                System.out.println("=====================");
                sendMessage("CMD|3|" + socket.getInetAddress() + "|" + socket.getPort(),"INITIATE QUERY");
                protocol.listFiles();
            }
            createMessage("CMD|0|" + socket.getInetAddress() + "|" + socket.getPort(),"CONNECTION TERMINATED");
            socket.close();

            
    }
    }

    public static void sendMessage(String header, String body){
        Message m = new Message(header,body);
        os.println(m.getHeader());
        os.println(m.getBody());
        os.flush();
    }

    public static void createMessage(String header, String body){
        Message m = new Message(header,body);
    }


    
}