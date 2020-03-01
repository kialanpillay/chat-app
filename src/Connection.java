package src;

import java.net.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;



import java.util.logging.Level;
import java.io.*;

public class Connection implements Runnable {

    private Socket clientSocket;
    private BufferedReader in = null;
    private PrintStream ps;

    /**Constructor for Connection class
     * 
     * @param client The clients socket
     */
    public Connection(Socket client) {
        this.clientSocket = client; 
    }
/** Spawns new thread for each client socket
 * 
 */
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
                    case "1": //Upload
                        sendMessage("CTRL|1|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"UPLOAD OPERATION ACKNOWLEDGED");
                        

                        String hPermission=in.readLine(); //Receive permissions message from client
                        assert(hPermission.contains("DAT|1")); //Assert that a permission value is being received
                        String bPermission = in.readLine();
                        String hKey, bKey = "";
             
                        createMessage("CTRL|1|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"FILE PERMISSION RECEIVED");
                        if(bPermission.equalsIgnoreCase("Key")){ //If a privat upload is requested
                            hKey = in.readLine();
                            assert(hKey.contains("DAT|1"));
                            bKey = in.readLine();
                            createMessage("CTRL|1|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"FILE KEY RECEIVED ");
                        }
                        //Adds metadata to relevant data members in the Server class
                        String filename = receiveFile();
                        if(Server.fileNames.contains(filename)){
                            int index = getFileIndex(filename);
                            Server.permissions.set(index,bPermission);
                            if(bPermission.equalsIgnoreCase("Key")){
                                Server.keys.set(index,bKey);
                            }
                            else{
                                Server.keys.set(index,"0");
                            }

                        }
                        else if(!filename.equals("")){
                            Server.fileNames.add(filename);
                            Server.permissions.add(bPermission);
                            if(bPermission.equalsIgnoreCase("Key")){
                                Server.keys.add(bKey);
                            }
                            else{
                                Server.keys.add("0");
                            }
                        }

                        Server.writeFile();

                        sendMessage("CMD|1|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"TERMINATE CONNECTION");
                        break;

                    case "2": //Download
                        createMessage("CTRL|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"DOWNLOAD OPERATION ACKNOWLEDGED");
                        String fileName = "";
                        String hFile = in.readLine();
                        if(hFile.contains("DAT|2")){
                            fileName = in.readLine();
                            createMessage("CTRL|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"FILE NAME RECEIVED");

                            if(!Server.fileNames.contains(fileName)){ //If file does not exist
                                sendFile(fileName, ""); //No access specified
                                sendMessage("CMD|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"TERMINATE CONNECTION");
                                break;
                            }
                            else{

                                String filePermission = checkPermission(fileName); //Get permission from metadata store
                                
                                if(filePermission.equalsIgnoreCase("KEY")){
                                    hKey = in.readLine();
                                    assert(hKey.contains("DAT|2"));
                                    bKey = in.readLine();

                                    if(verifyKey(bKey, fileName)){
                                        sendMessage("CTRL|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"VALID KEY");
                                        sendFile(fileName, "ACCESS");
                                        sendMessage("CMD|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"TERMINATE CONNECTION");
                                        break;
                                    }
                                    else{
                                        sendMessage("CTRL|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"INVALID KEY");
                                        System.err.println("Access Violation: " + clientSocket);
                                        sendMessage("CMD|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"TERMINATE CONNECTION");
                                    break;
                                    }
            
                                }
                                else if(filePermission.equalsIgnoreCase("VIS")){ //If file is not downloadable but visible
                                    sendFile(fileName, "DENIED");
                                    System.err.println("Access Violation: " + clientSocket);
                                    sendMessage("CMD|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"TERMINATE CONNECTION");
                                    break;
                                }
                                else {
                                    sendFile(fileName, "ACCESS");
                                    sendMessage("CMD|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"TERMINATE CONNECTION");
                                    break;

                                }
                            }
                        }
                        
                    case "3":
                        createMessage("CTRL|3|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"QUERY OPERATION ACKNOWLEDGED");
                        listFiles();
                        createMessage("CMD|3|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"TERMINATE CONNECTION");
                        break;

                    case "4":
                        createMessage("CTRL|4|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"FILE QUERY OPERATION ACKNOWLEDGED");
                        String query = "";
                        String hQuery= in.readLine();
                        if(hQuery.contains("DAT|4")){
                            query = in.readLine(); //Retrieve query from message body
                        }
                        queryFile(query);
                        createMessage("CMD|4|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"TERMINATE CONNECTION");
                        break;

                    default: //Close all resources
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
/**Receiving an upload from the client
 * 
 * @return  A string representing the file name being uploaded.
 * @throws IOException File not uploaded (received)
 */
    public String receiveFile() throws IOException {
        try {
            int bytesRead;

            DataInputStream clientData = new DataInputStream(clientSocket.getInputStream());

            String fileName = clientData.readUTF();
            OutputStream output = new FileOutputStream("server/"+fileName); //Write file to server location
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
            
            return fileName;
            
        } catch (IOException ex) {
            sendMessage("CMD|1|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"ERROR RECEIVED");
            System.err.println("Client error. Connection closed at port " + clientSocket.getPort());
            return "";
        }
        
    }
/** Sending a file to the client for a download request
 * 
 * @param fileName Name of file being sent for a client download
 * @param access Status of the files permission property
 * @throws IOException File could not be sent
 */
    public void sendFile(String fileName, String access) throws IOException {
        try {
            if(access.equals("DENIED")){
                OutputStream os = clientSocket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);
                dos.writeUTF("CTRL|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort());
                dos.writeUTF("ACCESS DENIED"); //Send ACCESS DENIED message to client.
                dos.flush();
            }
            else{
                File file = new File("server/"+fileName);
                byte[] dataBytes = new byte[(int) file.length()];

                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                DataInputStream dis = new DataInputStream(bis);
                dis.readFully(dataBytes, 0, dataBytes.length); //Read file into InputStream
                OutputStream os = clientSocket.getOutputStream(); //Get OutputStream of client for writing
                DataOutputStream dos = new DataOutputStream(os);
                dos.writeUTF("DAT|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort()); //Message Header
                dos.writeUTF(file.getName());
                dos.writeLong(dataBytes.length);
                dos.write(dataBytes, 0, dataBytes.length); //Data Message Body
                dos.flush();

                //Get receipt message from client
                String hResponse = in.readLine();
                String bResponse = in.readLine();
                if(hResponse.contains("CTRL|2") && bResponse.contains("DOWNLOAD RECEIVED")){
                
                    createMessage("CTRL|1|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"DOWNLOAD OPERATION COMPLETE");
                    System.out.println("File " + fileName + " sent to client at port " + clientSocket.getPort());
                }
                dis.close();
            }
            
        } catch (Exception e) { //Error handling
            sendMessage("CTRL|2|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"404 NOT FOUND");
            System.err.println("404 Error");

        } 
    }
/**Lists all files on the server that have public/visible permissions
 * 
 * @throws IOException Files could not be retrieved.
 */
    public void listFiles() throws IOException {
        try {
                String list = "";

                File folder = new File("server");
                File[]fileList = folder.listFiles();
                for (File file: fileList){
                    if(!file.getName().startsWith(".") && !file.getName().equals("meta.txt") && !checkPermission(file.getName()).equals("KEY")){
                        //If file is not hidden and not private
                        Date d = new Date(file.lastModified());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //Convert last modfied epoch seconds to a date
                        String permission = checkPermission(file.getName());
                        if(permission.equals("PUB")){
                            permission = "Public";
                        } 
                        else{
                            permission = "Visible";
                        }
                        String l = String.format("%-20s%-15s%-25s%-15s", file.getName(), file.length() + " B",sdf.format(d), permission);
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
                    createMessage("CTRL|3|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"QUERY OPERATION COMPLETE");
                    System.out.println("List of files sent to client at port " + clientSocket.getPort());
                }
                
        

        } catch (Exception e) {
            sendMessage("CTRL|3|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"404");
            System.err.println("Error retrieving files!");
        } 
    }
    /** Getting all information on a specific file.
     * 
     * @param fileName file that is being queried.
     * @throws IOException File information could not be accessed.
     */
    public void queryFile(String fileName) throws IOException {
        try {
                String details = "";

                File folder = new File("server");
                File[]fileList = folder.listFiles();
                boolean found = false;
                for (File file: fileList){
                    if(file.getName().equals(fileName)){
                        Date d = new Date(file.lastModified());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //Convert last modfied epoch seconds to a date
                        String permission = checkPermission(file.getName());
                        if(permission.equals("PUB")){
                            permission = "Public";
                        } 
                        else if(permission.equals("VIS")){
                            permission = "Visible";
                        }
                        else{
                            permission = "Private";
                        }
                        details = String.format("%-20s%-15s%-25s%-15s", file.getName(), file.length() + " B",sdf.format(d), permission);
                        found = true;
                    }
                    
                }
                if(!found){
                    details = String.format("%-20s%-15s%-25s%-15s", "404 Not Found", "0 B","-", "-");
                }
                
                byte[] dataBytes = details.getBytes("UTF-8");
                OutputStream os = clientSocket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);
                dos.writeLong(dataBytes.length);
                dos.write(dataBytes, 0, dataBytes.length);
                dos.flush();
                
                String hResponse = in.readLine();
                String bResponse = in.readLine();
                if(hResponse.contains("CTRL|4") && bResponse.contains("FILE QUERY RECEIVED")){
                    createMessage("CTRL|4|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"QUERY OPERATION COMPLETE");
                    System.out.println("File details sent to client at port " + clientSocket.getPort());
                }
                
        

        } catch (Exception e) {
            sendMessage("CTRL|4|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"404");
            System.err.println("Error accessing file information!");
        } 
    }
    
    /** Message to be sent to the printstream.
     * 
     * @param header consists of format MESSAGETYPE|OPERATION(number)|RECIPIENT| PORT  
     * @param body consists of OPERATION(text)/data
     */
    public void sendMessage(String header, String body){
        Message m = new Message(header,body);
        ps.println(m.getHeader());
        ps.println(m.getBody());
        ps.flush();
    }

    /**Creating a message
     * 
     * @param header consists of format MESSAGETYPE|OPERATION(number)|RECIPIENT| PORT 
     * @param body consists of OPERATION(text)/data
     */
    public void createMessage(String header, String body){
        Message m = new Message(header,body);
    }
/**Checking for the index of the file specified in an arrayList.
 * 
 * @param filename Name of file whose index has to be checked.
 * @return An interger of the files specified index.
 */
    public int getFileIndex(String filename){
       String FileName = filename;
       int fileIndex=0;
       for(int i =0; i<Server.fileNames.size();i++) {
            if(Server.fileNames.get(i).equals(FileName)){
                fileIndex = i;
            }
       
       }
       return fileIndex;

    }
/**Checking permission of specified file
 * 
 * @param filename Name of file whose permission has to be checked.
 * @return A string of the files permission (VIS,PUB,KEY)
 */
    public String checkPermission(String filename){
       String FileName = filename;
       int fileIndex = getFileIndex(FileName);
       String permission = Server.permissions.get(fileIndex);
       return permission;


    }
    /** Checking if the clients key matches the files key to access file
     * 
     * @param key Secret Key from client.
     * @param filename Name of file to be accessed by key.
     * @return Boolean value if the key is correct/incorrect.
     */
    public boolean verifyKey(String key,String filename){
        String clientKey = key;
        String FileName = filename;
        int keyIndex = getFileIndex(FileName);
        String fileKey = Server.keys.get(keyIndex);
        if(clientKey.equals(fileKey)){
            return true;
        }
        else{
            return false;
        }


    }
}