from analysis import *
from ploting import *
from util import typeset
import sys

if __name__ == "__main__":


    args = sys.argv[1:]
    files = args[:args.index(':')]
    args = args[args.index(':')+1:]
    results = []
    for filename in files:
        results.extend(loadprocessed(filename))

    df = "{0:.1f}"
    strategies = [
        # ('NoStrategy',),
        # ('Fire-0.0-false',),
        # ('Fire-0.5-false',), 
        # ('BetaReputation-0.0',), 
        # ('BetaReputation-0.5',),  
        ('Travos',),
        ('Blade-2',), 
        ('Habit-2',),
        ('Blade-3',), 
        ('Habit-3',),
        ('Blade-5',), 
        ('Habit-5',),
        ('Blade-10',), 
        ('Habit-10',),
        # ('Burnett',),
        # ('BasicML',),
        # ('FireLike',),
        # ('BasicContext-RandomForest-false',),
        # # ('BasicContext-RandomForest-true',),
        # ('FireLikeContext-RandomForest-false',),
        # # ('FireLikeContext-RandomForest-true',),
        # ('BasicStereotype',),
        # ('FireLikeStereotype',),
        # ('Mlrs-RandomForest-2.0-false-false-true-true',),
        # ('Mlrs-RandomForest-2.0-true-false-true-true',),
        # ('Mlrs-RandomForest-2.0-false-true-true-true',),
        # ('Mlrs-RandomForest-0.0-false-false-false-false',),
        # ('Mlrs-RandomForest-0.0-false-false-true-true',),
        ('Mlrs-RandomForest-2.0-false-false-false-false',),
        # ('Mlrs-RandomForest-2.0-true-true-false-false',),
         ('Mlrs-RandomForest-2.0-false-false-true-true',),
        # ('Mlrs-RandomForest-2.0-true-false-false-false',),
        # ('Mlrs-RandomForest-2.0-false-true-false-false',),
        #  ('Mlrs-RandomForest-5.0-false-false-false-false',),
        #  ('Mlrs-RandomForest-5.0-false-false-true-true',),
        # ('Mlrs-RandomForest-10.0-false-false-false-false',),
        #  ('Mlrs-RandomForest-10.0-false-false-true-true',),


        # ('Mlrs-RandomForest-0.0-true-false-false-false',),
        # ('Mlrs-RandomForest-0.0-false-true-false-false',),
        #  ('Mlrs-RandomForest-0.0-true-true-false-false',),
        # ('Mlrs2-NaiveBayes-2.0-false-false-false',),
        # ('MlrsB2-NaiveBayes-round-25.0-2.0-true',),
        # ('MlrsB2-NaiveBayes-round-50.0-2.0-true',),
        # ('MlrsB2-NaiveBayes-round-100.0-2.0-true',),
        # ('MlrsB2-NaiveBayes-round-250.0-2.0-true-true',),
        # ('MlrsB2-NaiveBayes-round-500.0-2.0-true-true',),
        # # # ('MlrsB2-NaiveBayes-round-750.0-2.0-true',),
        # ('MlrsB2-NaiveBayes-round-1000.0-2.0-true-true',)
    ]

    strategynamelookup = {
        "NoStrategy": "RAND",
        "Fire-0.0-false": "Basic",
        "Fire-0.5-false": "FIRE",
        "BetaReputation-0.0": "BasicBeta",
        "BetaReputation-0.5": "BetaRep",
        "Travos": "TRAVOS",
        "Blade-2": "BLADE",
        "Habit-2": "HABIT",
        "Blade-3": "BLADE-3",
        "Habit-3": "HABIT-3",
        "Blade-5": "BLADE-5",
        "Habit-5": "HABIT-5",
        "Blade-10": "BLADE-10",
        "Habit-10": "HABIT-10",
        "BasicML": "Basic-ML",
        "FireLike": "FIRE-ML",
        "Burnett": "Burnett",
        "BasicContext-RandomForest-false": "Context-ML",
        "BasicContext-RandomForest-true": "PayloadContext-ML",
        "BasicStereotype": "Stereotype-ML",
        "FireLikeStereotype": "FIRE-Stereotype-ML",
        "FireLikeContext-RandomForest-false": "FIRE-Context-ML",
        "FireLikeContext-RandomForest-true": "FIRE-PayloadContext-ML",
        "Mlrs-RandomForest-2.0-false-false-false-false": "MLRS",
        "Mlrs-RandomForest-2.0-true-false-false-false": "MLRS-c",
        "Mlrs-RandomForest-2.0-false-true-false-false": "MLRS-p",
        "Mlrs-RandomForest-2.0-true-true-false-false": "MLRS-cp",
        "Mlrs-RandomForest-0.0-false-false-false-false": "MLRS-0",
        "Mlrs-RandomForest-0.0-true-false-false-false": "MLRS-0c",
        "Mlrs-RandomForest-0.0-false-true-false-false": "MLRS-0p",
        "Mlrs-RandomForest-0.0-true-true-false-false": "MLRS-0cp",
        "Mlrs-RandomForest-2.0-false-false-true-true": "MLRS-a",
        "Mlrs-RandomForest-2.0-true-false-true-true": "MLRS-ca",
        "Mlrs-RandomForest-2.0-false-true-true-true": "MLRS-pa",
        "Mlrs-RandomForest-2.0-true-true-true-true": "MLRS-cpa",
        "Mlrs-RandomForest-0.0-false-false-true-true": "MLRS-0a",
        "Mlrs-RandomForest-0.0-true-false-true-true": "MLRS-0ca",
        "Mlrs-RandomForest-0.0-false-true-true-true": "MLRS-0pa",
        "Mlrs-RandomForest-0.0-true-true-true-true": "MLRS-0cpa",
    }

    splt = split(results, "memoryLimit", "numClients", "numProviders",
                 "numTerms", "witnessRequestLikelihood", "noiseRange", 
                 "numPreferences", "numSimCapabilities", "providerAvailabilityLikelihood")

    print "%", splt.keys()
    # index = (0.1, 100, 20, 100, 10, 3, 3, "true", 1)
    index = tuple([typeset(x) for x in args])

    splt = splt[index]

    # print index, len(splt)

    expsplt = split(splt, "exp")
    print "%", [len(expsplt[strategy]) for strategy in strategies if strategy in expsplt] 


    texstr = latexheader([], ["spy"])

    texstr += tikzheader()
    texstr += "[spy using outlines={circle, magnification=3, connect spies}]\n"
    texstr += axisheader(
        # "cycle list name=mark list",
        # "cycle multi list={linestyles*\\nextlist red,blue,green}",
        # "cycle multi list={red,green,black,blue \\nextlist linestyles}",
        # "cycle list={{green,solid},{red,solid},{red,dashed},{red,dashed},{brown,solid},{brown,dashed},{brown,dotted},{black,solid},{black,dashed},{black,dotted}}",
        "cycle list={{red,solid},{brown,dotted},{brown,dashed},{brown,solid},{brown,dashdotted},{red,dashed},{black,dotted},{black,dashdotted},{red,dashdotted},{red,dotted},{black,dashed},{black,solid}}",
        # "cycle multi list={mark list\\nextlist solid,dashed}",
        "title="+('-'.join([str(x) for x in index])),
        "legend columns=2",
        "legend style={at={(0.1,0.9)},anchor=north west,/tikz/column 2/.style={column sep=5pt,}}",
        xmin="0", xmax="1000",
        ymin="-1000", ymax="2500",
        width="15cm",
        height="10cm",
        xlabel="\\textbf{Round}",
        ylabel="\\textbf{Utility}"
    )

    step = 10
    start = 0
    for strategy in strategies:
        if strategy not in expsplt:
            continue
        texstr += plotheader("mark size=1.5")
        X = xrange(start, len(expsplt[strategy][0]["utilities_mean"]) + 1, step)
        Y = expsplt[strategy][0]["utilities_mean"][start::step]
        # start += step / len(strategies)
        texstr += coordinates(X, Y)
    # print len(expsplt[strategy]), strategy, len(expsplt[strategy][0]["utilities_mean"])

    # texstr += "\\begin{scope}"
    # texstr += "\\spy[black,size=5.5cm] on (2.15,1.25) in node [fill=white] at (3.1,5.25);"   
    # texstr += "\\end{scope}"

    texstr += legend([strategynamelookup[s[0]] for s in strategies if s in expsplt])

    texstr += axisfooter()
    texstr += tikzfooter()
    texstr += latexfooter()

    print texstr

# viewpdf(texstr)
