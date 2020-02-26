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
            System.err.println("Incorrect number of arguments!");
            System.exit(1);
        } 
        else {
            port = Integer.parseInt(args[1]);
            operation = args[2];
            String permission = "";
            String key = "";

            if(operation.equals("-u")){
                if(args.length < 5){
                    System.err.println("Incorrect number of arguments!");
                    System.exit(1);
                }
                permission=args[4];
                if(permission.contains("public")){
                    permission = "PUB";
                }
                else if(permission.contains("visible")){
                    permission = "VIS";
                }
                else{
                    permission = "KEY";
                }

                if(args.length>5 && permission.equals("KEY")){
                    key = args[5];   
                }
                else if(args.length<=5 && permission.equals("KEY")) {
                    System.err.println("No private key specified!");
                    System.exit(1);
                }
            }
            else if(operation.equals("-d")){
                if(args.length < 4){
                    System.err.println("Incorrect number of arguments!");
                    System.exit(1);
                }
                if(args.length>4){
                    key = args[4];
                    sendMessage("DAT|2|" + socket.getInetAddress() + "|" + socket.getPort(),key);    
                } 
            }
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
                


                switch (operation) {
                    case "-u":
                            System.out.println("Upload Requested: " + fileName);
                            System.out.println("=====================");
                            sendMessage("CMD|1|" + socket.getInetAddress() + "|" + socket.getPort(),"INITIATE UPLOAD");  
                            sendMessage("DAT|1|" + socket.getInetAddress() + "|" + socket.getPort(),permission); 
                            if(permission.equals("KEY")){
                                sendMessage("DAT|1|" + socket.getInetAddress() + "|" + socket.getPort(),key);    
                            }     
                            protocol.sendFile(new File(fileName));
                            
                            break;
                    case "-d":
                            System.out.println("Download Requested: " + fileName);
                            System.out.println("=====================");
                            sendMessage("CMD|2|" + socket.getInetAddress() + "|" + socket.getPort(),"INITIATE DOWNLOAD");
                            sendMessage("DAT|2|" + socket.getInetAddress() + "|" + socket.getPort(),fileName);
                            if(permission.equals("KEY")){
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