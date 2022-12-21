import java.net.*;
import java.util.Vector;
import java.util.Random;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.Collections;



public class Subserver extends Thread {
    public static final int TEACHER = 0;
    public static final int STUDENT = 1;
    public static final int HOUSE = 2;

    public static Teacher teacher;

    public static Vector<Subserver> students = new Vector<Subserver>();
    public static Vector<Subserver> houses = new Vector<Subserver>();
    public static Vector<Subserver> goHome = new Vector<Subserver>();
    public static Vector groups = new Vector<>();
    public static Vector candyPicked = new Vector<>();
    public static Vector<Integer> exiting = new Vector<Integer>();

    public static Object lectureFinished = new Object();
    public static Object getCandyFromTeacher = new Object();
    public static Object classStart = new Object();
    public static Object teacherObject = new Object();
    public static Object candyTurn = new Object();
    public static Object schoolFinished = new Object();

    public static boolean teacherWaiting = false;
    public static boolean lectureEnded = false;
    public static boolean teacherGone = false;
    public static boolean done = false;

    public static int missingStudents;
    public static int studentsLeft;
    public static int inLine = 0;
    public static int numGroups = -1;
    public static int numHouses = -1;
    public static int numStudents = -1;
    public static int numStudentsReady = 0;
    public static int numStudentsDone = 0;
    public static int numFinishedHouses = 0;
    public static int numWaiting = 0;
    public static int round = 0;

    public static int[] groupSizes;
    public static int[] groupWaitingSize;
    public static int[] totalCandies;
    public static boolean[] groupTable;

    public static BufferedWriter file;

    public static long time = System.currentTimeMillis();
    private int threadType;
    private Socket socket;
    private int group = -1;
    private int lastGroup = -1;
    private Random random;
    private BufferedReader br;
    private BufferedWriter bw;



    public Subserver(Socket socket, BufferedReader br, int threadType){
        this.socket = socket;
        this.threadType = threadType;
        this.random = new Random();
        this.br = br;
        try{
            this.bw = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            this.file = new BufferedWriter(new FileWriter("logs.txt"));
        }catch(Exception e){

        }
        if(this.threadType == 0){
            setName("Teacher Thread");
        }else if (this.threadType == 1){
            students.add(this);
            setName("Student Thread " + Integer.toString(students.size()));
        }else{
            houses.add(this);
            setName("House Thread " + Integer.toString(houses.size()));
        }
    }

    public static void setUp(){
        missingStudents = numStudents;
        studentsLeft = numStudents;
        groupSizes = new int[numGroups];
        groupWaitingSize = new int[numGroups];
        totalCandies = new int[numGroups];
        groupTable = new boolean[numGroups];

        for(int i = 0; i < numGroups; ++i){
            groups.addElement(new Object());
        }
        for(int i = 0; i < numGroups; ++i){
            candyPicked.addElement(new Object());
        }
        for(int i = 0; i < numGroups; ++i){
            groupTable[i] = false;
        }
    }

    public String msg(String m) {
        System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m);
        return "["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m + "\n";
    }

    public synchronized void writeOut(String m){
        try{
            file.write(msg(m));
        }catch(Exception e){}
    }

    // Run Method which has cases for the thread type
    //--------------------------------------------------------------------------------------------
    public void run(){
        int method = -1;
        try{
            bw.write("Ready");
            bw.flush();
            switch(threadType){
                case 0:{
                    while(method < 4){
                        method = br.read();
                        teacher(method);
                        if(method >= 4) bw.write("end");
                        else bw.write("OK");
                        bw.flush();
                    }
                        bw.write("end");
                        bw.flush();
                    break;
                }
                case 1: {
                    while(method < 7){
                        method = br.read();
                        student(method);
                        if(method == 4) bw.write( done() ? "done":"notdone");
                        else if(method == 7) bw.write("end");
                        else bw.write("OK");
                        bw.flush();
                    }
                    bw.write("end");
                    bw.flush();
                    break;
                }
                case 2:{
                    while(method < 2){
                        method = br.read();
                        house(method);
                        if(method == 1) bw.write(round == numHouses?"done":"notdone");
                        else if (method == 2) bw.write("end");
                        else bw.write("OK");
                        bw.flush();
                    }
                    bw.write("end");
                    bw.flush();
                    break;
                }
            }
            //Respond back to the thread to kick start the next phase of execution 
            bw.write("end");
            bw.flush();
            socket.close();
        }catch(Exception e){
            System.err.println(e.getStackTrace());
        }

    }

