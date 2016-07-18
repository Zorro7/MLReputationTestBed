from util import *
from analysis import *
from sys import argv

if __name__ == "__main__":

	if len(sys.argv) > 1:
		args = sys.argv[1:]
		filename = args[0]
	else:
		filename = "../../results/go5.4.res"

	results = loadprocessed(filename)

	aggkeys = ["gain100", "utility", "utility100", "utility250", "utility500"]

	header = results[0].keys()
	spltkeys = [h for h in header if h not in aggkeys]

	aggregated = []

	splt = split(results, *spltkeys)
	for key in splt.iterkeys():
		mn = findmean(splt[key], *aggkeys)
		st = findstd(splt[key], *aggkeys)
		aggregate = {k:mn[k] for k in spltkeys}
		for k in aggkeys:
			aggregate[k+"_mean"] = mn[k]
			aggregate[k+"_std"] = st[k]
		aggregate["iterations"] = len(splt[key])
		aggregated.append(aggregate)

	for res in aggregated:
		print util.shortdic(res)
