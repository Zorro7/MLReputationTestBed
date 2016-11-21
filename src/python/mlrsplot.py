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
        ('Random.res',),
        ('DirectBRS.res',),
        ('DirectFire.res',),
        ('DirectML-RF-2.res',),
        ('DirectMLContext-RF-2.res',),
        ('DirectMLStereotype-RF-2.res',),
        # ('DirectMLStereotypeContracts-RF-2.res',),
        # ('MLRSDirect-2.res',),
        # ('MLRSDirectAds-2.res',),
        # ('MLRSDirectContracts-2.res',),
        ('Fire.res',),
        ('BRS.res',),
        ('TRAVOS.res',),
        ('BLADE-2.res',),
        ('HABIT-2.res',),
        # ('ML-RF-2.res',),
        # ('MLContext-RF-2.res',),
        # ('MLStereotype-RF-2.res',),
        # ('MLStereotyeContract-RF-2.res',),
        ('MLRS-2.res',),
        ('MLRSAds-2.res',),
        # ('MLRSContracts-2.res',),
        # ('MLRS-10.res',),
        # ('MLRS-3.res',),
        # ('MLRS-5.res',),
        # ('MLRSAds-10.res',),
        # ('MLRSAds-3.res',),
        # ('MLRSAds-5.res',),
        # ('MLRSContracts-10.res',),
        # ('MLRSContracts-3.res',),
        # ('MLRSContracts-5.res',),
        # ('MLRSDirect-10.res',),
        # ('MLRSDirect-3.res',),
        # ('MLRSDirect-5.res',),
        # ('MLRSDirectAds-10.res',),
        # ('MLRSDirectAds-3.res',),
        # ('MLRSDirectAds-5.res',),
        # ('MLRSDirectContracts-10.res',),
        # ('MLRSDirectContracts-3.res',),
        # ('MLRSDirectContracts-5.res',),
    ]

    

    splt = split(results, "memoryLimit", "numTerms", "witnessRequestLikelihood", "noiseRange",
                 "numPreferences", "numSimCapabilities", "providerAvailabilityLikelihood", "providerAttrition")

    print "%", splt.keys()
    # index = (0.1, 100, 20, 100, 10, 3, 3, "true", 1)
    index = tuple([typeset(x) for x in args])

    splt = splt[index]

    # print index, len(splt)

    expsplt = split(splt, "resname")
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

    texstr += legend([s[0] for s in strategies if s in expsplt])

    texstr += axisfooter()
    texstr += tikzfooter()
    texstr += latexfooter()

    print texstr

# viewpdf(texstr)
