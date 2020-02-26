package src;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server{
    
    private static int port;
    private static Socket clientSocket = null;
    private static ServerSocket serverSocket = null;
    static ArrayList<String> fileNames = new ArrayList<String>();
    static ArrayList<String> permissions = new ArrayList<String>();
    static ArrayList<String> keys = new ArrayList<String>();
    public static void main (String [] args ) throws IOException {
        
        
//reading in 

    Scanner f  = new Scanner(new File("Files.txt"));
    while(f.hasNext()){
    





    }






        if(args.length < 1){
            System.out.println("Incorrect number of arguments!");
            System.exit(1);
            
        }
        else{
        port = Integer.parseInt(args[0]);
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("FileShare Server started at port " + port);

            File file = new File("server");
            file.mkdir();

        } catch (Exception e) {
            System.err.println("Port already in use.");
            System.exit(1);
        }
               
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                System.out.println("Accepted connection : " + clientSocket);

                Thread t = new Thread(new Connection(clientSocket));

                t.start();

            } catch (Exception e) {
                System.err.println("Error in connection attempt.");
                break;
            }
        }
        //TODO
        try{
            FileWriter writer = new FileWriter("Files.txt",true);
            for(int i=0;i<fileNames.size();i++){
                writer.write(fileNames.get(i)+",");
                writer.write(permissions.get(i)+",");
                writer.write(keys.get(i));


            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

        }
        serverSocket.close();
        System.out.println("FileShare Server stopped");
        System.exit(0);
    }

    }
