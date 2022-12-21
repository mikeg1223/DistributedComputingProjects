import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.io.FileReader;

public class Student extends Thread{
    
    public static long time = System.currentTimeMillis();
    private int port;
    private String host;

    public Student(int port, String host, int id){
        setName("Student Thread " + Integer.toString(id));
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
            bw.write("Student");
            bw.flush();
            int method = 0;
            String keyword = "" + (char)br.read();
            while(br.ready()){
                keyword += (char)br.read();
            }
            while(keyword.compareTo("end") != 0){
                if(method > 3 ) break;
                bw.write(method);
                bw.flush();
                method++;
                keyword = ""+ (char)br.read();
                while(br.ready()){
                    keyword += (char)br.read();
                }
            }
            // while(br.readLine().compareTo("end") != 0){
            //     if(method > 3) break;
            //     bw.write(method);
            //     method++;
            // }
            while(keyword.compareTo("done") != 0){
                if(method > 6) method = 4;
                bw.write(method);
                bw.flush();
                method++;
                keyword = ""+ (char)br.read();
                while(br.ready()){
                    keyword += (char)br.read();
                }
            }
            // while(br.readLine().compareTo("Done") != 0){
            //     if(method > 6) method = 4;
            //     bw.write(method);
            //     method++;
            // }
            method = 7;
            bw.write(7);
            bw.flush();
            br.read();
            s.close();
        }catch(Exception e){
            System.err.println(e.getStackTrace());
        }
        System.out.println("Finished");
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
            Student student = new Student(port, host, Integer.parseInt(args[0]));
            student.start();
            //student.join();

        }catch(Exception e){} 
    }


}
