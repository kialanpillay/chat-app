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
            try {
                // socket = new Socket("localhost", port);
                InetAddress address = InetAddress.getByName(args[0]);
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
            if(!operation.equals("-l")){
                String fileName = args[3];
                switch (operation) {
                    case "-u":
                            sendMessage("CMD|1|" + socket.getInetAddress() + "|" + socket.getPort(),"INITIATE UPLOAD");
                            protocol.sendFile(new File(fileName));
                            break;
                    case "-d":
                            sendMessage("CMD|2|" + socket.getInetAddress() + "|" + socket.getPort(),"INITIATE DOWNLOAD");
                            sendMessage("DAT|2|" + socket.getInetAddress() + "|" + socket.getPort(),fileName);
                            protocol.receiveFile(fileName);
                            break;
                }
            }
            else{
                sendMessage("CMD|3|" + socket.getInetAddress() + "|" + socket.getPort(),"INITIATE QUERY");
                listFiles();
            }
            //Receive Termination Command Message from Server
            //String hTerminate = in.readLine();
            //String bTerminate = in.readLine();
            //if(hTerminate.contains("CMD|0") && bTerminate.contains("TERMINATE")){
            sendMessage("CMD|0|" + socket.getInetAddress() + "|" + socket.getPort(),"CONNECTION TERMINATED");
            socket.close();
            //}
            
    }
    }

    public static void sendMessage(String header, String body){
        Message m = new Message(header,body);
        os.println(m.getHeader());
        os.println(m.getBody());
        os.flush();
    }

    public static void listFiles() throws IOException {
        String hAcknowledgment = in.readLine();
        String bAcknowledgment = in.readLine();
        if(hAcknowledgment.contains("CTRL|3") && bAcknowledgment.contains("ACKNOWLEDGED")){
            System.out.println("Available Files");
            System.out.println("---------------");

            try {
                DataInputStream clientData = new DataInputStream(socket.getInputStream());
                long size = clientData.readLong();
                byte[] data = new byte[(int)size];
                clientData.readFully(data);
                String str= new String(data,"UTF-8");
                System.out.println(str);
                sendMessage("CTRL|3|" + socket.getInetAddress() + "|" + socket.getPort(),"QUERY RECEIVED");
                os.close();
                in.close();
            } catch (IOException e) {
                System.out.println(e.getStackTrace());
                //String hError = inStream.readLine();
                //String bError = inStream.readLine();
                //if(hError.contains("CTRL|3") && bError.contains("404")){
                //    sendMessage("CTRL|3|" + socket.getInetAddress() + "|" + socket.getPort(),"ERROR RECEIVED");
                //}
                System.err.println("Error retrieving files from Server!");
                os.close();
                in.close();
            }
            System.out.println("\n");
        }
       
    }

    
}