import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerDiscord extends Remote{
    public void broadCastToAll(String message) throws RemoteException;
    public String welcome(String clientName)  throws RemoteException;
    public void clientToAll(String message, String clientName) throws RemoteException;
    public void removeClient(String clientName) throws RemoteException;
}
