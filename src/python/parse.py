


def search(results, **kwargs):
	ret = []
	singlekwargs = {k:v for k,v in kwargs.iteritems() if not isinstance(v,list) and not isinstance(v,set)}
	listkwargs = {k:set(v) for k,v in kwargs.iteritems() if k not in singlekwargs}
	for res in results:
		if all(res[key] == value for key,value in singlekwargs.iteritems()) and \
					all(res[key] in value for key,value in listkwargs.iteritems()):
			ret.append(res)
	return ret

def str(toprint, join='-'):
	if isinstance(toprint,tuple):
		return join.join([str(x) for x in toprint])
	return __builtin__.str(toprint)

def split(results, *keepsame):
	ret = {}
	for res in results:
		# newkey = '-'.join([str(res[k]) for k in keepsame])
		newkey = tuple(res[k] for k in keepsame)
		if newkey not in ret:
			ret[newkey] = []
		ret[newkey].append(res)
	return ret


def parseline(line):
	splt = line.split(":")
	res = {h:v for h,v in [x.split("=") for x in splt[0].split(",")]}
	res["utility"] = [float(x) for x in splt[1].split(",")]
	return res

def load(filename):
	with open(filename) as resF:
		inresults = False
		results = []
		for line in resF:
			line = line[:-1]
			if not line: continue
			if inresults:
				if ":" not in line:
					inresults = False
				else:
					res = parseline(line)
					results.append(res)
			if line == "--- RESULTS ---":
				inresults = True
	return results


if __name__ == "__main__":

	filename = "../../results/jobs.pbs.o35271-3705"

	results = load(filename)

	print len(results), results[0]
