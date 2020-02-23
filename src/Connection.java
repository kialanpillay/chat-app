package src;

import java.net.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;

public class Connection implements Runnable {

    private Socket clientSocket;
    private BufferedReader in = null;
    private static PrintStream ps;

    public Connection(Socket client) {
        this.clientSocket = client;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ps = new PrintStream(clientSocket.getOutputStream());
            String option = "";
            while ((option = in.readLine()) != "Q") {
                switch (option) {
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
            OutputStream output = new FileOutputStream(fileName);
            long size = clientData.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }
            clientData.close();
            output.close();
            System.out.println("File " + fileName + " received from client.");
            Server.fileNames.add(fileName);
        } catch (IOException ex) {
            Message errorAcknowledge = new Message("CTRL|1|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"ERROR ACKNOWLEDGED");
            ps.println(errorAcknowledge.getHeader());
            ps.println(errorAcknowledge.getBody());
            System.err.println("Client error. Connection closed.");
        }
    }

    public void sendFile(String fileName) {
        try {

            File file = new File(fileName);
            byte[] dataBytes = new byte[(int) file.length()];

            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(dataBytes, 0, dataBytes.length);

            OutputStream os = clientSocket.getOutputStream();

            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(file.getName());
            dos.writeLong(dataBytes.length);
            dos.write(dataBytes, 0, dataBytes.length);
            dos.flush();
            System.out.println("File " + fileName + " sent to client.");
            dis.close();
        } catch (Exception e) {
            Message notFound = new Message("CTRL|1|" + clientSocket.getInetAddress() + "|" + clientSocket.getPort(),"404 Not Found");
            ps.println(notFound.getHeader());
            ps.println(notFound.getBody());
            System.err.println("File does not exist!");

        } 
    }

    public void listFiles() {
        try {

            String list = "";
            for(int i = 0; i < Server.fileNames.size(); i++){
                list+=Server.fileNames.get(i)+"\n";
            }

            byte[] dataBytes = list.getBytes("UTF-8");
            OutputStream os = clientSocket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);

            dos.writeLong(dataBytes.length);
            dos.write(dataBytes, 0, dataBytes.length);
            dos.flush();
            System.out.println("List of files sent to client.");

        } catch (Exception e) {
            System.err.println("No files exist on server!");
        } 
    }
}