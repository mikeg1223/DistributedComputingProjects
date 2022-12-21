import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileReader;

public class House extends Thread{
    public static long time = System.currentTimeMillis();
    private int port;
    private String host;
    public House(int port, String host, int id){
        setName("House Thread " + Integer.toString(id));
        this.port = port;
        this.host = host;
    }
    
    public void msg(String m) {
        System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m);
    }

    public void run(){
        try{
            Socket s = new Socket(host, port);
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            bw.write("House");
            bw.flush();
            int method = 0;
            String temp =""+ (char)br.read();
            while(br.ready()){
                temp += (char)br.read();
            }
            bw.write(method);
            bw.flush();
            method++;
            String keyword = "" + (char)br.read();
            while(br.ready()){
                keyword += (char)br.read();
            }
            while(keyword.compareTo("done") != 0){
                bw.write(method);
                bw.flush();
                keyword = ""+ (char)br.read();
                while(br.ready()){
                    keyword += (char)br.read();
                }
            }
            // while(br.readLine().compareTo("done") != 0){
            //     bw.write(method);
            // }
            method++;
            while(keyword.compareTo("end") != 0){
                bw.write(method);
                bw.flush();
                method++;
                keyword = ""+ (char)br.read();
                while(br.ready()){
                    keyword += (char)br.read();
                }
            }
            // while(br.readLine().compareTo("end") != 0){
            //     bw.write(method);
            //     method++;
            // }
            s.close();
            System.out.println("Finished");
        }catch (Exception e){

        }
    }

    public static void main(String[] args){
        try{
            BufferedReader dataReader = new BufferedReader(new FileReader("data.txt"));
            String line = dataReader.readLine();
            String[] data = line.split(":");
            String host = data[1];
            line = dataReader.readLine();
            data = line.split(":");
            int port = Integer.parseInt(data[1]);
            House house = new House(port, host, Integer.parseInt(args[0]));
            house.start();
            //house.join();

        }catch(Exception e){} 
    }

}
