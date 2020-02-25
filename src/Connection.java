package src;

import java.net.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import sun.net.www.content.audio.basic;

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
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));//input stream
            ps = new PrintStream(clientSocket.getOutputStream());//send messages to client

            String headerRequest = in.readLine(); //Retrieve operation code from header
            String operation = headerRequest.substring(4,5);//Retrieve operation code from header
            String bodyRequest = in.readLine();
            assert(bodyRequest.contains("INITIATE")); //Check if body of message contains initiate operation.
            
            


                switch (operation) {
                    case "1":
                        sendMessage("CTRL|1|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"UPLOAD OPERATION ACKNOWLEDGED");
                        receiveFile();

                        String hPermission=in.readLine();
                        String bPermission = in.readLine();
                        Server.permissions.add(bPermission);
                    
                        createMessage("CTRL|1|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"FILE PERMISSION RECEIVED");
                        if(bPermission.equalsIgnoreCase("Key")){
                            String hKey = in.readLine();
                            String bKey = in.readLine();
                            Server.keys.add(bKey);

                            createMessage("CTRL|1|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"FILE KEY RECEIVED ");
                        }else{
                            Server.keys.add("0");

                        }
                        
                         
                        sendMessage("CMD|0|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"TERMINATE CONNECTION");
                        break;
                    case "2":
                        createMessage("CTRL|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"DOWNLOAD OPERATION ACKNOWLEDGED");
                        String fileName = "";
                        String hFile = in.readLine();
                        if(hFile.contains("DAT|2")){
                            fileName = in.readLine();
                            createMessage("CTRL|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"FILE NAME RECEIVED");
                            String filePermission = checkPermission(fileName);
                            
                            if(filePermission.equalsIgnoreCase("KEY")){
                                String hKey = in.readLine();
                                String bKey = in.readLine();
                                if(verifyKey(bKey, fileName)){
                                   sendFile(fileName);
                                   break;

                                }else{
                                    createMessage("CTRL|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"FILE KEY INCORRECT");
                                    sendMessage("CMD|0|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"TERMINATE CONNECTION");
                                break;

                                }
                                


                            }
                            else if(filePermission.equalsIgnoreCase("VIS")){
                                createMessage("CTRL|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"FILE CAN'T BE DOWNLOADED");
                                sendMessage("CMD|0|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"TERMINATE CONNECTION");
                                break;
                            }
                            else{
                                sendFile(fileName);
                                sendMessage("CMD|0|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"TERMINATE CONNECTION");
                                break;

                            }
                            

                           

                        }
                        
                        
                    case "3":
                        createMessage("CTRL|3|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"QUERY OPERATION ACKNOWLEDGED");
                        listFiles();
                        sendMessage("CMD|0|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"TERMINATE CONNECTION");
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

    public void receiveFile() throws IOException { //upload-saving key and permission above try
        try {
            int bytesRead;

            DataInputStream clientData = new DataInputStream(clientSocket.getInputStream());

            String fileName = clientData.readUTF();
            Server.fileNames.add(fileName);
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
            sendMessage("CMD|1|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"ERROR RECEIVED");
            System.err.println("Client error. Connection closed at port " + clientSocket.getPort());
            
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
            





                createMessage("CTRL|1|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"DOWNLOAD OPERATION COMPLETE");
                System.out.println("File sent to client at port " + clientSocket.getPort());
            }
            dis.close();
        } catch (Exception e) {
            sendMessage("CTRL|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"404 NOT FOUND");
            System.err.println("404 NOT FOUND");

        } 
    }

    public void listFiles() throws IOException {
        try {
                String list = "";

                File folder = new File("server");
                File[]fileList = folder.listFiles();
                for (File file: fileList){
                    if(!file.getName().startsWith(".")){
                        Date d = new Date(file.lastModified());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
                        String l = String.format("%-20s%-15s%-25s%-15s", file.getName(), file.length() + " B",sdf.format(d),"Public");
                        list+=l + " " + "\n";
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
                    createMessage("CTRL|1|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"QUERY OPERATION COMPLETE");
                    System.out.println("List of files sent to client at port " + clientSocket.getPort());
                }
                
        

        } catch (Exception e) {
            sendMessage("CTRL|3|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"404");
            System.err.println("Error retrieving files!");
        } 
    }

    public void sendMessage(String header, String body){
        Message m = new Message(header,body);
        ps.println(m.getHeader());
        ps.println(m.getBody());
        ps.flush();
    }

    public void createMessage(String header, String body){
        Message m = new Message(header,body);
    }

    private int getFileIndex(String filename){
       String FileName = filename;
       int fileIndex=0;
       for(int i =0; i<Server.fileNames.size();i++) {
        if(Server.fileNames.get(i).equals(FileName)){
           fileIndex = i;
        }
       
       }
       return fileIndex;

    }

    private String checkPermission(String filename){
       String FileName = filename;
       int fileIndex = getFileIndex(FileName);
       String permission = Server.permissions.get(fileIndex);
       return permission;


    }
    private boolean verifyKey(String key,String filename){
    String clientKey = key;
    String FileName = filename;
    int keyIndex = getFileIndex(FileName);
    String fileKey = Server.keys.get(keyIndex);
    if(clientKey.equals(fileKey)){
        return true;

    }else{
        return false;
    }


    }
}