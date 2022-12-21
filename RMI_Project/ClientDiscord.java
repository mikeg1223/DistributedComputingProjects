import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientDiscord extends Remote{
    public void sendMessage(String message) throws RemoteException;
    public void receiveMessage(String message) throws RemoteException; 
    public String getName() throws RemoteException;
}
