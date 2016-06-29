from util import *
from analysis import *


if __name__ == "__main__":

	filename = "../../results/go2.res"
	results = loadprocessed(filename)


	df = "{0:.1f}"

	splt = split(results, "clientInvolvementLikelihood", "memoryLimit")
	print splt.keys()
	index = (0.2,100)
	# splt = {"all": results}
	# index = "all"
	topspltkeys = ["numClients", "numProviders", "honestWitnessLikelihood", "negationWitnessLikelihood", "randomWitnessLikelihood",
					"pessimisticWitnessLikelihood", "promotionWitnessLikelihood", "slanderWitnessLikelihood",
						"providersToPromote", "providersToSlander",
						"numSimCapabilities", "numProviderCapabilities", "numTerms"]
	botspltkeys = ["exp"]
	scorename = "gain100"
	topsplt = split(splt[index], *topspltkeys)
	for topkey,topval in sorted(topsplt.iteritems()):
		botsplt = split(topval, *botspltkeys)
		print topkey,
		for botkey,botval in sorted(botsplt.iteritems()):
			mn = findmean(botval, scorename)
			print "\t", df.format(mn[scorename]).zfill(5),
		print
	print sorted(botsplt.keys())
