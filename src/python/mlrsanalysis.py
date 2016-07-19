from util import *
from analysis import *
from sys import argv

if __name__ == "__main__":

	if len(sys.argv) > 1:
		args = sys.argv[1:]
		filename = args[0]
		index = tuple([typeset(a.replace(",","")) for a in args[1:]])
	else:
		filename = "../../results/go5.4.res"
		index = (0.1,100,100,100,1,2,5)
	results = loadprocessed(filename)

	if int(index[6]) < int(index[7]):
		sys.exit(0)

	df = "{0:.1f}"
	strategies = [('NoStrategy',),('Fire-0.0',), ('Fire-0.5',), ('BetaReputation',), ('Travos',), ('Blade-2',), ('Habit-2',),
	# ('Blade-5',), ('Habit-5',),
					# ('Burnett',),
					('BasicML',),
					('FireLike',),
					('BasicContext',),
					('FireLikeContext',),
					('BasicStereotype',),
					('FireLikeStereotype',),
					('Mlrs2-NaiveBayes-0.0-false',),
					('Mlrs2-NaiveBayes-1.0-false',),
					('Mlrs2-NaiveBayes-0.5-false',),
					('Mlrs2-NaiveBayes-2.0-false',),
					('Mlrs2-NaiveBayes-0.0-true',),
					('Mlrs2-NaiveBayes-1.0-true',),
					('Mlrs2-NaiveBayes-0.5-true',),
					('Mlrs2-NaiveBayes-2.0-true',),
					# ('Mlrs-J48-5-0.5',),
					# ('Mlrs-NaiveBayes-5-0.5',),
					# ('Mlrs-NaiveBayes-5-0.5',)
					]
	# [('BasicML',), ('BasicStereotype',), ('BetaReputation',), ('Blade-2',), ('Fire-0.0',),  ('FireLike',), ('FireLikeStereotype',), ('Habit-2',), ('Mlrs-J48-10-0.0',), ('Mlrs-J48-10-0.5',), ('Mlrs-J48-2-0.0',),
	# ('Mlrs-J48-2-0.5',), ('Mlrs-NaiveBayes-10-0.0',), ('Mlrs-NaiveBayes-10-0.5',), ('Mlrs-NaiveBayes-2-0.0',), ('Mlrs-NaiveBayes-2-0.5',), ('NoStrategy',), ('Travos',)]

	splt = split(results, "clientInvolvementLikelihood", "memoryLimit", "numClients", "numProviders",
	"numSimCapabilities", "numProviderCapabilities", "numTerms", "numAdverts", "usePreferences")

# 0.1, 100.0, 100.0, 100.0, 1.0, 5.0, 5.0
# 0.1, 100.0, 100.0, 100.0, 5.0, 5.0, 5.0
# 0.1, 100.0, 100.0, 100.0, 5.0, 2.0, 5.0

# 0.1, 100.0, 100.0, 100.0, 1.0, 2.0, 5.0
# 0.1, 100.0, 25.0, 25.0, 5.0, 2.0, 5.0
# 0.1, 100.0, 25.0, 25.0, 5.0, 5.0, 5.0
# 0.1, 100.0, 25.0, 25.0, 1.0, 2.0, 5.0
# 0.1, 100.0, 25.0, 25.0, 1.0, 5.0, 5.0

	# splt = {"all": results}
	# index = "all"
	topspltkeys = ["honestWitnessLikelihood"
					, "negationWitnessLikelihood"
					, "randomWitnessLikelihood"
					,"optimisticWitnessLikelihood","pessimisticWitnessLikelihood"
					#,"providersToPromote", "providersToSlander"
					 ,"promotionWitnessLikelihood", "slanderWitnessLikelihood",
						]
	topsplt = split(splt[index], *topspltkeys)
	# exps = [(1,0,0,0,0,0,0)]
	exps = sorted(topsplt.keys(), reverse=True, key=lambda x: str(x))
	exps = [e for e in exps if e[0] in [1,0.5]]
	botspltkeys = ["exp"]
	scorename = "utility250"

	means = []
	stds = []
	stderrs = []
	for topkey in exps:
		topval = topsplt[topkey]
		botsplt = split(topval, *botspltkeys)
		mns = [botsplt[botkey][0][scorename+"_mean"] if botkey in botsplt else -9999 for botkey in strategies]
		sts = [botsplt[botkey][0][scorename+"_std"] if botkey in botsplt else -9999 for botkey in strategies]
		ses = [st / (float(botsplt[botkey][0]["iterations"])**0.5) for st in sts]
		means.append(mns)
		stds.append(sts)
		stderrs.append(ses)

	meanis = [maxindices(mns) for mns in means]

	print "\\begin{table}\n\\begin{tabular}{l"+("r"*len(exps))+"}"
	print "Strategy & ",
	for boti in xrange(0,len(exps)):
		print exps[boti],
		if boti < len(exps)-1:
			print "&",
	print "\\\\"
	for topi in xrange(0,len(strategies)):
		print str(strategies[topi]), "&",
		for boti in xrange(0,len(exps)):
			if topi in meanis[boti]:
				print "\\bf",
			# print df.format(stderrs[boti][topi]*1.96),
			print df.format(means[boti][topi]),
			if boti < len(exps)-1:
				print "&",
		print "\\\\"
	print "\\end{tabular}"
 	print "\\caption{"+str(index)+"}"
	print "\\end{table}"


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
