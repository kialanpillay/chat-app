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
                readFile();

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
                    writeFile();

                } catch (Exception e) {
                    System.err.println("Error in connection attempt.");
                    break;
                }
            }

        }
        serverSocket.close();
        System.out.println("FileShare Server stopped");
        System.exit(0);
    }


    public static void writeFile(){
        try{
            FileWriter writer = new FileWriter("server/meta.txt");
            for(int i=0;i<fileNames.size();i++){
                if(i<fileNames.size()-1){
                    writer.write(fileNames.get(i)+","+permissions.get(i)+","+keys.get(i)+"\n");
                }
                else{
                    writer.write(fileNames.get(i)+","+permissions.get(i)+","+keys.get(i));
                }
                
            }
            writer.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void readFile(){
        
        try{Scanner f  = new Scanner(new File("server/meta.txt"));

            while(f.hasNext()){
                String line = f.nextLine();
                int firstComma = line.indexOf(",");
                String fileName = line.substring(0,firstComma);
                String permission = line.substring(firstComma+1, firstComma+4);
                String key = line.substring(firstComma+5,line.length());
                fileNames.add(fileName);
                permissions.add(permission);
                keys.add(key);
            }

        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    }
