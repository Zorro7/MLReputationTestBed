from analysis import *
from anova import *

if __name__ == "__main__":
	filename = "../../results/go5.4.res"
	results = loadprocessed(filename)

	strategies = ['NoStrategy','Fire-0.5','BetaReputation','Travos','Blade-2','Habit-2','Blade-5','Habit-5',
					'Burnett','FireLikeStereotype','BasicStereotype','Mlrs-J48-5-0.5','Mlrs-NaiveBayes-2-0.5','Mlrs-NaiveBayes-5-0.5',]

	splt = split(results, "clientInvolvementLikelihood", "memoryLimit", "numClients", "numProviders", "numTerms", "numAdverts", "usePreferences", "numSimCapabilities", "numProviderCapabilities", "exp")


	for strategy in strategies:

		print "-------", strategy, "------*"
		index = (0.1,100,100,100,5,0,"true",1,5,strategy)
		strategyresults = splt[index]

		numreplicates = 25
		divs = [  "negationWitnessLikelihood", "randomWitnessLikelihood", "optimisticWitnessLikelihood",
					 "pessimisticWitnessLikelihood"#, "promotionWitnessLikelihood", "slanderWitnessLikelihood"
					]


		measurement = "utility"
		nwayanova(strategyresults, numreplicates, divs, measurement)
		print "\n\n\n"
