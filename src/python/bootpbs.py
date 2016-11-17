from makepbs import *
import sys

args = sys.argv[1:]
strategy = args[0]

cmdargs = {
    "strategy": strategy,
    "numSimulations": 5,
    "memoryLimit,numRounds": [250],
    "numTrustees": 100,
    "numTrustors": 20,
    "observability": [0.25,0.5,0.75,1],
    "subjectivity": [0.25,0.5,0.75,1],
    "trusteesAvailable": 10,
    "advisorsAvailable": 10,
    "trustorParticipation": 1,
    # "trusteeLeaveLikelihood,trustorLeaveLikelihood": [0.0,0.05,0.1,0.15,0.2,0.25,0.3,0.35,0.4,0.45,0.5],
    "trusteeLeaveLikelihood,trustorLeaveLikelihood": [0.0,0.1,0.2,0.3,0.4,0.5],
}

basecommand = "java -jar MLRS.jar "

cmds = makecommands(basecommand, cmdargs)
for cmd in cmds:
    print cmd
    # print "echo \""+cmd+"\"; "+cmd
