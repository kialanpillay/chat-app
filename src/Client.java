package src;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private static Socket socket;
    private static String fileName;
    private static Scanner in;

    public static void main(String[] args) throws IOException {
        try {
            socket = new Socket("localhost", 8080);
            in = new Scanner(System.in);
        } catch (Exception e) {
            System.err.println("Cannot connect to the server, try again later.");
            System.exit(1);
        }

        //os = new PrintStream(socket.getOutputStream());
        String option = "";

        while(option !="Q"){
            System.out.println("FileShare Application");
            System.out.println("=====================");
            System.out.println("MENU");
            System.out.println("1) List Stored Files");
            System.out.println("2) Upload File");
            System.out.println("3) Download File");
            System.out.println("Q) Download File");

            option = in.nextLine();

            if(option!="Q"){

                switch (Integer.parseInt(option)) {
                    case 1:
                        sendFile();
                        break;
                    case 2:
                        System.err.print("Enter file name: ");
                        fileName = in.nextLine();
                        receiveFile(fileName);
                        break;
                    default:
                        System.out.println();

                }
            }
        }
        socket.close();
    }

    public static void sendFile() {
        try {
            System.err.print("Enter file name: ");
            fileName = in.nextLine();

            File myFile = new File(fileName);
            byte[] mybytearray = new byte[(int) myFile.length()];

            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            //bis.read(mybytearray, 0, mybytearray.length);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(mybytearray, 0, mybytearray.length);

            OutputStream os = socket.getOutputStream();

            //Sending file name and file size to the server
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(myFile.getName());
            dos.writeLong(mybytearray.length);
            dos.write(mybytearray, 0, mybytearray.length);
            dos.flush();
            System.out.println("File "+fileName+" sent to Server.");
        } catch (Exception e) {
            System.err.println("File does not exist!");
        }
    }

    public static void receiveFile(String fileName) {
        try {
            int bytesRead;
            InputStream in = socket.getInputStream();

            DataInputStream clientData = new DataInputStream(in);

            fileName = clientData.readUTF();
            OutputStream output = new FileOutputStream(("received_from_server_" + fileName));
            long size = clientData.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }

            output.close();
            in.close();

            System.out.println("File "+fileName+" received from Server.");
        } catch (IOException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}