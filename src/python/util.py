import __builtin__

def str(toprint, join='-'):
	if isinstance(toprint,tuple):
		return join.join([str(x) for x in toprint])
	return __builtin__.str(toprint)

def shortdic(dic):
	return ','.join([str(k)+"="+str(v) for k,v in dic.iteritems()])

def longdic(dic):
	return {x[:x.index("=")]:x[x.index("=")+1:] for x in dic.split(",")}

def shortlist(lst):
	return ";".join([str(v) for v in lst])

def longlist(lst):
	return [v for v in lst.split(";")]

def numbers(dic):
	for k,v in dic.iteritems():
		try:
			if isinstance(v, list):
				dic[k] = [float(x) for x in v]
			else:
				dic[k] = float(v)
		except:
			pass
	return dic

def lists(dic):
	for k,v in dic.iteritems():
		if ";" in v:
			dic[k] = longlist(v)
	return dic


def typeset(x):
	try:
		return int(x)
	except:
		pass
	try:
		return float(x)
	except:
		pass
	return str(x)


def maxindices(arr):
	mx = arr[0]
	mxi = [0]
	for i,a in zip(xrange(1,len(arr)+1),arr[1:]):
		if a > mx:
			mx = a
			mxi = [i]
		elif a == mx:
			mxi.append(i)
	return mxi
