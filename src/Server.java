/***
 * This class is responsible for creating a Server that can accept a number of requests from different clients
 * Once the server is running, it listens for incoming connections.
 * On receipt of a client connection, it creates a new dedicated socket for communication with this particular client,
 * and starts a new thread to handle any client requests
 * @version 1.00
 */

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

/**Writing files from the  files array to a textfile
 * 
 */
    public static void writeFile(){
        try{
            FileWriter writer = new FileWriter("server/meta.txt",false);
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
/**
 * Reading files information from the textfile to populate the arrayLists.
 * @throws IOException File cant be found
 */
    public static void readFile() throws IOException {
        
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
