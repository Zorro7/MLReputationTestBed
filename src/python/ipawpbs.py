from makepbs import *

cmdargs1 = {
    "strategy": "jaspr.strategy.NoStrategy,jaspr.acmelogistics.strategy.ipaw.RecordFire,jaspr.acmelogistics.strategy.ipaw.IpawSimple(weka.classifiers.functions.LinearRegression;false),jaspr.acmelogistics.strategy.ipaw.Ipaw(weka.classifiers.functions.LinearRegression;false),jaspr.acmelogistics.strategy.ipaw.IpawEvents(weka.classifiers.functions.LinearRegression;false)",
    "numRounds": 1000,
    "numSimulations": 50,
    "memoryLimit": [100,250,500],
    "numProviders": [10,25,50,100],
    "defaultServiceDuration": [1,5],
    "eventProportion": [0.05,0.1,0.2,0.3],
    "eventLikelihood": [0,0.05,0.1,0.2],
    "eventDelay": [1,2,3],
    "adverts": "true"
}
cmdargs2 = {
    "strategy": "jaspr.acmelogistics.strategy.ipaw.IpawSimple(jaspr.utilities.weka.MultiRegression&weka.classifiers.functions.LinearRegression;false),jaspr.acmelogistics.strategy.ipaw.Ipaw(jaspr.utilities.weka.MultiRegression&weka.classifiers.functions.LinearRegression;false),jaspr.acmelogistics.strategy.ipaw.IpawEvents(jaspr.utilities.weka.MultiRegression&weka.classifiers.functions.LinearRegression;false)",
    "numRounds": 1000,
    "numSimulations": 50,
    "memoryLimit": [100,250,500],
    "numProviders": [10,25,50,100],
    "defaultServiceDuration": [1,5],
    "eventProportion": [0.05,0.1,0.2,0.3],
    "eventLikelihood": [0,0.05,0.1,0.2],
    "eventDelay": [1,2,3],
    "adverts": "false"
}


basecommand = "java -cp MLRS.jar jaspr.acmelogistics.ACMEMultiConfiguration"

cmds = makecommands(basecommand, cmdargs1)
# cmds = [cmd for cmds in makecommands(basecommand, args) for cmd in cmds]
for cmd in cmds:
    print "echo \""+cmd+"\"; "+cmd


cmds = makecommands(basecommand, cmdargs2)
# cmds = [cmd for cmds in makecommands(basecommand, args) for cmd in cmds]
for cmd in cmds:
    print "echo \""+cmd+"\"; "+cmd
