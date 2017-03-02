from analysis import *
from util import *




if __name__ == "__main__":


	df = "{0:.4f}"

	args = sys.argv[1:]
	filename = args[0]
	scorename = args[1]
	index = tuple([typeset(a.replace(",", "")) for a in args[2:]])


	results = loadprocessed(filename)


	strategies = [	('Random.res',),
					('Fire-0.res',),
					('Fire-1.res',),
					('Fire-0.5.res',),
					('Fire-2.res',),
					('BRS-0.res',),
					('BRS-1.res',),
					('BRS-0.5.res',),
					('BRS-2.res',),
					# ('FireContext-0.res',),
					# ('FireContext-1.res',),
					# ('FireContext-0.5.res',),
					# ('FireContext-2.res',),
					# ('BRSContext-0.res',),
					# ('BRSContext-1.res',),
					# ('BRSContext-0.5.res',),
					# ('BRSContext-2.res',),
					('Blade-2.res',),
					('Habit-2.res',),
					('MLBRS-NB-2.res',),
					('MLBRSContext-NB-2.res',),
					('MLBRSStereotype-NB-2.res',),
					('MLBRSStereotypeContext-NB-2.res',),
					('MLFireContext-NB-2-2.res',),
					('MLFire-NB-2-2.res',),
					('MLFireStereotype-NB-2-2.res',),
					('MLFireStereotypeContext-NB-2-2.res',),
					('MLHabit-NB-2-2.res',),
					('MLHabitContext-NB-2-2.res',),
					('MLHabitStereotype-NB-2-2.res',),
					('MLHabitStereotypeContext-NB-2-2.res',),
					# ('MLBRS-RF-2.res',),
					# ('MLBRSContext-RF-2.res',),
					# ('MLBRSStereotype-RF-2.res',),
					# ('MLBRSStereotypeContext-RF-2.res',),
					# ('MLFire-RF-2-2.res',),
					# ('MLFireContext-RF-2-2.res',),
					# ('MLFireStereotype-RF-2-2.res',),
					# ('MLFireStereotypeContext-RF-2-2.res',),
					# ('MLHabit-RF-2-2.res',),
					# ('MLHabitContext-RF-2-2.res',),
					# ('MLHabitStereotype-RF-2-2.res',),
					# ('MLHabitStereotypeContext-RF-2-2.res',),
]




	spltkeys = ['stereotypeFeatureNoise',
				'contextFeatureNoise',
				'numPreferences',
				# 'trustorLeaveLikelihood',	
				]
	splt = split(results, *spltkeys)

	print splt.keys()
	topspltkeys = ['trustorLeaveLikelihood']
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
				justprint(".")
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













