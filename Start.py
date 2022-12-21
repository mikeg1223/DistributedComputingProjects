import os

items = {}
with open("data.txt", "r") as file:
    for line in file:
        line = line.split(":")
        items[line[0]] = line[1]

os.system('start cmd /c Java Server')
os.system('start cmd /c Java Teacher')

for i in range(int(items["numStudents"])):
    os.system('start cmd /c Java Student '+ str(i+1))

for i in range(int(items["numHouses"])):
    os.system('start cmd /c Java House '+ str(i+1))

