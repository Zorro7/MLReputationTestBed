import __builtin__

def str(toprint, join='-'):
	if isinstance(toprint,tuple):
		return join.join([str(x) for x in toprint])
	return __builtin__.str(toprint)

def shortdic(dic):
	return ','.join([str(k)+"="+str(v) for k,v in dic.iteritems()])

def longdic(dic):
	return {x[:x.index("=")]:x[x.index("=")+1:] for x in dic.split(",")}

def numbers(dic):
	for k,v in dic.iteritems():
		try:
			dic[k] = float(v)
		except:
			pass
	return dic
