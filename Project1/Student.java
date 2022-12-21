import java.util.Random;

public class Student extends Thread {
    private Classroom classroom;
    private Neighborhood neighborhood;
    private int numGroups, group, numRounds;

    public Student(Classroom classroom, int numGroups, Neighborhood neighborhood, int numRounds, int id){
        this.classroom = classroom;
        this.numGroups = numGroups;
        this.neighborhood = neighborhood;
        this.numRounds = numRounds;
        setName("Student Thread " + Integer.toString(id));
    }

    public static long time = System.currentTimeMillis();
    public void msg(String m) {
        System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m);
    }

    public void run(){
        msg("Starting! On the way to school");
        Random random = new Random();
        try{
            sleep(random.nextInt(1000,5000)); // sleep randomly up to 2 seconds
        }catch(Exception e){}
        msg("Made it to school, time for lecture");
        classroom.arrive();
        msg("The teacher is finished. Time to get on line for candy");
        classroom.lineUp();
        msg("Yum, I got my candy. Time to group up for trick or treating");
        group = random.nextInt(numGroups);
        msg("I'm going to go with group " + Integer.toString(group+1)); // group is from 0
        neighborhood.startTrickOrTreating(group);
        while(neighborhood.notDone()){
            msg("Waiting to be called for trick or treating");
            neighborhood.trickOrTreat(group);
            // double check. may have been released from end of program
            if(neighborhood.notDone()){
                try{
                    sleep(random.nextInt(1000,5000));
                }catch (Exception e ){}
                msg("I got my candy. Leaving this house now and waiting for my group");
                neighborhood.leaveHouse(group);
            }
        }
        msg("The day is over! Let me count my candy and go home");
        neighborhood.goHome(group);
        msg("I'm going home now.");

    }
    
}
