import java.util.HashMap;
import java.rmi.RemoteException;
import java.rmi.registry.*;

public class ServerRemoteObject implements ServerDiscord{

    // keep a map of references to client objects 
    HashMap<String, ClientDiscord> clients;
    // keep a map of names to nicknames
    HashMap<String,String> clientNameToName;
    // keep a reference to the main registry to look up clients
    Registry registry;

    //default constructor uses localhost and port 3000
    public ServerRemoteObject(){
        clients = new HashMap<String, ClientDiscord>();
        clientNameToName = new HashMap<String, String>();
        try {
            registry = LocateRegistry.getRegistry("localhost", 3000);
        } catch(Exception e){
            System.err.println(e);
        }
    }

    public ServerRemoteObject(String host, int port){
        clients = new HashMap<String, ClientDiscord>();
        clientNameToName = new HashMap<String, String>();
        try {
            registry = LocateRegistry.getRegistry(host, port);
        } catch(Exception e){
            System.err.println(e);
        }
    }


    public void broadCastToAll(String message){
        try{
            // use a lambda function over a hashmap to send a message to each client
            clients.forEach((key, value)->{
                try{
                    clients.get(key).receiveMessage(message);
                }catch (RemoteException e){
                    System.err.println(e);
                }
            });
        }catch(Exception e){
            System.err.println(e);
        }
    }

    // add the client to the hashmaps, get their nickname, let everyone know
    public String welcome(String clientName){
        String nickname = "";
        try{
            clients.put(clientName, (ClientDiscord) registry.lookup(clientName));
            nickname = clients.get(clientName).getName();
            clientNameToName.put(clientName, nickname);
            broadCastToAll(nickname + " has joined the server.");
        } catch(Exception e){
            System.err.println(e);
        }
        return nickname;
    }

    // send to everyone but the sender
    public void clientToAll(String message, String clientName){
        try{
            // use a lambda function over a hashmap to send a message to each client who isnt the sender
            clients.forEach((key, value)->{
                try{
                    if(key.compareTo(clientName) != 0){
                        clients.get(key).receiveMessage(clientNameToName.get(clientName) + ": " + message);
                    }
                }catch (RemoteException e){
                    System.err.println(e);
                }
            });
        }catch(Exception e){
            System.err.println(e);
        }
    }

    //removes the client from the hashmaps, the registry, and lets everyone know they are leaving
    public void removeClient(String clientName){
        try{
            clients.remove(clientName);
            registry.unbind(clientName);
            broadCastToAll(clientNameToName.get(clientName) + " has left the server.");
            clientNameToName.remove(clientName);
        }   catch(Exception e){
            System.err.println(e);
            System.err.println(e.getStackTrace());
        }
        
    }

}
