# Project 1

## Threads:
1. Students
2. Teachers
3. Houses

## Default Values
1. numStudents = 20
2. numHouses = 4
3. numGroups = 5

## Story

start:
students sleep
teacher waits for all students to finish sleeping
teacher then lectures. Students wait for this to finish.
students line up and wait for candy
The teacher gives out candy in line order. (rwcv like)
Once all candy is given out the teacher terminates.

After getting candy students join one of numGroups, generate a random int from 1 to numGroups.
Students wait with their group to be invited to trick or treat at a house.

Each round a house can be trick or treated by one group. One house per group. 
Each round a different group than last round is chosen
After picking a group the hosue signals that group.
House waits until the group is done picking candies.

Students pick a random amount of candies from 1 to 10 and sleeps shortly.
After all students are done they wait together to be selected by another house.

After numHouse rounds the trick or treating ends and the houses terminate

Lastly each group computes it's average number of candies per student.
Groups of students terminate in order of ranking of candy amount

## Classroom Monitor

### Methods
1. arrive() -- student arrives, reduces missingStudents. if missingStudents == 0 release teacher
2. allPresent() -- boolean synchronized to see if all students have arrived
3. 

### Condition Variables
1. classStart
2. lectureDone
3. candyTurn
4. studentsWaiting

### Local variables
1. missingStudents = numStudents 
2. studentsLeft = numStudents
3. 


## Neighborhood Monitor

### Methods
1. 
2. 
3. 

### Condition Variables 
1. nextRound -- houses wait for the next round here
2. 
3. 

### Local Variables
1. 
2. 
3. 