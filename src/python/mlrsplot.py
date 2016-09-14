from analysis import *
from ploting import *

if __name__ == "__main__":

    filename = "../../results/long4.res"

    results = loadprocessed(filename)

    df = "{0:.1f}"
    strategies = [
        ('NoStrategy',),
        ('Fire-0.0',),
        ('Fire-0.5',), ('BetaReputation',),
        ('Travos',),
        ('Blade-2',), ('Habit-2',),
        ('Burnett',),
        # ('BasicML',),
        # ('FireLike',),
        # ('BasicContext',),
        # ('FireLikeContext',),
        # ('BasicStereotype',),
        # ('FireLikeStereotype',),
        ('Mlrs2-NaiveBayes-2.0-true-false',),
        ('Mlrs2-NaiveBayes-2.0-true-true',),
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
        "Fire-0.0": "Basic",
        "Fire-0.5": "FIRE",
        "BetaReputation": "BetaRep",
        "Travos": "TRAVOS",
        "Blade-2": "BLADE",
        "Habit-2": "HABIT",
        "BasicML": "Basic-ML",
        "FireLike": "FIRE-ML",
        "Burnett": "Burnett",
        "BasicContext": "Context-ML",
        "BasicStereotype": "Stereotype-ML",
        "FireLikeStereotype": "FIRE-Stereotype-ML",
        "FireLikeContext": "FIRE-Context-ML",
        "Mlrs2-NaiveBayes-2.0-true-false": "MLRS",
        "Mlrs2-NaiveBayes-2.0-true-true": "MLRS-Ads",
        'MlrsB-NB-2-5-0.6-2.0-true.res': "MLRS-B0.6",
        'MlrsB-NB-2-5-0.7-2.0-true.res': "MLRS-B0.7",
        'MlrsB-NB-2-5-0.8-2.0-true.res': "MLRS-B0.8",
        'MlrsB-NB-2-5-0.9-2.0-true.res': "MLRS-B0.9",
        'MlrsB2-NaiveBayes-records-2.0-2.0-true-true': "MLRS-2records",
        'MlrsB2-NaiveBayes-records-5.0-2.0-true-true': "MLRS-5records",
        'MlrsB2-NaiveBayes-records-10.0-2.0-true-true': "MLRS-10records",
        'MlrsB2-NaiveBayes-records-25.0-2.0-true-true': "MLRS-25records",
        'MlrsB2-NaiveBayes-records-50.0-2.0-true-true': "MLRS-50records",
        'MlrsB2-NaiveBayes-directRecords-2.0-2.0-true-true': "MLRS-2directRecords",
        'MlrsB2-NaiveBayes-directRecords-5.0-2.0-true-true': "MLRS-5directRecords",
        'MlrsB2-NaiveBayes-directRecords-10.0-2.0-true-true': "MLRS-10directRecords",
        'MlrsB2-NaiveBayes-round-10.0-2.0-true-true': "MLRS-10rounds\t",
        'MlrsB2-NaiveBayes-round-25.0-2.0-true-true': "MLRS-25rounds\t",
        'MlrsB2-NaiveBayes-round-50.0-2.0-true-true': "MLRS-50rounds\t",
        'MlrsB2-NaiveBayes-round-100.0-2.0-true-true': "MLRS-100rounds\t",
        'MlrsB2-NaiveBayes-round-250.0-2.0-true-true': "MLRS-250rounds\t",
        'MlrsB2-NaiveBayes-round-500.0-2.0-true-true': "MLRS-500rounds\t",
        'MlrsB2-NaiveBayes-round-750.0-2.0-true-true': "MLRS-750rounds\t",
        'MlrsB2-NaiveBayes-round-1000.0-2.0-true-true': "MLRS-1000rounds\t",
    }

    splt = split(results, "clientInvolvementLikelihood", "memoryLimit", "numClients", "numProviders",
                 "numSimCapabilities",
                 "numTerms", "numAdverts", "usePreferences", "honestWitnessLikelihood")

    index = (0.1, 100, 100, 100, 10, 3, 3, "true", 1)

    splt = splt[index]

    # print index, len(splt)

    expsplt = split(splt, "exp")

    texstr = latexheader([], ["spy"])

    texstr += tikzheader()
    texstr += "[spy using outlines={circle, magnification=3, connect spies}]\n"
    texstr += axisheader(
        # "cycle list name=mark list",
        # "cycle multi list={linestyles*\\nextlist red,blue,green}",
        # "cycle multi list={red,green,black,blue \\nextlist linestyles}",
        # "cycle list={{green,solid},{red,solid},{red,dashed},{red,dashed},{brown,solid},{brown,dashed},{brown,dotted},{black,solid},{black,dashed},{black,dotted}}",
        "cycle list={{red,dotted},{brown,dotted},{brown,dashed},{brown,solid},{brown,dashdotted},{red,dashed},{black,dotted},{black,dashdotted},{black,dashed},{black,solid}}",
        # "cycle multi list={mark list\\nextlist solid,dashed}",
        "legend columns=2",
        "legend style={at={(0.985,0.025)},anchor=south east,/tikz/column 2/.style={column sep=5pt,}}",
        xmin="0", xmax="2500",
        ymin="-300", ymax="2350",
        width="15cm",
        height="10cm",
        xlabel="\\textbf{Round}",
        ylabel="\\textbf{Utility}"
    )

    step = 10
    start = 0
    for strategy in strategies:
        texstr += plotheader("mark size=1.5")
        X = xrange(start, len(expsplt[strategy][0]["utilities_mean"]) + 1, step)
        Y = expsplt[strategy][0]["utilities_mean"][start::step]
        # start += step / len(strategies)
        texstr += coordinates(X, Y)
    # print len(expsplt[strategy]), strategy, len(expsplt[strategy][0]["utilities_mean"])

    texstr += "\\begin{scope}"
    texstr += "\\spy[black,size=5.5cm] on (2.15,1.25) in node [fill=white] at (3.1,5.25);"   
    texstr += "\\end{scope}"

    texstr += legend([strategynamelookup[s[0]] for s in strategies])

    texstr += axisfooter()
    texstr += tikzfooter()
    texstr += latexfooter()

    print texstr

# viewpdf(texstr)
