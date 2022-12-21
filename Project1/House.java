public class House extends Thread {
    private Neighborhood neighborhood;
    private int index, numRounds, lastGroup = -1;
    
    public House(Neighborhood neighborhood, int index, int numRounds, int id){
        this.neighborhood = neighborhood;
        this.index = index;
        this.numRounds = numRounds;
        setName("House Thread " + Integer.toString(id));
    }

    public static long time = System.currentTimeMillis();
    public void msg(String m) {
        System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m);
    }

    public void run(){
        msg("Waiting for students to finish with school");
        neighborhood.waitForStudents();
        msg("Students are done with school! Time to give out candy");
        for(int i = 0; i < numRounds; ++i){
            msg("Starting round " + Integer.toString(i+1));
            lastGroup = neighborhood.giveOutCandy(lastGroup);
            msg("Finished giving out candy for round " + Integer.toString(i+1) + " to group " + Integer.toString(lastGroup+1));
        }
        neighborhood.roundsDone();
        msg("House is finished giving out candy. Ending");
    }
}
