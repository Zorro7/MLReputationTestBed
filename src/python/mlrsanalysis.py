from analysis import *
from util import *


def justprint(s):
    sys.stdout.write(s)
    sys.stdout.flush()


if __name__ == "__main__":

    if len(sys.argv) > 1:
        args = sys.argv[1:]
        filename = args[0]
        scorename = args[1]
        index = tuple([typeset(a.replace(",", "")) for a in args[2:]])
    else:
        filename = "../../results/go5.4.res"
        index = (0.1, 100, 100, 100, 1, 2, 5)
        scorename = "utility"
    results = loadprocessed(filename)

    # if int(index[-4]) < int(index[-3]):
    # 	sys.exit(0)

    df = "{0:.1f}"
    strategies = [('NoStrategy',),
                  ('Fire-0.0-false',),
                  ('Fire-0.5-false',), 
                  ('BetaReputation-0.0',), 
                  ('BetaReputation-0.5',), 
                  ('Travos',), 
                  ('Blade-2',), 
                  ('Habit-2',),
                  ('Burnett',),
                  ('BasicML-RandomForest',),
                  ('FireLike-RandomForest',),
                  ('BasicContext-RandomForest-false',),
                  # ('BasicContext-RandomForest-true',),
                  ('FireLikeContext-RandomForest-false',),
                  # ('FireLikeContext-RandomForest-true',),
                  ('BasicStereotype-RandomForest-false',),
                  ('BasicStereotype-RandomForest-true',),
                  ('FireLikeStereotype-RandomForest-false',),
                  ('FireLikeStereotype-RandomForest-true',),
                  ('Mlrs-RandomForest-2.0-false-false-false-false',),
                  ('Mlrs-RandomForest-2.0-false-true-false-false',),
                  ('Mlrs-RandomForest-2.0-true-false-false-false',),
                  ('Mlrs-RandomForest-2.0-true-true-false-false',),
                  # ('Mlrs-RandomForest-2.0-true-true-true-false',),
                  ('Mlrs-RandomForest-2.0-true-true-true-true',),
                  # ('Mlrs-RandomForest-0.0-false-false-false-false',),
                  # ('Mlrs-RandomForest-0.0-false-false-true-true',),
                  # ('Mlrs-RandomForest-0.0-true-false-false-false',),
                  # ('Mlrs-RandomForest-0.0-true-true-false-false',),
                  # ('MlrsB2-RandomForest-round-500.0-2.0-true-true',),
                  # ('MlrsB2-RandomForest-round-750.0-2.0-true-true',),
                  # ('MlrsB-RandomForest-round-1000.0-2.0-true-true-false',),
                  # ('MlrsB2-NaiveBayes-directRecords-2.0-2.0-true',),
                  # ('MlrsB2-NaiveBayes-directRecords-5.0-2.0-true',),
                  # ('MlrsB2-NaiveBayes-directRecords-10.0-2.0-true',),
                  # ('MlrsB2-NaiveBayes-round-10.0-2.0-true',),
                  # ('MlrsB2-NaiveBayes-round-25.0-2.0-true',),
                  # ('MlrsB2-NaiveBayes-round-50.0-2.0-true',),
                  # ('MlrsB2-NaiveBayes-round-100.0-2.0-true',),
                  # ('MlrsB-NB-2-5-0.6-2.0-true.res',),
                  # ('MlrsB-NB-2-5-0.7-2.0-true.res',),
                  # ('MlrsB-NB-2-5-0.8-2.0-true.res',),
                  # ('MlrsB-NB-2-5-0.9-2.0-true.res',),
                  ]

    strategynamelookup = {
        "NoStrategy": "RAND\t\t\t\t",
        "Fire-0.0-false": "Basic\t\t\t\t",
        "Fire-0.5-false": "FIRE\t\t\t\t",
        "BetaReputation-0.0": "BasicBeta\t\t\t",
        "BetaReputation-0.5": "BetaRep\t\t\t\t",
        "Travos": "TRAVOS\t\t\t\t",
        "Blade-2": "BLADE\t\t\t\t",
        "Habit-2": "HABIT\t\t\t\t",
        "BasicML": "Basic-ML\t\t\t",
        "FireLike": "FIRE-ML\t\t\t",
        "Burnett": "Burnett\t\t\t\t",
        "BasicML-RandomForest": "BasicML\t\t\t\t",
        "FireLike-RandomForest": "FireML\t\t\t\t",
        "BasicContext-RandomForest-false": "Context-ML\t\t\t",
        "BasicContext-RandomForest-true": "PayloadContext-ML\t\t",
        "BasicStereotype-RandomForest-false": "Stereotype-ML\t\t\t",
        "BasicStereotype-RandomForest-true": "PayloadStereotype-ML\t\t",
        "FireLikeStereotype-RandomForest-false": "FIRE-Stereotype-ML\t\t",
        "FireLikeStereotype-RandomForest-true": "FIRE-PayloadStereotype-ML\t",
        "FireLikeContext-RandomForest-true": "FIRE-Context-ML\t\t\t",
        "FireLikeContext-RandomForest-false": "FIRE-PayloadContext-ML\t\t",
        "Mlrs-RandomForest-2.0-false-false-false-false": "MLRS\t\t\t\t",
        "Mlrs-RandomForest-2.0-true-false-false-false": "MLRS-c\t\t\t\t",
        "Mlrs-RandomForest-2.0-false-true-false-false": "MLRS-p\t\t\t\t",
        "Mlrs-RandomForest-2.0-true-true-false-false": "MLRS-cp\t\t\t\t",
        "Mlrs-RandomForest-0.0-false-false-true-true": "MLRS-0\t\t\t\t",
        "Mlrs-RandomForest-0.0-false-false-false-false": "MLRS-0A\t\t\t\t",
        "Mlrs-RandomForest-2.0-true-true-true-false": "MLRS-pa\t\t\t\t",
        "Mlrs-RandomForest-2.0-true-true-true-true": "MLRS-cpA\t\t\t",

    }

    # [('BasicML',), ('BasicStereotype',), ('BetaReputation',), ('Blade-2',), ('Fire-0.0',),  ('FireLike',), ('FireLikeStereotype',), ('Habit-2',), ('Mlrs-J48-10-0.0',), ('Mlrs-J48-10-0.5',), ('Mlrs-J48-2-0.0',),
    # ('Mlrs-J48-2-0.5',), ('Mlrs-NaiveBayes-10-0.0',), ('Mlrs-NaiveBayes-10-0.5',), ('Mlrs-NaiveBayes-2-0.0',), ('Mlrs-NaiveBayes-2-0.5',), ('NoStrategy',), ('Travos',)]

    splt = split(results, "clientInvolvementLikelihood", "witnessRequestLikelihood", "memoryLimit", "numClients", "numProviders",
                 # "numSimCapabilities", 
                 "limitClientsUntilRound",
                 "numTerms", "numAdverts", "usePreferences", "noiseRange", "honestWitnessLikelihood")


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
    # exps = [e for e in exps if all(x in [0,0.5] for x in e)]
    # exps = [(0.5, 0.5, 0.0, 0.0, 0.0, 0.0, 0.0), (0.5, 0.0, 0.5, 0.0, 0.0, 0.0, 0.0), (0.5, 0.0, 0.0, 0.25, 0.25, 0.0, 0.0), (0.5, 0.0, 0.0, 0.0, 0.0, 0.25, 0.25)]
    exps = sorted(topsplt.keys(), key=lambda x: x[0])
    botspltkeys = ["exp"]

    means = []
    stds = []
    stderrs = []
    for topkey in exps:
        topval = topsplt[topkey]
        botsplt = split(topval, *botspltkeys)
        print botsplt.keys()
        print [len(botsplt[botkey]) for botkey in strategies]
        # del botsplt[botkey][0]['utilities_mean']
        # del botsplt[botkey][1]['utilities_mean']
        # del botsplt[botkey][0]['utilities_std']
        # del botsplt[botkey][1]['utilities_std']
        # print botsplt[botkey][0]
        # print botsplt[botkey][1]
        # del botsplt[botkey][0]
        mns = [botsplt[botkey][0][scorename + "_mean"] if botkey in botsplt else -9999 for botkey in strategies]
        sts = [botsplt[botkey][0][scorename + "_std"] if botkey in botsplt else -9999 for botkey in strategies]
        ses = [st / (float(botsplt[botkey][0]["iterations"]) ** 0.5) for st in sts]
        means.append(mns)
        stds.append(sts)
        stderrs.append(ses)

    meanis = [maxindices(mns) for mns in means]

    print str(index), exps
    for topi in xrange(0, len(strategies)):
        print strategynamelookup[str(strategies[topi])], "\t",
        for boti in xrange(0, len(exps)):
            if topi in meanis[boti]:
                justprint(".")
            else:
                justprint(" ")
            # sep = (stderrs[boti][topi]/means[boti][topi])*100
            sep = stderrs[boti][topi]
            print df.format(means[boti][topi]).zfill(6), "(" + df.format(sep).zfill(5) + ")",
            if boti < len(exps) - 1:
                print "\t",
        print
    print

    # print str(index), exps
    # for topi in xrange(0,len(strategies)):
    # 	print strategynamelookup[str(strategies[topi])], "\t",
    # 	for boti in xrange(0,len(exps)):
    # 		if topi in meanis[boti]:
    # 			justprint(".")
    # 		else:
    # 			justprint(" ")
    # 		# print df.format(stderrs[boti][topi]*1.96),
    # 		print df.format(means[boti][topi]), "("+df.format(stderrs[boti][topi]*1.96)+")",
    # 		if boti < len(exps)-1:
    # 			print "\t",
    # 	print
    # print

    for topi in xrange(0, len(strategies)):
        print strategynamelookup[str(strategies[topi])], "&",
        for boti in xrange(0, len(exps)):
            if topi in meanis[boti]:
                print "\\bf",
            # print df.format(stderrs[boti][topi]*1.96),
            print df.format(means[boti][topi]),  # "& ("+df.format(stderrs[boti][topi])+")",
            if boti < len(exps) - 1:
                print "&",
        print "\\\\"


        # print "\\begin{tikzpicture}"
        # print "\\begin{axis}[ybar=0pt,bar width=1pt, width=\\textwidth]\n"
        # for topi in xrange(0,len(exps)):
        # 	print "\\addplot coordinates {"
        # 	for boti in xrange(0,len(strategies)):
        # 		print "("+str(boti)+","+df.format(means[topi][boti])+") +- (0,"+df.format(stderrs[topi][boti])+")"
        # 	print "};\n"
        # print "\n\\legend{"+',''.join([str(s) for s in strategies])+"}"
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
