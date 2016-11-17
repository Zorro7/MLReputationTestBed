import __builtin__
import os
import sys
import util
from copy import copy


df = "{0:.3f}"

def parseline(line, expdic=None):
	splt = line.split(":")
        if expdic is None:
            res = {h:v for h,v in [x.split("=") for x in splt[0].split(",")]}
        else:
            res = copy(expdic)
            res["exp"] = splt[0]
        del res["strategy"]
	utilities = [float(x) for x in splt[1].split(",")]
        res["utility"] = utilities[-1]
        res["utility100"] = utilities[99] - utilities[0]
       # res["utility250"] = utilities[249] - utilities[0]
        #res["utility500"] = utilities[499] - utilities[0]
        res["gain100"] = utilities[-1] - utilities[-100]
        res["utilities"] = util.shortlist([df.format(u) for u in utilities])
	# res["utility"] = res["utilities"][-1]
	#res["utility"] = float(splt[1][splt[1].rindex(",")+1:])
	return res

def loadraw(filename):
	with open(filename) as resF:
            header = resF.readline()[:-1]
            if header.startswith("[") and header.endswith("]"):
                header = header.replace("[--","").replace("]","")
                expdic = {h:v for h,v in [x.split(" ") for x in header.split(" --")]}
            else:
                expdic = None
	    inresults = False
	    results = None
	    for line in resF:
	    	line = line[:-1]
	    	if not line: continue
	    	if inresults:
	    		if ":" not in line:
	    			inresults = False
	    		else:
	    			res = parseline(line, expdic)
	    			results.append(res)
	    	if line == "--- RESULTS ---":
	    		inresults = True
	    		results = []
	return results


if __name__ == "__main__":

	args = sys.argv[1:]

	dirname = args[0]
	results = []
	for path,dirs,files in os.walk(dirname):
		for filename in files:
			print filename, "...",
			fileres = loadraw(path+"/"+filename)
			if fileres is not None:
				results.extend(fileres)
				print "done."
			else:
				print "fail."

	for res in results:
		print util.shortdic(res)
