from copy import copy

def makecommands(base, args):
	if len(args) == 1:
		ns,vs = args.iteritems().next()
		if not isinstance(vs, list):
			vs = [vs]
		return [base+" "+makearg(ns,v) for v in vs]
	else:
		ns,vs = args.iteritems().next()
		if not isinstance(vs, list):
			vs = [vs]
		del args[ns]
		return [cmd+" "+makearg(ns,v) for cmd in makecommands(base, args) for v in vs]

def makearg(ns, v):
	if isinstance(v, dict):
		return ''.join(makecommands("", copy(v)))[1:]
	elif isinstance(v, list):
		return ''.join(["--"+str(n)+" '"+str(vn)+"'" for n,vn in zip(ns.split(","),v)])
	else:
		return ' '.join(["--"+str(n)+" '"+str(v)+"'" for n in ns.split(",")])

def makeoutput(cmd):
	return '_'.join([x for x,i in zip(cmd.split(" ")[5:],xrange(0,len(cmd.split(" ")))) if i % 2 == 0]).replace("weka.classifiers.", "")
