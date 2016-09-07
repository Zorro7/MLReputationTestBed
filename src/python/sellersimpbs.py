from makepbs import *

witLikelihoodLevels = [0, 0.1, 0.3, 0.5]

witdics = [x for level in witLikelihoodLevels for x in
           [{"honestWitnessLikelihood": 1 - level, "pessimisticWitnessLikelihood": level,
             "negationWitnessLikelihood": 0, "randomWitnessLikelihood": 0,
             "promotionWitnessLikelihood": 0, "slanderWitnessLikelihood": 0,
             "providersToPromote": 0, "providersToSlander": 0},
            {"honestWitnessLikelihood": 1 - level, "pessimisticWitnessLikelihood": 0,
             "negationWitnessLikelihood": level, "randomWitnessLikelihood": 0,
             "promotionWitnessLikelihood": 0, "slanderWitnessLikelihood": 0,
             "providersToPromote": 0, "providersToSlander": 0},
            {"honestWitnessLikelihood": 1 - level, "pessimisticWitnessLikelihood": 0,
             "negationWitnessLikelihood": 0, "randomWitnessLikelihood": level,
             "promotionWitnessLikelihood": 0, "slanderWitnessLikelihood": 0,
             "providersToPromote": 0, "providersToSlander": 0},
            {"honestWitnessLikelihood": 1 - level, "pessimisticWitnessLikelihood": 0,
             "negationWitnessLikelihood": 0, "randomWitnessLikelihood": 0,
             "promotionWitnessLikelihood": level, "slanderWitnessLikelihood": 0,
             "providersToPromote": 0.25, "providersToSlander": 0.25},
            {"honestWitnessLikelihood": 1 - level, "pessimisticWitnessLikelihood": 0,
             "negationWitnessLikelihood": 0, "randomWitnessLikelihood": 0,
             "promotionWitnessLikelihood": 0, "slanderWitnessLikelihood": level,
             "providersToPromote": 0.25, "providersToSlander": 0.25},
            {"honestWitnessLikelihood": 1 - level, "pessimisticWitnessLikelihood": level / 3.,
             "negationWitnessLikelihood": level / 3., "randomWitnessLikelihood": level / 3.,
             "promotionWitnessLikelihood": 0, "slanderWitnessLikelihood": 0,
             "providersToPromote": 0, "providersToSlander": 0},
            {"honestWitnessLikelihood": 1 - level, "pessimisticWitnessLikelihood": level / 6.,
             "negationWitnessLikelihood": level / 6., "randomWitnessLikelihood": level / 6.,
             "promotionWitnessLikelihood": level / 6., "slanderWitnessLikelihood": level / 6.,
             "providersToPromote": 0.25, "providersToSlander": 0.25}]
           ]

cmdargs = {
    "strategy":
        ["jaspr.strategy.NoStrategy," \
         "jaspr.strategy.fire.Fire(0.5)," \
         "jaspr.strategy.fire.Fire(0.0)," \
         "jaspr.strategy.betareputation.BetaReputation," \
         "jaspr.strategy.betareputation.Travos," \
         "jaspr.strategy.blade.Blade(2)," \
         "jaspr.strategy.habit.Habit(2),",
         "jaspr.sellerssim.strategy.general.BasicML(weka.classifiers.trees.J48;2)," \
         "jaspr.sellerssim.strategy.general.FireLike(weka.classifiers.trees.J48;2)," \
         "jaspr.sellerssim.strategy.general.BasicStereotype(weka.classifiers.bayes.NaiveBayes;2)," \
         "jaspr.sellerssim.strategy.general.FireLikeStereotype(weka.classifiers.bayes.NaiveBayes;2)," \
         "jaspr.sellerssim.strategy.general.BasicML(weka.classifiers.trees.J48;10)," \
         "jaspr.sellerssim.strategy.general.FireLike(weka.classifiers.trees.J48;10)," \
         "jaspr.sellerssim.strategy.general.BasicStereotype(weka.classifiers.bayes.NaiveBayes;10)," \
         "jaspr.sellerssim.strategy.general.FireLikeStereotype(weka.classifiers.bayes.NaiveBayes;10)," \
         "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.trees.J48;2;0.5;false)," \
         "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.trees.J48;2;0.0;false)," \
         "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.bayes.NaiveBayes;2;0.5;true)," \
         "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.bayes.NaiveBayes;2;0.0;true)," \
         "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.trees.J48;10;0.5;false)," \
         "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.trees.J48;10;0.0;false)," \
         "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.bayes.NaiveBayes;10;0.5;true)," \
         "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.bayes.NaiveBayes;10;0.0;true),",
         # "jaspr.strategy.blade.Blade(10)," \
         # "jaspr.strategy.habit.Habit(10),",
         ],
    "numRounds": [1000],
    "numSimulations": 10,
    "memoryLimit": [100],
    "clientInvolvementLikelihood": [0.1],
    "numClients": [10, 25, 50],
    "numProviders": [10, 25, 50],
    "eventLikelihood": 0,
    "eventEffects": 0,
    "numSimCapabilities": [1, 5],
    "numProviderCapabilities": [1, 5],
    "numTerms": [1, 5],
    "wit": witdics
}

basecommand = "java -jar MLRS.jar "

cmds = makecommands(basecommand, cmdargs)
for cmd in cmds:
    print cmd
    # print "echo \""+cmd+"\"; "+cmd
