import java.util.Random;
import java.util.Vector;
import java.util.Collections;

public class Neighborhood {
    
    private int numHouses, numGroups, numWaiting, numStudents, numStudentsReady, numStudentsDone, numFinishedHouses;
    private Vector schoolFinished = new Vector(), groups = new Vector(), houses = new Vector(), candyPicked = new Vector();
    private Vector exiting = new Vector<Integer>();
    private int[] groupSizes;
    private int[] groupWaitingSize;
    private int[] totalCandies;
    private boolean[] groupTable;
    private boolean done;

    // Set-up section, concerned with coordinating whether students are ready to trick or treat
    //===================================================================================================
    public synchronized boolean schoolTime(Object convey, boolean initial){
        boolean status = true;
        if(numStudentsReady == numStudents){ 
            status = false;
        }
        else if (initial){ // if this is the first time this thread executed this
            schoolFinished.addElement(convey);
        }
        return status;
    }

    // Puts house threads on notification objects / condition variables to wait for students
    public void waitForStudents(){
        Object convey = new Object();
        synchronized(convey){
            if(schoolTime(convey, true)){
                while (true) // wait to be notified, not interrupted
                try { convey.wait(); break; }
                catch (InterruptedException e) { 
                    if(schoolTime(convey, false))continue;
                    else break;
                }
            }
        }
    }

    // Students prepare to trick or treat by recording in the local variables that they are ready
    public synchronized void startTrickOrTreating(int group){
        numStudentsReady++;
        groupSizes[group]++;
        if(numStudentsReady == numStudents){ // all students are ready, start trick or treating
            for(int i = 0; i < schoolFinished.size(); ++i){
                synchronized(schoolFinished.elementAt(i)){schoolFinished.elementAt(i).notify();}
            }
        }
    }

    // Trick or treating section, concerned with coordinating the group and houses
    //==============================================================================================
    
    //A way for houses to detect when a new round has started
    public synchronized boolean notNextRound(Object convey){
        boolean status = true;
        numWaiting++;
        if(numWaiting == numHouses){
            status = false;
            while(houses.size() > 0){ //new round, wake up all the houses
                synchronized(houses.elementAt(0)){houses.elementAt(0).notify();}
                houses.removeElementAt(0);
            }
            numWaiting = 0;
        } 
        else{
            houses.addElement(convey);
        }

        return status;
    }

    public synchronized boolean isHousesWaiting(){return numWaiting >0;}

    // notifys a group that it has been chosen by a house 
    public synchronized void callGroup(int nextPick, Object convey){
        candyPicked.setElementAt(convey, nextPick);
        synchronized(groups.elementAt(nextPick)){groups.elementAt(nextPick).notifyAll();}
    }

    // ME accesses through the monitor for a table which dictates whether or a not a group is already assigned
    public synchronized boolean TASGroupTableEntry(int group, boolean value){
        boolean sample = groupTable[group];
        groupTable[group] = value;
        return sample;
    }

    public synchronized boolean getGroupTableEntry(int group){return groupTable[group];}

    // wait for a new round, then choose a group, wake them up, and give them candy
    public int giveOutCandy(int lastGroup){
        Object convey = new Object();
        synchronized(convey){
            if(notNextRound(convey)){
                while (true) // wait to be notified, not interrupted
                try { convey.wait(); break; }
                catch (InterruptedException e) { 
                    if(isHousesWaiting()) continue; 
                    else break;
                }
            }
        }
        Random random = new Random();
        int nextPick = lastGroup;
        // pick must be different, and not already taken
        while(nextPick == lastGroup || TASGroupTableEntry(nextPick, true)) nextPick = random.nextInt(numGroups);
        synchronized(convey){
            callGroup(nextPick, convey);
            while (true) // wait to be notified, not interrupted
                try { convey.wait(); break; }
                catch (InterruptedException e) { 
                    continue; 
                }
        }
        //reset the table
        TASGroupTableEntry(nextPick, false);
        return nextPick;
    }

    public synchronized void addCandy(int group, int amount){
        totalCandies[group] += amount;
    }

    // A house may have chosen a group already in a race condition, only block if the group is still waiting
    public void trickOrTreat(int group){
        if(!getGroupTableEntry(group)){
            synchronized(groups.elementAt(group)){
                while (true) // wait to be notified, not interrupted
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

    // Ending section. Concerned with organizing the stuends so they can leave in order
    //=====================================================================================================

    //when all houses are done, start releasing the groups for exiting processes
    public synchronized void roundsDone(){
        numFinishedHouses++;
        if(numFinishedHouses == numHouses){
            done = true;
            for(Object convey : groups) synchronized(convey){convey.notifyAll();}
        }
    }

    public synchronized boolean notDone(){return !done;}
    public synchronized boolean done(){return done;}

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
        synchronized(convey){
            cannotLeave(convey);
            //we always want to block here. Someone will be broken out of this to handle releasing 
            while (true) // wait to be notified, not interrupted
            try {  convey.wait(); break; }
            catch (InterruptedException e) { 
                continue; // TODO factor for race condition
            }
            ifLastToLeave();
        }
    }

    public Neighborhood(int numRounds, int numGroups, int numStudents){
        this.numHouses = numRounds;
        this.numGroups = numGroups;
        this.numWaiting = 0;
        this.numStudents = numStudents;
        numStudentsReady = 0;
        numFinishedHouses = 0;
        groupSizes = new int[numGroups];
        groupWaitingSize = new int[numGroups];
        totalCandies = new int[numGroups];
        numStudentsDone = 0;
        done = false;
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
}
