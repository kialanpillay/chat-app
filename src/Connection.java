package src;

import java.net.*;
import java.util.logging.Logger;


import java.util.logging.Level;
import java.io.*;

public class Connection implements Runnable {

    private Socket clientSocket;
    private BufferedReader in = null;
    private PrintStream ps;

    public Connection(Socket client) {
        this.clientSocket = client;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ps = new PrintStream(clientSocket.getOutputStream());

            String headerRequest = in.readLine(); //Retrieve operation code from header
            String operation = headerRequest.substring(4,5);//Retrieve operation code from header
            String bodyRequest = in.readLine();
            assert(bodyRequest.contains("INITIATE")); //Check if body of message contains initiate operation.


                switch (operation) {
                    case "1":
                        sendMessage("CTRL|1|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"UPLOAD OPERATION ACKNOWLEDGED");
                        receiveFile();
                        sendMessage("CMD|0|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"TERMINATE CONNECTION");
                        break;
                    case "2":
                        //sendMessage("CTRL|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"DOWNLOAD OPERATION ACKNOWLEDGED");
                        String fileName = "";
                        String hFile = in.readLine();
                        if(hFile.contains("DAT|2")){
                            fileName = in.readLine();

                        }
                        //sendMessage("CTRL|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"FILE NAME RECEIVED");
                        sendFile(fileName);
                        //sendMessage("CMD|0|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"TERMINATE CONNECTION");
                        break;
                    case "3":
                        //sendMessage("CTRL|3|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"QUERY OPERATION ACKNOWLEDGED");
                        listFiles();
                        //sendMessage("CMD|0|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"TERMINATE CONNECTION");
                        break;
                    default:
                        in.close();
                        ps.close();
                        clientSocket.close();
                        break;
                }
            
            in.close();
            ps.close();
            clientSocket.close();

        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void receiveFile() throws IOException {
        try {
            int bytesRead;

            DataInputStream clientData = new DataInputStream(clientSocket.getInputStream());

            String fileName = clientData.readUTF();
            OutputStream output = new FileOutputStream("server/"+fileName);
            long size = clientData.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }

            sendMessage("CTRL|1|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"UPLOAD RECEIVED");
            System.out.println("File " + fileName + " received from client.");

            clientData.close();
            output.close();

            
        } catch (IOException ex) {
            String hError = in.readLine();
            String bError = in.readLine();
            if(hError.contains("CTRL|1") && bError.contains("ERROR")){
                sendMessage("CMD|1|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"ERROR RECEIVED");
                System.err.println("Client error. Connection closed at port " + clientSocket.getPort());
            }
            in.readLine();
            in.readLine(); //Get Client Error Acknowledgment;
        }
    }

    public void sendFile(String fileName) throws IOException {
        try {

            File file = new File("server/"+fileName);
            byte[] dataBytes = new byte[(int) file.length()];

            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(dataBytes, 0, dataBytes.length);
            OutputStream os = clientSocket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF("DAT|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort());
            dos.writeUTF(file.getName());
            dos.writeLong(dataBytes.length);
            dos.write(dataBytes, 0, dataBytes.length);
            dos.flush();

            //Get receipt message from client
            String hResponse = in.readLine();
            String bResponse = in.readLine();
            if(hResponse.contains("CTRL|2") && bResponse.contains("DOWNLOAD RECEIVED")){
                System.out.println("File sent to client at port " + clientSocket.getPort());
            }
            dis.close();
        } catch (Exception e) {
            System.out.println(e);
            sendMessage("CTRL|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"404 NOT FOUND");
            System.err.println("404 NOT FOUND");
            in.readLine();
            in.readLine(); //Get Client Error Acknowledgment;

        } 
    }

    public void listFiles() throws IOException {
        try {
                String list = "";

                File folder = new File("server");
                File[]fileList = folder.listFiles();
                for (File file: fileList){
                    if(!file.getName().startsWith(".")){
                        list+=file.getName() + "\n";
                    }
                    
                }
                byte[] dataBytes = list.getBytes("UTF-8");
                OutputStream os = clientSocket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);
                dos.writeLong(dataBytes.length);
                dos.write(dataBytes, 0, dataBytes.length);
                dos.flush();
                
                String hResponse = in.readLine();
                String bResponse = in.readLine();
                if(hResponse.contains("CTRL|3") && bResponse.contains("QUERY RECEIVED")){
                    System.out.println("List of files sent to client at port " + clientSocket.getPort());
                }
                
        

        } catch (Exception e) {
            sendMessage("CTRL|3|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"404");
            System.err.println("Error retrieving files!");
            in.readLine();
            in.readLine(); //Get Client Error Acknowledgment;
        } 
    }

    public void sendMessage(String header, String body){
        Message m = new Message(header,body);
        ps.println(m.getHeader());
        ps.println(m.getBody());
        ps.flush();
    }
}