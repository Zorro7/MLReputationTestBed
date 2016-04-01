import parse
import util


def search(results, **kwargs):
	ret = []
	singlekwargs = {k:v for k,v in kwargs.iteritems() if not isinstance(v,list) and not isinstance(v,set)}
	listkwargs = {k:set(v) for k,v in kwargs.iteritems() if k not in singlekwargs}
	for res in results:
		if all(res[key] == value for key,value in singlekwargs.iteritems()) and \
					all(res[key] in value for key,value in listkwargs.iteritems()):
			ret.append(res)
	return ret


def split(results, *keepsame):
	ret = {}
	for res in results:
		# newkey = '-'.join([str(res[k]) for k in keepsame])
		newkey = tuple(res[k] for k in keepsame)
		if newkey not in ret:
			ret[newkey] = []
		ret[newkey].append(res)
	return ret



def loadprocessed(filename):
	results = []
	with open(filename) as resF:
		for line in resF:
			results.append(util.longdic(line[:-1]))
	return results

if __name__ == "__main__":

	args = sys.argv[1:]

	filename = args[0]
	results = loadprocessed(filename)

	for res in results:
		print res
