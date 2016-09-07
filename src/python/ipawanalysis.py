from analysis import *
from util import *


def splttostr(splt, reskey=None, depth = 0, lines=None):
	if lines == None:
		lines = []
	if isinstance(splt, dict):
		for k,v in sorted(splt.items()):
			line = ["\t" for i in xrange(depth)]
			if isinstance(v, dict):
				line.extend([k,":::"])
				line.extend(['\t'+str(k1) for k1 in sorted(v)])
				lines.append(line)
				lines.append(["\t" for i in xrange(depth)])
			splttostr(v, reskey=reskey, depth=depth+1, lines=lines)
		lines.append([])
	else:
		line = ["\t"+str(s) if reskey is None else "\t"+str(s[reskey]) for s in splt]
		lines[-1].extend(line)
	return '\n'.join([str(''.join([str(x) for x in line])) for line in lines])


def splitmany(splt, *splits):
	if len(splits) == 1:
		return {k: split(v, *splits[0]) for k,v in splt.iteritems()}
	else:
		level = {k: split(v, *splits[-1]) for k,v in splt.iteritems()}
		return {k: splitmany(v, *splits[:-1]) for k,v in level.iteritems()}

def aggregate(splt, funch, *meanof):
	if isinstance(splt, dict):
		return {k: aggregate(v, funch, *meanof) for k,v in splt.iteritems()}
	else:
		return [funch(splt, *meanof)]

if __name__ == "__main__":

	filename = "../../results/ipaw.res"
	results = loadprocessed(filename)

	# results = split(results, "numProviders", "memoryLimit", "defaultServiceDuration")
	# print results.keys()
	# splt = {"all": results[(100,500.0,5.0)]}
	# #["memoryLimit"], ["numProviders"],
	# splt = splitmany(splt, ["strategy"], ["eventLikelihood", "eventProportion", "eventDelay"])
	# mn = aggregate(splt, findmean, "utility")
	# sd = aggregate(splt, findstd, "utility")
	# sderr = aggregate(splt, findstderr, "utility")
	# print "---MEAN---"
	# print splttostr(mn, "utility")
	# print "---STD---"
	# print splttostr(sd, "utility")
	# print "---STDERR---"
	# print splttostr(sderr, "utility")


	df = "{0:.1f}"

	splt = split(results, "defaultServiceDuration")
	index = (5.0,)
	# splt = {"all": results}
	# index = "all"
	topspltkeys = ["numProviders", "memoryLimit", "defaultServiceDuration", "eventLikelihood", "eventProportion", "eventDelay"]
	botspltkeys = ["strategy"]
	scorename = "utility"
	topsplt = split(splt[index], *topspltkeys)
	for topkey,topval in sorted(topsplt.iteritems()):
		botsplt = split(topval, *botspltkeys)
		print topkey,
		for botkey,botval in sorted(botsplt.iteritems()):
			mn = findmean(botval, scorename)
			print "\t", df.format(mn[scorename]).zfill(5),
		print
	print sorted(botsplt.keys())





	# import anova
	# results, numreplicates, divs, measurement, nobs = None
	# divs = ["strategy", "defaultServiceDuration", "memoryLimit", "numProviders", "eventDelay", "eventLikelihood", "eventProportion"]
	# anova.nwayanova(results, 25, divs, "utility")
