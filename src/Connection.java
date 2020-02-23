package src;

import java.net.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;

public class Connection implements Runnable{
    
    private Socket clientSocket;
    private BufferedReader in = null;

    public Connection(Socket client) {
        this.clientSocket = client;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
            String operation = in.readLine();
                switch (operation) {
                    case "1":
                        receiveFile();
                        break;
                    case "2":
                        String outGoingFileName;
                        while ((outGoingFileName = in.readLine()) != null) {
                            sendFile(outGoingFileName);
                        }
                        break;
                    case "3":
                        listFiles();
                        break;
                    default:
                        break;
                }
            
            in.close();

        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void receiveFile() {
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
            clientData.close();
            output.close();
            System.out.println("File "+fileName+" received from client at port " + clientSocket.getPort());
        } catch (IOException ex) {
            System.err.println("Client error. Connection closed.");
        }
    }

    public void sendFile(String fileName) {
        try {

            File file = new File(fileName);
            byte[] dataBytes = new byte[(int)file.length()];

            FileInputStream fis = new FileInputStream("server/"+fileName);
            BufferedInputStream bis = new BufferedInputStream(fis);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(dataBytes, 0, dataBytes.length);

            OutputStream os = clientSocket.getOutputStream();

            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(file.getName());
            dos.writeLong(dataBytes.length);
            dos.write(dataBytes, 0, dataBytes.length);
            dos.flush();
            System.out.println("File "+fileName+" sent to client at port " + clientSocket.getPort());
            dis.close();
        } catch (Exception e) {
            System.err.println("404 Not Found");
        } 
    }

    public void listFiles() {
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
            System.out.println("List of stored files sent to client at port " + clientSocket.getPort());

        } catch (Exception e) {
            System.err.println("No files are stored on Server!");
        } 
    }
}