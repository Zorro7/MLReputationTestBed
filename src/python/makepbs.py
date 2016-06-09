import sys

def makecommands(base, args):
    if len(args) == 1:
        a,vs = args.iteritems().next()
        if not isinstance(vs, list):
            vs = [vs]
        return [base+" --"+a+" '"+str(v)+"'" for v in vs]
    else:
        a,vs = args.iteritems().next()
        if not isinstance(vs, list):
            vs = [vs]
        del args[a]
        return [cmd+" --"+a+" '"+str(v)+"'" for cmd in makecommands(base, args) for v in vs]
        # return [makecommands(base+" "+a+" "+str(v), args) for v in vs]

def makeoutput(cmd):
    return '_'.join([x for x,i in zip(cmd.split(" ")[5:],xrange(0,len(cmd.split(" ")))) if i % 2 == 0]).replace("weka.classifiers.", "")
