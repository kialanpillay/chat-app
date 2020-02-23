package src;

import java.net.*;
import java.io.*;

public class Protocol {

    private Socket socket;
    private BufferedReader inStream;
    private PrintStream ps;


    public Protocol(Socket socket, BufferedReader in, PrintStream ps) {
        this.socket = socket;
        this.inStream = in;
        this.ps = ps;
    }

    public void sendFile(File file) throws IOException {
        
        //String hAcknowledgment = inStream.readLine();
        //String bAcknowledgment = inStream.readLine();
        //if(hAcknowledgment.contains("CTRL|1") && bAcknowledgment.contains("ACKNOWLEDGED")){
     
            try {
                byte[] dataBytes = new byte[(int) file.length()];
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                DataInputStream dis = new DataInputStream(bis);
                dis.readFully(dataBytes, 0, dataBytes.length);
                OutputStream os = this.socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);
                dos.writeUTF(file.getName());
                dos.writeLong(dataBytes.length);
                dos.write(dataBytes, 0, dataBytes.length);
                dos.flush();
                
                //String hResponse = inStream.readLine(); //Store Header AND Message of Server Response
                //String bResponse = inStream.readLine();
                //if(hResponse.contains("CTRL|1") && bResponse.contains("UPLOAD RECEIVED")){
                    //sendMessage("CTRL|1|" + socket.getInetAddress() + "|" + socket.getPort(),"UPLOAD OPERATION COMPLETE");
                    System.out.println("File " + file.getName() + " sent to Server.");
                //}
                //else{
                //    System.err.println("Error uploading file!");
                //}

                dis.close();

            } catch (Exception e) {
                //sendMessage("CTRL|1|" + socket.getInetAddress() + "|" + socket.getPort(),"FATAL ERROR");
                System.err.println("Error uploading file!");
            }
        //}
    }

    public void receiveFile(String fileName) throws IOException {
        //String hAcknowledgment = inStream.readLine();
        //String bAcknowledgment = inStream.readLine();
        //if(hAcknowledgment.contains("CTRL|2") && bAcknowledgment.contains("ACKNOWLEDGED")){
            //String hRecevied = inStream.readLine();
            //String bRecevied = inStream.readLine();
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
                //sendMessage("CTRL|2|" + socket.getInetAddress() + "|" + socket.getPort(),"DOWNLOAD RECEIVIED");
                System.out.println("File " + fileName + " received from Server.");

                output.close();
                
            } catch (IOException ex) {
                //String hError = inStream.readLine();
                //String bError = inStream.readLine();
                System.out.println(ex);
                //if(hError.contains("CTRL|2") && bError.contains("404")){
                //    sendMessage("CTRL|2|" + socket.getInetAddress() + "|" + socket.getPort(),"ERROR RECEIVED");
                //   System.err.println("File does not exist on server!");
                //}
            }
         //}
    }

    public void listFiles() throws IOException {
        //String hAcknowledgment = inStream.readLine();
        //String bAcknowledgment = inStream.readLine();
        //if(hAcknowledgment.contains("CTRL|3") && bAcknowledgment.contains("ACKNOWLEDGED")){
            System.out.println("Available Files");
            System.out.println("---------------");

            try {
                DataInputStream clientData = new DataInputStream(socket.getInputStream());
                long size = clientData.readLong();
                byte[] data = new byte[(int)size];
                clientData.readFully(data);
                String str= new String(data,"UTF-8");
                System.out.println(str);
                //sendMessage("CTRL|3|" + socket.getInetAddress() + "|" + socket.getPort(),"QUERY RECEIVED");
            } catch (IOException e) {
                //String hError = inStream.readLine();
                //String bError = inStream.readLine();
                ////if(hError.contains("CTRL|3") && bError.contains("404")){
                //    sendMessage("CTRL|3|" + socket.getInetAddress() + "|" + socket.getPort(),"ERROR RECEIVED");
                //}
                System.out.println(e);
                System.err.println("Error retrieving files from Server!");
            }
            System.out.println("\n");
        //}
       
    }

    public void sendMessage(String header, String body){
        Message m = new Message(header,body);
        ps.println(m.getHeader());
        ps.println(m.getBody());
    }

}