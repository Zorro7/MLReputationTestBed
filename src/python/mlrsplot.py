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

    df = "{0:.5f}"
    strategies = [
    ('Random.res',),
    ('BRSDirect.res',),
    ('FireDirect.res',),
    ('BRS.res',),
    ('Fire.res',),
    ('TRAVOS.res',),
    ('BLADE-2.res',),
    ('HABIT-2.res',),
    ('MLRS-M5.res',),
    ('MLRS-NB2.res',),
    ('MLRS-RF2.res',),
    ]

    

    splt = split(results, "memoryLimit", "numTerms", "witnessesAvailable", "sigma",
                 "numPreferences", "numSimCapabilities", "providersAvailable", "providerAttrition")

    print "%", splt.keys()
    # index = (0.1, 100, 20, 100, 10, 3, 3, "true", 1)
    index = tuple([typeset(x) for x in args])

    splt = splt[index]

    # print index, len(splt)

    expsplt = split(splt, "resname")
    print "%", expsplt.keys()
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
        "legend style={at={(0.1,0.1)},anchor=north west,/tikz/column 2/.style={column sep=5pt,}}",
        xmin="0", xmax="500",
        ymin="0", ymax="1",
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
        texstr += coordinates(X, Y, df = df)
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
