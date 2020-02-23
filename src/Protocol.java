package src;

import java.net.*;
import java.io.*;
import java.util.logging.*;

public class Protocol {

    private Socket socket;

    public Protocol(Socket socket) {
        this.socket = socket;
    }

    public void sendFile(File file) {
        try {

            byte[] dataBytes = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(dataBytes, 0, dataBytes.length);

            OutputStream os = this.socket.getOutputStream();

            // Sending file name and file size to the server
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(file.getName());
            dos.writeLong(dataBytes.length);
            dos.write(dataBytes, 0, dataBytes.length);
            dos.flush();
            System.out.println("File " + file.getName() + " sent to Server.");
            dis.close();
        } catch (Exception e) {
            System.err.println("File does not exist!");
        }
    }

    public void receiveFile(String fileName) {
        try {
            int bytesRead = 0;
            DataInputStream clientData = new DataInputStream(socket.getInputStream());

            fileName = clientData.readUTF();
            OutputStream output = new FileOutputStream(("received_from_server_" + fileName));
            long size = clientData.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }

            output.close();

            System.out.println("File " + fileName + " received from Server.");
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void listFiles() {
        System.out.println("Available Files");
        System.out.println("---------------");
        try {
            DataInputStream clientData = new DataInputStream(socket.getInputStream());
            long size = clientData.readLong();
            byte[] data = new byte[(int)size];
            clientData.readFully(data);
            String str= new String(data,"UTF-8");
            System.out.println(str);
        } catch (IOException e) {
            System.err.print(e);
        }
        System.out.println("\n");
       
    }

}