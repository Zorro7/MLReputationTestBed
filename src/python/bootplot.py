from analysis import *
from ploting import *
from util import typeset
import sys


def justprint(s):
    sys.stdout.write(s)
    sys.stdout.flush()

def tabout(s, maxlen):
	return s+" "*(maxlen-len(s))


if __name__ == "__main__":


	df = "{0:.4f}"

	args = sys.argv[1:]
	filename = args[0]
	index = tuple([typeset(a.replace(",", "")) for a in args[1:]])


	results = loadprocessed(filename)


	strategies = [('Random.res',),
				  ('DirectBRS.res',),
  				  # ('WitnessBRS.res',),
  				  ('DirectStereotypePrivateIds.res',),
				  # ('DirectStereotype.res',),
				  # ('WitnessStereotypeAssessObs.res',),
				  # ('WitnessStereotypeFullObs.res',),
				  # ('WitnessStereotypeInteractObs.res',),
				  ('WitnessStereotypePrivateIds.res',),
				  # ('TransWitnessStereotypeAssessObs.res',),
				  # ('TransWitnessStereotypeFullObs.res',),
				  # ('TransWitnessStereotypeInteractObs.res',),
				  ('TransWitnessStereotypePrivateIdsAssessObs.res',),
				  ('TransWitnessStereotypePrivateIdsInteractObs.res',),
				]

	spltkeys = ['observability',
				'subjectivity',
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

	expsplt = split(splt[index], "resname")
	print "%", [len(expsplt[strategy]) for strategy in strategies if strategy in expsplt]


	texstr = latexheader([], [])

	texstr += tikzheader()
	texstr += axisheader(
		"cycle list name=color list",
		# "cycle multi list={linestyles*\\nextlist red,blue,green}",
		# "cycle multi list={red,green,black,blue \\nextlist linestyles}",
		# "cycle list={{green,solid},{red,solid},{red,dashed},{red,dashed},{brown,solid},{brown,dashed},{brown,dotted},{black,solid},{black,dashed},{black,dotted}}",
		# "cycle list={{red,dotted},{brown,dotted},{brown,dashed},{brown,solid},{brown,dashdotted},{red,dashed},{black,dotted},{black,dashdotted},{black,dashed},{black,solid}}",
		# "cycle multi list={mark list\\nextlist solid,dashed}",
		"legend columns=2",
		"legend style={at={(0.0,0.1)},anchor=south west,/tikz/column 2/.style={column sep=5pt,}}",
		xmin="0", xmax="250",
		ymin="0", ymax="1",
		width="15cm",
		height="10cm",
		xlabel="\\textbf{Round}",
		ylabel="\\textbf{Utility}",
		title='-'.join([str(x) for x in index])
	)

	step = 5
	start = 0
	for strategy in strategies:
		if strategy not in expsplt:
			continue
		texstr += plotheader("mark size=1.5")
		X = xrange(start, len(expsplt[strategy][0]["utilities_mean"]) + 1, step)
		Y = expsplt[strategy][0]["utilities_mean"][start::step]
		# start += step / len(strategies)
		texstr += coordinates(X, Y, df="{0:.4f}")
	# print len(expsplt[strategy]), strategy, len(expsplt[strategy][0]["utilities_mean"])

	texstr += legend([str(s[0]) for s in strategies if s in expsplt])

	texstr += axisfooter()
	texstr += tikzfooter()
	texstr += latexfooter()

	print texstr
