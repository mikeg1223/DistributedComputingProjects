import java.util.Vector;


// is intended to act as a java monitor

public class Classroom {
    private int missingStudents, studentsLeft, inLine = 0;
    private Vector classStart = new Vector(), candyTurn  = new Vector(), teacherObject = new Vector();
    private boolean lectureEnded = false;

    // Set up section
    //===============================================================================================

    // Returns whether there are still students in transit
    public synchronized boolean studentsInTransit(Object convey, Boolean isTeacher){
        boolean status = true;
        if(!isTeacher) missingStudents--;
        if(isTeacher && missingStudents == 0){
            status = false;
        }
        else if(missingStudents == 0){ // in this case we have a student
            classStart.addElement(convey);
            synchronized(teacherObject.elementAt(0)){
                teacherObject.elementAt(0).notify();
                teacherObject.removeElementAt(0);
            }
        }
        else if(isTeacher){
            teacherObject.addElement(convey);
        }
        else{
            classStart.addElement(convey);
        }
        return status;
    }

    // A function for students to wait in until they arrive and the lecture is complete
    public void arrive(){
        Object convey = new Object();
        synchronized(convey){
            if(studentsInTransit(convey, false)){
                while (true) // wait to be notified, not interrupted
                try { convey.wait(); break; }
                catch (InterruptedException e) { 
                    if(lectureEnded) continue; 
                    else break;}
            }
        }
    }


    // A function for teachers to wait in until all students arrive
    public void teacherArrive(){
        Object convey = new Object();
        synchronized(convey){
            if(studentsInTransit(convey, true)){
                while (true) // wait to be notified, not interrupted
                try { convey.wait(); break; }
                catch (InterruptedException e) { 
                    if(studentsInTransit(convey, true)) continue; 
                    else break;
                }
            }
        }
    }

    // Lecture and Line coordination section
    //==================================================================================================

    // Wakes the student threads up so they can organize themselves into a line
    public synchronized void releaseStudents(Object convey){
        lectureEnded = true;
        for(Object obj : classStart) synchronized(obj){obj.notify();}
        teacherObject.addElement(convey);
    }

    public synchronized boolean compareLineToStudents(){
        return inLine != studentsLeft;
    }

    public void endLecture(){
        Object convey = new Object();
        synchronized(convey){
            releaseStudents(convey);
            while (true) // wait to be notified, not interrupted
            try { convey.wait(); break; }
            catch (InterruptedException e) {
                if(compareLineToStudents()) continue;
                else break;
            }
        }
    }

    //  Students place themselves in the logical line
    public synchronized void getInLine(Object convey){
        candyTurn.add(convey);
        inLine++;
        if(inLine == studentsLeft){ //if all students have lined up the teacher starts serving them candy
            synchronized(teacherObject.elementAt(0)){
                teacherObject.elementAt(0).notify();
                teacherObject.removeElementAt(0);
            }
        }

    }

    public synchronized boolean notServed(Object convey){
        return candyTurn.contains(convey);
    }
    
    // starts the process for getting in line for candy
    public void lineUp(){
        Object convey = new Object();
        synchronized(convey){
            getInLine(convey);
            while (true) // wait to be notified, not interrupted
            try { convey.wait(); break; }
            catch (InterruptedException e) { 
                if(notServed(convey)) continue;
                else break;
             }
        }
    }

    // Teacher continuously hands out candy while people are in line. 
    public synchronized void giveOutCandy(){
        while(!candyTurn.isEmpty()){
            synchronized(candyTurn.elementAt(0)){candyTurn.elementAt(0).notify();}
            candyTurn.removeElementAt(0);
        }
    }

    public Classroom(int numStudents){
        missingStudents = numStudents;
        studentsLeft = numStudents;
    }

}