import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileReader;

public class Teacher extends Thread{
    public static long time = System.currentTimeMillis();
    private int port;
    private String host;

    public Teacher(int port, String host){
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
            bw.write("Teacher");
            bw.flush();
            int method = 0;
            String keyword = "" + (char)br.read();
            while(br.ready()){
                keyword += (char)br.read();
            }
            while(keyword != "end"){
                bw.write(method);
                bw.flush();
                method++;
                keyword = ""+ (char)br.read();
                while(br.ready()){
                    keyword += (char)br.read();
                }
            }
            System.out.println("Finished");
            s.close();
        }catch(Exception e){

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
            

            Teacher teacher = new Teacher(port, host);
            teacher.start();
            //teacher.join();
        }catch(Exception e){
            System.err.println(e);
            System.err.println(e.getStackTrace());
        }
    }
    
}
