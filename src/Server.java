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
    try{Scanner f  = new Scanner(new File("Files.txt"));

        while(f.hasNext()){
            String line = f.nextLine();
            int firstComma = line.indexOf(",");
            String fileName = line.substring(0,firstComma);
            String permission = line.substring(firstComma+1, firstComma+4);
            String key = line.substring(firstComma+5);
            fileNames.add(fileName);
            permissions.add(permission);
            keys.add(key);
        }

    }
    catch(IOException e){
        e.printStackTrace();
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
                    writeFile();

                } catch (Exception e) {
                    System.err.println("Error in connection attempt.");
                    break;
                }
            }
            //TODO


        

        }
        serverSocket.close();
        System.out.println("FileShare Server stopped");
        System.exit(0);
    }


    public static void writeFile(){
        try{
            FileWriter writer = new FileWriter("File.txt");
            writer.write("hello");
            for(int i=0;i<fileNames.size();i++){
                writer.write(fileNames.get(i)+","+permissions.get(i)+","+keys.get(i));
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    }
