from analysis import *
from util import *




if __name__ == "__main__":


	df = "{0:.4f}"

	args = sys.argv[1:]
	filename = args[0]
	scorename = args[1]
	index = tuple([typeset(a.replace(",", "")) for a in args[2:]])


	results = loadprocessed(filename)


	strategies = [('Random.res',),
				  ('DirectBRS.res',),
  				  ('WitnessBRS.res',),
				  ('DirectStereotype.res',),
				  ('DirectStereotypePrivateIds.res',),
				  ('WitnessStereotypeAssessObs.res',),
				  ('WitnessStereotypeFullObs.res',),
				  ('WitnessStereotypeInteractObs.res',),
				  ('WitnessStereotypePrivateIds.res',),
				  ('TransWitnessStereotypeAssessObs.res',),
				  ('TransWitnessStereotypeFullObs.res',),
				  ('TransWitnessStereotypeInteractObs.res',),
				  ('TransWitnessStereotypePrivateIdsAssessObs.res',),
				  ('TransWitnessStereotypePrivateIdsInteractObs.res',),
				]

	spltkeys = [#'observability',
				#'subjectivity',
				'trustorLeaveLikelihood',
				'trusteeLeaveLikelihood',
				# 'numTrustees',			
				# 'numTrustors',
				# 'memoryLimit',
				# 'trusteesAvailable',
				# 'trustorParticipation',
				# 'advisorsAvailable',			
				]
	splt = split(results, *spltkeys)

	print splt.keys()
	topspltkeys = ["observability","subjectivity"]
	topsplt = split(splt[index], *topspltkeys)
	exps = sorted(topsplt.keys(), key=lambda x: '-'.join([str(z) for z in x]))
	botspltkeys = ["resname"]

	means = []
	stds = []
	stderrs = []
	for topkey in exps:
		topval = topsplt[topkey]
		botsplt = split(topval, *botspltkeys)
		print botsplt.keys()
		print [len(botsplt[botkey]) for botkey in strategies]
		mns = [botsplt[botkey][0][scorename + "_mean"] if botkey in botsplt else -9999 for botkey in strategies]
		sts = [botsplt[botkey][0][scorename + "_std"] if botkey in botsplt else -9999 for botkey in strategies]
		ses = [st / (float(botsplt[botkey][0]["iterations"]) ** 0.5) for st in sts]
		means.append(mns)
		stds.append(sts)
		stderrs.append(ses)

	meanis = [maxindices(mns) for mns in means]

	print str(index), exps
	for topi in xrange(0, len(strategies)):
		print tabout(str(strategies[topi]),40), "\t",
		for boti in xrange(0, len(exps)):
			if topi in meanis[boti]:
				justprint(" ")
			else:
				justprint(" ")
			# sep = (stderrs[boti][topi]/means[boti][topi])*100
			sep = stderrs[boti][topi]
			# print df.format(means[boti][topi]).zfill(6), "(" + df.format(sep).zfill(5) + ")",
			print df.format(means[boti][topi]).zfill(6),
			if boti < len(exps) - 1:
				print "\t",
		print
	print
	print
	print "Standard deviations"

	for topi in xrange(0, len(strategies)):
		print tabout(str(strategies[topi]),40), "\t",
		for boti in xrange(0, len(exps)):
			if topi in meanis[boti]:
				justprint(" ")
			else:
				justprint(" ")
			# sep = (stderrs[boti][topi]/means[boti][topi])*100
			sep = stderrs[boti][topi]
			# print df.format(means[boti][topi]).zfill(6), "(" + df.format(sep).zfill(5) + ")",
			print df.format(stds[boti][topi]).zfill(6),
			if boti < len(exps) - 1:
				print "\t",
		print
	print













