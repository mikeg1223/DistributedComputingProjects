
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.registry.*;

public class Client {
    public static void main(String[] args){
        try{
            //get and set the host
            String host = args[0];
            System.setProperty("java.rmi.server.hostname", host);
            int port = Integer.parseInt(args[1]);

            // connect to the stub for the server
            Registry registry = LocateRegistry.getRegistry(host, port);
            ServerDiscord stub = (ServerDiscord) registry.lookup("ServerDiscord");

            System.out.println("____________________________________________________");
            System.out.println("|         What is the name of this client          |");
            System.out.println("|__________________________________________________|\n\n");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String name = bufferedReader.readLine();
            try{    
                    //while NotBoundException is NOT being thrown salt the name, because name is in use
                    Random random = new Random();
                    while(true){
                        registry.lookup(name);
                        name += Integer.toString(random.nextInt(10));
                    }
            }catch(NotBoundException e){

            }
            catch(Exception e){
                System.out.println(e.getStackTrace());
            }

            // Start the local skeleton for the client
            ClientRemoteObject clientRemoteObject = new ClientRemoteObject(name, stub);
            ClientDiscord skeleton = (ClientDiscord) UnicastRemoteObject.exportObject(clientRemoteObject, 0);
            registry.bind(name, skeleton);

            // to clear the screen
            System.out.print("\033[H\033[2J");  
            System.out.flush();

            String serverName = stub.welcome(name);

            // to clear the screen
            System.out.print("\033[H\033[2J");  
            System.out.flush();

            System.out.println("____________________________________________________");
            System.out.println("|          Enter \"exit\" to terminate.            |");
            System.out.println("|__________________________________________________|\n\n");


            //continuously get input from the user until the exit condition is met 
            System.out.print(serverName + ": ");
            String nextLine = bufferedReader.readLine();
            while(nextLine.compareTo("exit") != 0){
                skeleton.sendMessage(nextLine);
                System.out.print(serverName + ": ");
                nextLine = bufferedReader.readLine();
            }

            // exit protocol
            stub.removeClient(name);
            UnicastRemoteObject.unexportObject(clientRemoteObject, false);    

        }catch(Exception e){
            System.err.println(e);
            System.err.println(e.getStackTrace());
        }


    }
}
