import __builtin__
import os
import sys
import util

def parseline(line):
	splt = line.split(":")
	res = {h:v for h,v in [x.split("=") for x in splt[0].split(",")]}
	# res["utilities"] = [float(x) for x in splt[1].split(",")]
	# res["utility"] = res["utilities"][-1]
	res["utility"] = float(splt[1][splt[1].rindex(",")+1:])
	return res

def loadraw(filename):
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

	args = sys.argv[1:]

	dirname = args[0]
	results = []
	for path,dirs,files in os.walk(dirname):
		for filename in files:
			print filename, "...",
			results.extend(loadraw(path+"/"+filename))
			print "done."

	for res in results:
		print util.shortdic(res)
