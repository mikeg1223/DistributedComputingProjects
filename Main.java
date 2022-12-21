
public class Main {
    public static void main(String[] args){

        int numStudents = 20, numHouses = 4, numGroups = 5;

        Classroom classroom = new Classroom(numStudents);
        Neighborhood neighborhood = new Neighborhood(numHouses, numGroups, numStudents);
        Teacher teacher = new Teacher(classroom);
        teacher.start();
        Student[] students = new Student[numStudents];
        House[] houses = new House[numHouses];
        for(int i = 0; i < numStudents; ++i){
            students[i] = new Student(classroom, numGroups, neighborhood, numHouses, i+1);
            students[i].start();
        }
        for(int i = 0; i < numHouses; ++i){
            houses[i] = new House(neighborhood, i, numHouses, i+1);
            houses[i].start();
        }


    }
}
