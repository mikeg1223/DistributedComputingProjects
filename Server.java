import java.io.BufferedInputStream;
import java.net.*;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileReader;

public class Server{

    public ServerSocket serverSocket;
    public BufferedReader br;
    public int numGroups;
    public int numHouses;
    public int numStudents;
    
    public Server(String host, int port){
        try{
            serverSocket = new ServerSocket(port);
        } catch(Exception e){
            System.err.println(e);
            System.err.println(e.getStackTrace());
        }
    }

    public void start(){

        try{

            while(true){
                Socket s = serverSocket.accept();
                System.out.println("Connection detected.");
                br = new BufferedReader(new InputStreamReader( s.getInputStream()));
                int type = -1;
                String tempType =""+ (char)br.read();
                while(br.ready()){
                    tempType += (char)br.read();
                }
                System.out.println(tempType);
                if (tempType.compareTo("Student") == 0){
                    type = Subserver.STUDENT;
                } else if (tempType.compareTo("Teacher") == 0){
                    type = Subserver.TEACHER;
                } else{
                    type = Subserver.HOUSE;
                }
                Subserver subserver = new Subserver(s, br, type);
                subserver.start();
            }
        }catch(Exception e){
            System.err.println(e);
            System.err.println(e.getStackTrace());
        }
    }

    public static void main(String[] args){
        
        try{
            BufferedReader dataReader = new BufferedReader(new FileReader("data.txt"));
            String line = dataReader.readLine();
            String[] items = line.split(":");
            String host = items[1];
            line = dataReader.readLine();
            items = line.split(":");
            int port = Integer.parseInt(items[1]);
            line = dataReader.readLine();
            items = line.split(":");
            Subserver.numStudents = Integer.parseInt(items[1]);
            line = dataReader.readLine();
            items = line.split(":");
            Subserver.numGroups = Integer.parseInt(items[1]);
            line = dataReader.readLine();
            items = line.split(":");
            Subserver.numHouses = Integer.parseInt(items[1]);
            Subserver.setUp();

            Server server = new Server(host, port);
            server.start();
        }catch(Exception e){
            System.err.println(e);
            System.err.println(e.getStackTrace());
        }
    }
    
}