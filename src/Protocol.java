package src;

import java.net.*;
import java.io.*;
import java.util.logging.*;

public class Protocol {

    private Socket socket;

    public Protocol(Socket socket) {
        this.socket = socket;
    }

    public void sendFile(String fileName) {
        try {

            File file = new File(fileName);
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
            System.out.println("File " + fileName + " sent to Server.");
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
        BufferedReader reader;
        try {
            DataInputStream clientData = new DataInputStream(socket.getInputStream());
            System.out.println(clientData.readUTF());
            //reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //System.out.println(reader.readLine());
        } catch (IOException e) {
            System.err.print(e);
        }
        System.out.println("\n");
       
    }

}