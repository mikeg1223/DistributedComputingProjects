import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;

public class Server extends ServerRemoteObject{
    public static void main(String[] args){
        try{
            // get and set host + port
            String host = args[0];
            System.setProperty("java.rmi.server.hostname", host);
            int port = Integer.parseInt(args[1]);

            //export the skeleton and create the registry. Then bind the skeleton to the registry. 
            ServerRemoteObject serverRemoteObject = new ServerRemoteObject(host, port);
            ServerDiscord skeleton = (ServerDiscord) UnicastRemoteObject.exportObject(serverRemoteObject, port);
            Registry registry = LocateRegistry.createRegistry(port);
            registry.bind("ServerDiscord", skeleton);
        }catch(Exception e){
            System.err.println(e);
        }
    }
}