/***
 * This class is responsible for creating a client that can communicate with a server at a specific IP address and port.
 * Interaction with the Client is via the Command Line, with the extraction of the relevant arguments occurring in the main method. 
 * The class has a number of private data members required for its functionality.
 * The class creates a socket with the specified address and port, and after obtaining the InputStream of the server, communicates by sending 
 * simple messages to the server. 
 * Three possible operations are possible on the client side: Upload, Download and Query a File List.
 * All errors are correctly handled, and the program gracefully exits after printing an appropriate message.
 * @version 1.00
 */

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

        if (args.length < 3 && !args[0].equals("--help")) {
            System.err.println("Incorrect number of arguments!"); //Error handling
            System.exit(1);
        }
        else if(args[0].equals("--help")){ //Help Menu
            System.out.println("Welcome to FileShare Help!");
            System.out.println("");
            System.out.println("General Command Format");
            System.out.println("java src/Client <IP Address> <Port Number> <-Flag> <File> <Permission> [Key]");
            System.out.println("");
            System.out.println("Upload Example");
            System.out.println("    java src/Client 127.0.0.1 8080 -u yourfile.txt (--public/--visible)");
            System.out.println("Private File Upload Example");
            System.out.println("    java src/Client 127.0.0.1 8080 -u yourfile.txt --private yoursecretkey");
            System.out.println("Download Example");
            System.out.println("    java src/Client 127.0.0.1 8080 -d yourfile.txt");
            System.out.println("Download Private File Example");
            System.out.println("    java src/Client 127.0.0.1 8080 -d yourfile.txt yoursecretkey");
            System.out.println("List All Files Example");
            System.out.println("    java src/Client 127.0.0.1 8080 -l");
            System.out.println("Query File Example");
            System.out.println("    java src/Client 127.0.0.1 8080 -l yourfile.txt");
            System.exit(0);
        }
        else {
            //Argument Extraction
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
                //Parsing the permission argument
                if(permission.contains("public")){
                    permission = "PUB";
                }
                else if(permission.contains("visible")){
                    permission = "VIS";
                }
                else{
                    permission = "KEY";
                }

                if(args.length > 5 && permission.equals("KEY")){
                    key = args[5];   
                }
                else if(args.length <= 5 && permission.equals("KEY")) {
                    System.err.println("No private key specified!");
                    System.exit(1);
                }
            }
            else if(operation.equals("-d")){
                if(args.length > 4){
                    key = args[4]; 
                }
            }
            InetAddress address = InetAddress.getByName(args[0]);
            try {
                socket = new Socket(address, port); //Creating socket
                in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //Retrieving server InputStream
                os = new PrintStream(socket.getOutputStream());
                protocol = new Protocol(socket, in, os);
            } catch (Exception e) {
                System.err.println("Cannot connect to the server, try again later.");
                System.exit(1);
            }

            //Client Requests
            System.out.println("FileShare Application");
            System.out.println("=====================");
            System.out.println("Server IP Address: " + address);
            System.out.println("Server Port: " + port);
            if(!operation.equals("-l")){
                String fileName = args[3]; //Retrieve filename
                switch (operation) {
                    case "-u":
                            System.out.println("Upload Requested: " + fileName);
                            System.out.println("=====================");
                            sendMessage("CMD|1|" + socket.getInetAddress() + "|" + socket.getPort(),"INITIATE UPLOAD");  
                            sendMessage("DAT|1|" + socket.getInetAddress() + "|" + socket.getPort(),permission); 
                            if(permission.equals("KEY")){
                                sendMessage("DAT|1|" + socket.getInetAddress() + "|" + socket.getPort(),key);    
                            }//Send message with shared secret key    
                            protocol.sendFile(new File(fileName));
                            
                            break;
                    case "-d":
                            System.out.println("Download Requested: " + fileName);
                            System.out.println("=====================");
                            sendMessage("CMD|2|" + socket.getInetAddress() + "|" + socket.getPort(),"INITIATE DOWNLOAD");
                            sendMessage("DAT|2|" + socket.getInetAddress() + "|" + socket.getPort(),fileName);
                            if(args.length > 4){
                                sendMessage("DAT|2|" + socket.getInetAddress() + "|" + socket.getPort(),key); 
                                protocol.receivePrivateFile(fileName);
                                //Method called if downloading a private file with a shared key     
                            }
                            else{
                                protocol.receiveFile(fileName); 
                            }
                            
                            break;
                }
            }
            else{
                if(args.length > 3){ //Query Operation for specific file
                    String fileName = args[3];
                    System.out.println("Server Query Requested: " + fileName);
                    System.out.println("=====================");
                    sendMessage("CMD|4|" + socket.getInetAddress() + "|" + socket.getPort(),"INITIATE FILE QUERY");
                    sendMessage("DAT|4|" + socket.getInetAddress() + "|" + socket.getPort(),fileName);
                    protocol.queryFile();
                }
                else{ //Standard Server Query
                    System.out.println("Server Query Requested: ");
                    System.out.println("=====================");
                    sendMessage("CMD|3|" + socket.getInetAddress() + "|" + socket.getPort(),"INITIATE QUERY");
                    protocol.listFiles();
                }
                
            }
            sendMessage("CMD|0|" + socket.getInetAddress() + "|" + socket.getPort(),"CONNECTION TERMINATED");
            socket.close();

            
    }
    }
/**Message to be sent to the printstream.
 * 
 * @param header consists of format MESSAGETYPE|OPERATION(number)|RECIPIENT| PORT 
 * @param body consists of OPERATION(text)/data
 */
    public static void sendMessage(String header, String body){
        Message m = new Message(header,body);
        os.println(m.getHeader());
        os.println(m.getBody());
        os.flush();
    }


    
}