// Used in both Teacher and Student
//-------------------------------------------------------------------------------------------------------
    // Returns whether there are still students in transit, function common to Teacher and student
    public synchronized boolean studentsInTransit(Boolean isTeacher){
        boolean status = true;
        if(!isTeacher) missingStudents--;
        if(isTeacher && missingStudents == 0){
            status = false;
        }
        else if(missingStudents == 0){ // in this case we have a student
            synchronized(teacherObject){
                teacherObject.notifyAll();
            }
        }
        return status;
    }

// Implementation for teacher functions
// -------------------------------------------------------------------------------------------------------
    public void teacher(int i){
        switch(i){
            case 0:{
                writeOut("Heading to School!");
                teacherArrive();
                break;
            }
            case 1:{
                writeOut("Starting Lecture");
                Random random = new Random();
                try{
                    sleep(random.nextInt(1000,5000)); // sleep randomly up to 2 seconds
                }catch(Exception e){}
                break;
            }
            case 2: {
                writeOut("Ending Lecture");
                endLecture();
                break;
            }
            case 3:{
                writeOut("Giving out Candy");
                teacherGiveOutCandy();
                break;
            }
        }
    }
    // A function for teachers to wait in until all students arrive
    public void teacherArrive(){
        synchronized(teacherObject){
            if(studentsInTransit(true)){
                while (true) // wait to be notified, not interrupted
                try { teacherObject.wait(); break; }
                catch (InterruptedException e) { 
                    if(studentsInTransit(true)) continue; 
                    else break;
                }
            }
        }
    }
    
    // Wakes the student threads up so they can organize themselves into a line
    public synchronized void releaseStudents(){
        lectureEnded = true;
        synchronized(classStart){
            classStart.notifyAll();
        }
    }

    public synchronized boolean compareLineToStudents(){
        return inLine != studentsLeft;
    }

    public void endLecture(){
        releaseStudents();
        synchronized(teacherObject){
            while (compareLineToStudents()) // wait to be notified, not interrupted
            try { teacherObject.wait(); break; }
            catch (InterruptedException e) {
                if(compareLineToStudents()) continue;
                else break;
            }
        }
    }

    // Teacher give out candy to everyone at once
    public synchronized void teacherGiveOutCandy(){
        synchronized(candyTurn){
            candyTurn.notifyAll();
            teacherGone = true;
        }
    }

    // Implementation for Student Functions
    //-------------------------------------------------------------------------------------
    void student(int i){
        switch(i){
            case 0:{
                writeOut("On my way to school");
                try{
                    sleep(random.nextInt(1000,5000)); // sleep randomly up to 2 seconds
                }catch(Exception e){}
                break;
            }
            case 1: {
                writeOut("Arrived at school, waiting for and then attending lecture");
                studentArrive();
                break;
            }
            case 2:{
                writeOut("Lecture is over, going for candy");
                lineUp();
                break;
            }
            case 3: {
                writeOut("Got candy, grouping up");
                group = random.nextInt(numGroups);
                writeOut("I'm joinin group " + Integer.toString(group));
                startTrickOrTreating(group);
                break;
            }
            case 4:{
                writeOut("Waiting for group to be ready for the next house");
                trickOrTreat(group);
                break;
            }
            case 5: {
                try{
                    writeOut("Getting my candy");
                    sleep(random.nextInt(1000,5000));
                }catch (Exception e ){}
                break;
            }
            case 6: {
                writeOut("Leaving previous house with same group");
                leaveHouse(group);
                break;
            }
            case 7: {
                writeOut("Done with Trick or Treating for the night!");
                goHome(group);
                break;
            }
        }
    }

    // A function for students to wait in until they arrive and the lecture is complete
    public void studentArrive(){
        synchronized(classStart){
            if(studentsInTransit(false)){
                while (true) // wait to be notified, not interrupted
                try { classStart.wait(); break; }
                catch (InterruptedException e) { 
                    if(lectureEnded) continue; 
                    else break;}
            }
        }
    }

    //  Students place themselves in the logical line
    public synchronized void getInLine(){
        inLine++;
        if(inLine == studentsLeft){ //if all students have lined up the teacher starts serving them candy
            synchronized(teacherObject){
                teacherObject.notify();
            }
        }
    }

    public synchronized boolean notServed(){
        return !teacherGone;
    }

    // starts the process for getting in line for candy
    public void lineUp(){
        synchronized(candyTurn){
            getInLine();
            while (compareLineToStudents()) // wait to be notified, not interrupted
            try { candyTurn.wait(); break; }
            catch (InterruptedException e) { 
                if(notServed()) continue;
                else break;
             }
        }
    }

    // Students prepare to trick or treat by recording in the local variables that they are ready
    public synchronized void startTrickOrTreating(int group){
        numStudentsReady++;
        groupSizes[group]++;
        if(numStudentsReady == numStudents){ // all students are ready, start trick or treating
            synchronized(schoolFinished){schoolFinished.notifyAll();}
        }
    }


    public synchronized boolean notDone(){return !done;}
    public synchronized boolean done(){return done;}
    public synchronized boolean getGroupTableEntry(int group){return groupTable[group];}


    public synchronized void addCandy(int group, int amount){
        totalCandies[group] += amount;
    }

     // A house may have chosen a group already in a race condition, only block if the group is still waiting
     public void trickOrTreat(int group){
        if(!getGroupTableEntry(group)){
            synchronized(groups.elementAt(group)){
                while (notDone()) // wait to be notified, not interrupted
                    try { groups.elementAt(group).wait(); break; }
                    catch (InterruptedException e) { 
                        continue; // TODO factor for race condition
                    }
            }
        }
                Random random = new Random();
        // thread may have been blocking when houses ended. Check for this condition. 
        if(notDone()) addCandy(group, random.nextInt(10) + 1);
    }

    // Students regroup with eachother. Then release the house for the next round. 
    public synchronized void leaveHouse(int group){
        groupWaitingSize[group]++;
        if(groupWaitingSize[group] == groupSizes[group]){
            groupWaitingSize[group] = 0;
            synchronized(candyPicked.elementAt(group)){candyPicked.elementAt(group).notify();}
        }
    }

    // when we see the last student, group up in ascending order, and wake up the last student to leave
    public synchronized void cannotLeave(Integer convey){
        numStudentsDone++;
        exiting.addElement(convey);
        if(numStudentsDone == numStudents){
            Collections.sort(exiting);
            synchronized(exiting.elementAt(0)){exiting.elementAt(0).notify();}
        }
    }

    // only fully executes for the last person to leave, they release everyone else in order
    public synchronized void ifLastToLeave(){
        if(numStudentsDone == numStudents){
            for(int i = exiting.size()-1;i >= 0; --i){
                synchronized(exiting.elementAt(i)){exiting.elementAt(i).notify();}
                numStudents--;
            }
        }
    }

    // Calculate the average candies per group and store that information with the notification objects
    public void goHome(int group){
        int averageCandies = totalCandies[group] / groupSizes[group];
        Integer convey = Integer.valueOf(averageCandies);
        writeOut("My group average is " + Integer.toString(convey) +" candies each");
        synchronized(convey){
            cannotLeave(convey);
            //we always want to block here. Someone will be broken out of this to handle releasing 
            while (true) // wait to be notified, not interrupted
            try {  convey.wait(); break; }
            catch (InterruptedException e) { 
                continue; // TODO factor for race condition
            }
            ifLastToLeave();
            writeOut("I'm shutting down now, I had " + Integer.toString(averageCandies) + " candies");
        }
        try{file.close();}catch(Exception e){}
    }
    
    // Implementation for House functions
    //-------------------------------------------------------------------------------------------------

    void house(int i){
        switch(i){
            case 0:{
                writeOut("Students are in school, waiting");
                waitForStudents();
                break;
            }
            case 1: {
                writeOut("Choosing group for round " + Integer.toString(round));
                lastGroup = houseGiveOutCandy(lastGroup);
                writeOut("Gave candy to group " + Integer.toString(lastGroup));
                break;
            }
            case 2:{
                roundsDone();
                break;
            }
        }
    }

    public synchronized boolean schoolTime(boolean initial){
        boolean status = true;
        if(numStudentsReady == numStudents){ 
            status = false;
        }
        return status;
    }

    // Puts house threads on notification objects / condition variables to wait for students
    public void waitForStudents(){
        synchronized(schoolFinished){
            if(schoolTime(true)){
                while (true) // wait to be notified, not interrupted
                try { schoolFinished.wait(); break; }
                catch (InterruptedException e) { 
                    if(schoolTime(false))continue;
                    else break;
                }
            }
        }
    }

    // ME accesses through the monitor for a table which dictates whether or a not a group is already assigned
    public synchronized boolean TASGroupTableEntry(int group, boolean value){
        boolean sample = groupTable[group];
        groupTable[group] = value;
        return sample;
    }

    //A way for houses to detect when a new round has started
    public synchronized boolean notNextRound(){
        boolean status = true;
        numWaiting++;
        if(numWaiting == numHouses){
            status = false;
            writeOut("All houses are ready to pick");
            synchronized(houses){houses.notifyAll();}
            numWaiting = 0;
            round++;
        } 
        return status;
    }

    public synchronized boolean isHousesWaiting(){return numWaiting >0;}

    // notifys a group that it has been chosen by a house 
    public synchronized void callGroup(int nextPick, Object convey){
        candyPicked.setElementAt(convey, nextPick);
        synchronized(groups.elementAt(nextPick)){groups.elementAt(nextPick).notifyAll();}
    }

    // wait for a new round, then choose a group, wake them up, and give them candy
    public int houseGiveOutCandy(int lastGroup){
        synchronized(houses){
            if(notNextRound()){
                while (true) // wait to be notified, not interrupted
                try { houses.wait(); break; }
                catch (InterruptedException e) { 
                    if(isHousesWaiting()) continue; 
                    else break;
                }
            }
        }
        Random random = new Random();
        int nextPick = lastGroup;
        // pick must be different, and not already taken
        while(nextPick == lastGroup || groupSizes[nextPick] == 0 || TASGroupTableEntry(nextPick, true)) nextPick = random.nextInt(numGroups);
        writeOut("Chose group number " + Integer.toString(nextPick) + " for round " + Integer.toString(round-1));
        Object convey = new Object();
        synchronized(convey){
            callGroup(nextPick, convey);
            while (true) // wait to be notified, not interrupted
                try { convey.wait(); break; }
                catch (InterruptedException e) { 
                    continue; 
                }
        }
        //reset the reservation table
        TASGroupTableEntry(nextPick, false);
        return nextPick;
    }
    
    //when all houses are done, start releasing the groups for exiting processes
    public synchronized void roundsDone(){
        numFinishedHouses++;
        if(numFinishedHouses == numHouses){
            done = true;
            for(Object convey : groups) synchronized(convey){convey.notifyAll();}
        }
    }

    
}
