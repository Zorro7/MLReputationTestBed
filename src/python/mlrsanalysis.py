from util import *
from analysis import *
from sys import argv


def justprint(s):
	sys.stdout.write(s)
	sys.stdout.flush()


if __name__ == "__main__":

	if len(sys.argv) > 1:
		args = sys.argv[1:]
		filename = args[0]
		index = tuple([typeset(a.replace(",","")) for a in args[1:]])
	else:
		filename = "../../results/go5.4.res"
		index = (0.1,100,100,100,1,2,5)
	results = loadprocessed(filename)

	if int(index[-4]) < int(index[-3]):
		sys.exit(0)

	df = "{0:.1f}"
	strategies = [('NoStrategy',),
	('Fire-0.0',),
	 ('Fire-0.5',), ('BetaReputation',), ('Travos',), ('Blade-2',), ('Habit-2',),
	# ('Blade-5',), ('Habit-5',),
					# ('Burnett',),
					('BasicML',),
					('FireLike',),
					('BasicContext',),
					('FireLikeContext',),
					('BasicStereotype',),
					('FireLikeStereotype',),
					# ('Mlrs2-NaiveBayes-0.0-false',),
					# ('Mlrs2-NaiveBayes-1.0-false',),
					# ('Mlrs2-NaiveBayes-0.5-false',),
					# ('Mlrs2-NaiveBayes-2.0-false',),
					# ('Mlrs2-NaiveBayes-0.0-true',),
					# ('Mlrs2-NaiveBayes-1.0-true',),
					# ('Mlrs2-NaiveBayes-0.5-true',),
					('Mlrs2-NaiveBayes-2.0-true',),
					# ('Mlrs-J48-5-0.5',),
					# ('Mlrs-NaiveBayes-5-0.5',),
					# ('Mlrs-NaiveBayes-5-0.5',)
					]


	strategynamelookup = {
		"NoStrategy": "RAND\t\t",
		"Fire-0.0": "Basic\t\t",
		"Fire-0.5": "FIRE\t\t",
		"BetaReputation": "BetaRep\t\t",
		"Travos": "TRAVOS\t\t",
		"Blade-2": "BLADE\t\t",
		"Habit-2": "HABIT\t\t",
		"BasicML": "Basic-ML\t",
		"FireLike": "FIRE-ML\t\t",
		"BasicContext": "Context-ML\t",
		"BasicStereotype": "Stereotype-ML\t",
		"FireLikeStereotype": "FIRE-Stereotype-ML",
		"FireLikeContext": "FIRE-Context-ML",
		"Mlrs2-NaiveBayes-2.0-true": "MLRS\t\t"
	}

	# [('BasicML',), ('BasicStereotype',), ('BetaReputation',), ('Blade-2',), ('Fire-0.0',),  ('FireLike',), ('FireLikeStereotype',), ('Habit-2',), ('Mlrs-J48-10-0.0',), ('Mlrs-J48-10-0.5',), ('Mlrs-J48-2-0.0',),
	# ('Mlrs-J48-2-0.5',), ('Mlrs-NaiveBayes-10-0.0',), ('Mlrs-NaiveBayes-10-0.5',), ('Mlrs-NaiveBayes-2-0.0',), ('Mlrs-NaiveBayes-2-0.5',), ('NoStrategy',), ('Travos',)]

	splt = split(results, "clientInvolvementLikelihood", "memoryLimit", "numClients", "numProviders",
	"numTerms", "numAdverts", "usePreferences", "honestWitnessLikelihood")


	# topspltkeys = ["honestWitnessLikelihood"
	# 				, "negationWitnessLikelihood"
	# 				, "randomWitnessLikelihood"
	# 				,"optimisticWitnessLikelihood","pessimisticWitnessLikelihood"
	# 				#,"providersToPromote", "providersToSlander"
	# 				 ,"promotionWitnessLikelihood", "slanderWitnessLikelihood",
	# 					]
	topspltkeys = ["numSimCapabilities"]
	topsplt = split(splt[index], *topspltkeys)
	# exps = sorted(topsplt.keys(), reverse=True, key=lambda x: str(x))
	# exps = [e for e in exps if e[0] in [1,0.5]]
	exps = sorted(topsplt.keys(), key=lambda x: x[0])
	botspltkeys = ["exp"]
	scorename = "utility"

	means = []
	stds = []
	stderrs = []
	for topkey in exps:
		topval = topsplt[topkey]
		botsplt = split(topval, *botspltkeys)
		print [len(botsplt[botkey]) for botkey in strategies]
		mns = [botsplt[botkey][0][scorename+"_mean"] if botkey in botsplt else -9999 for botkey in strategies]
		sts = [botsplt[botkey][0][scorename+"_std"] if botkey in botsplt else -9999 for botkey in strategies]
		ses = [st / (float(botsplt[botkey][0]["iterations"])**0.5) for st in sts]
		means.append(mns)
		stds.append(sts)
		stderrs.append(ses)

	meanis = [maxindices(mns) for mns in means]



	print str(index)
	print "Strategy \t\t Honest \t Negation \t Random \t Opt \t\t Opt/Pess \t Pess \t\t Pro \t\t Pro/Sland \t Sland"
	for topi in xrange(0,len(strategies)):
		print strategynamelookup[str(strategies[topi])], "\t",
		for boti in xrange(0,len(exps)):
			if topi in meanis[boti]:
				justprint(".")
			else:
				justprint(" ")
			# print df.format(stderrs[boti][topi]*1.96),
			print df.format(means[boti][topi]).zfill(6), "("+df.format(stderrs[boti][topi]*1.96)+")",
			if boti < len(exps)-1:
				print "\t",
		print
	print




	# print "\\begin{table}\n\\begin{tabular}{l"+("r"*len(exps))+"}"
	# print "Strategy & Honest & Negation & Random & Opt & Opt/Pess & Pess & Pro & Pro/Sland & Sland",
	# print "\\\\"
	# for topi in xrange(0,len(strategies)):
	# 	print strategynamelookup[str(strategies[topi])], "&",
	# 	for boti in xrange(0,len(exps)):
	# 		if topi in meanis[boti]:
	# 			print "\\bf",
	# 		# print df.format(stderrs[boti][topi]*1.96),
	# 		print df.format(means[boti][topi]), "& ("+df.format(stderrs[boti][topi]*1.96)+")",
	# 		if boti < len(exps)-1:
	# 			print "&",
	# 	print "\\\\"
	# print "\\end{tabular}"
 # 	print "\\caption{"+str(index)+"}"
	# print "\\end{table}"


	# print "\\begin{tikzpicture}"
	# print "\\begin{axis}[ybar=0pt,bar width=1pt, width=\\textwidth]\n"
	# for topi in xrange(0,len(exps)):
	# 	print "\\addplot coordinates {"
	# 	for boti in xrange(0,len(strategies)):
	# 		print "("+str(boti)+","+df.format(means[topi][boti])+") +- (0,"+df.format(stderrs[topi][boti])+")"
	# 	print "};\n"
	# print "\n\\legend{"+','.join([str(s) for s in strategies])+"}"
	# print "\\end{axis}\n"
	# print "\\end{tikzpicture}"

	# means = zip(*means)
	# stds = zip(*stds)
	# stderrs = zip(*stderrs)
	# print "\\begin{tikzpicture}"
	# print "\\begin{axis}[ybar=0pt,bar width=1pt, width=\\textwidth]\n"
	# for topi in xrange(0,len(strategies)):
	# 	print "\\addplot coordinates {"
	# 	for boti in xrange(0,len(exps)):
	# 		print "("+str(boti)+","+df.format(means[topi][boti])+") +- (0,"+df.format(stderrs[topi][boti])+")"
	# 	print "};\n"
	# print "\n\\legend{"+','.join([str(s) for s in strategies])+"}"
	# print "\\end{axis}\n"
	# print "\\end{tikzpicture}"





		# print "\\begin{tikzpicture}"
		# print "\\begin{axis}[xbar]"
		# for topi,topkey in zip(xrange(0,len(topsplt)),topsplt):
		# 	print "\\addplot coordinates {"
		# 	topval = topsplt[topkey]
		# 	botsplt = split(topval, *botspltkeys)
		# 	for boti,botkey in zip(xrange(0,len(strategies)),strategies):
		# 		if botkey not in botsplt:
		# 			print "(0,"+str(boti)+" +- (0,0)"
		# 			continue
		# 		botval = botsplt[botkey]
		# 		mn = findmean(botval, scorename)
		# 		st = findstd(botval, scorename)
		# 		se = st[scorename] / float(len(botval))**0.5
		# 		print "("+df.format(mn[scorename]).zfill(5)+","+str(boti)+") +- ("+df.format(se)+",0)"
		# 	print "};\n"
		# 	if topi==2: break
		# print "\\legend{"+','.join([str(s) for s in strategies])+"}"
		# print "\n\\end{axis}"
		# print "\n\\end{tikzpicture}"
