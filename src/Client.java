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
    private static Protocol protocol;
    private static PrintStream os;

    public static void main(String[] args) throws IOException {
        try {
            socket = new Socket("localhost", 8080);
            in = new Scanner(System.in);
            protocol = new Protocol(socket);
        } catch (Exception e) {
            System.err.println("Cannot connect to the server, try again later.");
            System.exit(1);
        }

        os = new PrintStream(socket.getOutputStream());
        String option = "";

        while(!option.equals("Q")){
            System.out.println("FileShare Application");
            System.out.println("=====================");
            System.out.println("MENU");
            System.out.println("1) Upload File");
            System.out.println("2) Download File");
            System.out.println("3) List Stored Files");
            System.out.println("Q) Quit\n");
            //src/test.txt

            option = in.nextLine();
            if(!option.equals("Q")){

                switch (option) {
                    case "1":
                        os.println("1");
                        System.out.println("Enter file name: ");
                        fileName = in.nextLine();
                        protocol.sendFile(fileName);
                        break;
                    case "2":
                        os.println("2");
                        System.out.println("Enter file name: ");
                        fileName = in.nextLine();
                        protocol.receiveFile(fileName);
                        break;
                    case "3":
                        os.println("3");
                        protocol.listFiles();
                        break;
                    default:
                        System.out.println("Invalid Option");

                }
            }
        }
        socket.close();
    }

    
}