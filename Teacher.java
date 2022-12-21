import java.util.Vector;
import java.util.Random;


public class Teacher extends Thread{
    private Classroom classroom;
    public Teacher(Classroom classroom){
        this.classroom = classroom;
        setName("Teacher Thread");
    }

    public static long time = System.currentTimeMillis();
    public void msg(String m) {
        System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m);
    }

    public void run(){
        msg("Starting! Heading to classroom");
        classroom.teacherArrive();
        msg("Starting Lecture");
        Random random = new Random();
        try{
            sleep(random.nextInt(1000,5000)); // sleep randomly up to 2 seconds
        }catch(Exception e){}
        msg("Ending Lecture");
        classroom.endLecture();
        msg("Giving Out Candy");
        classroom.giveOutCandy();
        msg("I'm done!");
    }

}