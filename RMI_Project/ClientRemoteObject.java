import java.io.*;

// Extending the client interface
public class ClientRemoteObject implements ClientDiscord {

    // keeps the client name and keeps a reference to the stub routing to the server
    private String name;
    private ServerDiscord serverDiscord;

    public ClientRemoteObject(String name, ServerDiscord serverDiscord){
        this.name = name;
        this.serverDiscord = serverDiscord;
    }

    // sends out the message to all other clients through the server
    public void sendMessage(String message){
        try{
            serverDiscord.clientToAll(message, name);
        }catch(Exception e){
            System.err.println(e);
        }
    }

    // uses newline to avoid confusion when user is mid typing when receiving a message
    public void receiveMessage(String message){
        System.out.println("\n"+message);
    }

    // to get a nickname for the user
    public String getName(){
        String serverName = "";
        try{
            System.out.println("____________________________________________________");
            System.out.println("|           What is your server name?              |");
            System.out.println("|__________________________________________________|\n\n");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            serverName = reader.readLine();
        }catch (Exception e){
            System.err.println(e);
        }
        return serverName;
    }
}
