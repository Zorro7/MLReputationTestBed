import itertools
import numpy as np
from copy import copy
from operator import mul
from scipy import stats


## Searches through results for the ones to plot.
def search(results, **kwargs):
    ret = {}
    for nr, res in results.iteritems():
        # print kwargs, nr, res
        if all(res[key] == value for key, value in kwargs.iteritems()):
            ret[nr] = res
    return ret


def split(results, *keepsame):
    if len(keepsame) == 0:
        return {(): [res for res in results]}
    ret = {}
    for res in results:
        # newkey = '-'.join([str(res[k]) for k in keepsame])
        newkey = tuple([str(res[k]) for k in keepsame])
        if newkey not in ret:
            ret[newkey] = []
        ret[newkey].append(res)
    return ret


def getobservations(results, column, **kwargs):
    subset = search(results, **kwargs)
    vals = []
    for nr, res in subset.iteritems():
        vals.append(res[column])
    return vals


def diff(results1, results2, maintitles=None):
    if maintitles is None:
        maintitles = []
    diffs = {}
    for nr in results1:
        # print nr, results1[nr], results2[nr]
        d = {}
        for k in results1[nr]:
            # print k, results1[nr][k], results2[nr][k]
            if k in maintitles:
                d[k] = results1[nr][k]
            else:
                try:
                    d[k] = results1[nr][k] - results2[nr][k]
                except:
                    d[k] = results1[nr][k]
        diffs[nr] = d
    return diffs


def findmean(results, *meanof):
    avg = copy(results[0])  ##init
    for m in meanof:
        avg[m] = 0
    for cand in results:  ##for all candidates
        for m in meanof:
            avg[m] += cand[m]
    for m in meanof:
        avg[m] /= float(len(results))
    return avg


def stars(pvalue):
    if pvalue <= 0.001:
        return "***"
    elif pvalue <= 0.01:
        return "**"
    elif pvalue <= 0.05:
        return "*"
    return ""


def powerset(iterable, maxlen=None, reverse=False):
    "powerset([1,2,3]) --> () (1,) (2,) (3,) (1,2) (1,3) (2,3) (1,2,3)"
    s = list(iterable)
    if maxlen is None:
        maxlen = len(s)
    if reverse:
        return itertools.chain.from_iterable(itertools.combinations(s, r) for r in reversed(range(maxlen + 1)))
    return itertools.chain.from_iterable(itertools.combinations(s, r) for r in range(maxlen + 1))


def asstrings(tupl):
    if len(tupl) == 0:
        return ""
    if len(tupl) == 1:
        return tupl[0]
    return tupl


def cumsum(means, div, others):
    # for d in powerset(divs, reverse=True):
    # 	if not all(x in div for x in d):
    # 		continue
    SS = 0.
    for value in means[div]:
        # print "\t", (d, value, means[d][value]),
        # print "\t", (div, value), " = ( "#, means[div][value])
        sepdivs = [k for k in div]
        sepvals = [v for v in value]
        # print sepdivs, sepvals
        tmp = 0.
        for subdiv, subval in zip(powerset(sepdivs), powerset(sepvals)):
            if len(subdiv) != len(subval):
                print "Something went terribly wrong. Don't call Phil."
                asdf
            if (len(subdiv) - len(div)) % 2 == 0:
                # print "\t\t + ", (subdiv, subval, means[subdiv][subval])
                tmp += means[subdiv][subval]
            else:
                # print "\t\t - ", (subdiv, subval, means[subdiv][subval])
                tmp -= means[subdiv][subval]
        SS += tmp ** 2
    # print "\t ) **2 = ", SS
    return SS


def variance(observations, mean):
    tot = 0.
    for value in observations:
        # print "\t", "+ (", value, "-", mean, ")**2"
        tot += (value - mean) ** 2
    return tot


def nwayanova(results, numreplicates, divs, measurement, nobs=None):
    divs = sorted(divs)
    # print divs
    observations = {}
    means = {}
    df = {}
    if nobs == None:
        nobs = len(results)
    for div in powerset(divs):
        # print div
        subset = split(results, *div)
        means[div] = {}
        observations[div] = {}
        df[div] = len(subset.keys())

        for value in subset.keys():
            # tmp =[s[measurement] for s in subset[value]]
            observations[div][value] = [s[measurement] for s in subset[value]]
            means[div][value] = findmean(subset[value], measurement)[measurement]
        # print "\t", value, len(observations[div][value]), means[div][value], observations[div][value]

    # print means
    # print nobs, df
    DF = {}
    SS = {}
    MS = {}
    for div in powerset(divs):
        others = tuple([d for d in divs if d not in div])
        # others = tuple(set(divs).difference(set(div)))
        # print div, others

        if len(div) == 0:
            SS[div] = variance(observations[div][div], means[div][div])
            DF[div] = nobs - reduce(mul, [df[(d,)] for d in divs], 1.)
        # print "SS[", div, "] = ", SS[div]
        else:
            SS[div] = cumsum(means, div, others) * df[others] * numreplicates
            DF[div] = reduce(mul, [df[(d,)] - 1 for d in div], 1.)
        # print "SS[", div, "] * ", df[others] * numreplicates, " = ", SS[div]
        # print "DF[", div, "] = ", DF[div]
        MS[div] = SS[div] / DF[div]
    # print "MS[", div, "] = ", MS[div]
    # print

    Fscore = {}
    for div in powerset(divs):
        Fscore[div] = MS[div] / MS[()]

    print "\n-- main output table --\n"
    print "DIV SS DF MS F P"
    for div in powerset(divs):
        Fscore = MS[div] / MS[()]
        Pvalue = 1.0 - stats.f.cdf(Fscore, DF[div], DF[()])
        print str(div).replace(" ", ""), SS[div], DF[div], MS[div], Fscore, Pvalue, stars(Pvalue)

    print "\n\nThe hypothesis tests below are taken from http://sites.stat.psu.edu/~jls/stat512/lectures/lec24.pdf, but I am unsure of the degrees of freedom. \nREAD: DO NOT TRUST THEM.\n"
    ##factor B has no effect whatsoever at eny combination of levels of A and C,"
    for div in divs:
        others = others = tuple([d for d in divs if d not in div])
        print "Factor", div, "has no effect whatsoever at any combination of levels of", others,
        SSsum = 0.
        DFsum = 0.
        # print
        for d in powerset(divs):
            if div in d:
                # print "+", (d, SS[d], DF[d])
                SSsum += SS[d]
                DFsum += DF[d]
        # print "=", SSsum /  DFsum
        Fscore = (SSsum / DFsum) / MS[()]
        Pvalue = 1.0 - stats.f.cdf(Fscore, DFsum, DF[()])
        print "DF:", DFsum, "F:", Fscore, "P:", Pvalue, stars(Pvalue)

    print "Hypothesis of additivity",
    SSsum = 0.
    DFsum = 0.
    # print
    for d in powerset(divs):
        if len(d) >= 2:
            # print "+", (d, SS[d], DF[d])
            SSsum += SS[d]
            DFsum += DF[d]
        # print "=", SSsum
    Fscore = (SSsum / DFsum) / MS[()]
    Pvalue = 1.0 - stats.f.cdf(Fscore, DFsum, DF[()])
    print "DF:", DFsum, "F:", Fscore, "P:", Pvalue, stars(Pvalue)


def twowayanova(groups):
    ncols = len(groups)  # a
    nrows = len(groups[0])  # b
    ninner = len(groups[0][0])  # r
    print ncols, "x", nrows, "x", ninner
    means = [[np.mean(groups[a][b]) for b in xrange(nrows)] for a in xrange(ncols)]
    print "means", means
    colmeans = [np.mean(means[a]) for a in xrange(ncols)]
    print "colmeans", colmeans
    rowmeans = [np.mean([means[a][b] for a in xrange(ncols)]) for b in xrange(nrows)]
    print "rowmeans", rowmeans
    overallmean = np.mean(rowmeans)
    print "overallmean", overallmean

    SSwithin = 0.
    for col in xrange(ncols):
        for row in xrange(nrows):
            for i in xrange(ninner):
                SSwithin += (groups[col][row][i] - means[col][row]) ** 2
    print "SSwithin", SSwithin
    DFwithin = (ninner - 1.) * ncols * nrows
    print "DFwithin", DFwithin
    MSwithin = SSwithin / DFwithin
    print "MSwithin", MSwithin

    SScol = 0.
    for col in xrange(ncols):
        SScol += (colmeans[col] - overallmean) ** 2
    SScol *= ninner * nrows
    print "SScol", SScol
    DFcol = ncols - 1.
    print "DFcol", DFcol
    MScol = SScol / DFcol
    print "MScol", MScol

    SSrow = 0.
    for row in xrange(nrows):
        SSrow += (rowmeans[row] - overallmean) ** 2
    SSrow *= ninner * ncols
    print "SSrow", SSrow
    DFrow = nrows - 1.
    print "DFrow", DFrow
    MSrow = SSrow / DFrow
    print "MScol", MSrow

    SSinteraction = 0.
    for col in xrange(ncols):
        for row in xrange(nrows):
            SSinteraction += (means[col][row] - colmeans[col] - rowmeans[row] + overallmean) ** 2
    SSinteraction *= ninner
    print "SSinteraction", SSinteraction
    DFinteraction = (ncols - 1.) * (nrows - 1.)
    print "DFinteraction", DFinteraction
    MSinteraction = SSinteraction / DFinteraction
    print "MSinteraction", MSinteraction

    print "F-test:::::"
    print "MScol / MSwithin", MScol / MSwithin
    print "MSrow / MSwithin", MSrow / MSwithin
    print "MSinteraction / MSwithin", MSinteraction / MSwithin

    Pcols = 1.0 - stats.f.cdf(MScol / MSwithin, DFcol, DFwithin)
    Prows = 1.0 - stats.f.cdf(MSrow / MSwithin, DFrow, DFwithin)
    Pinteraction = 1.0 - stats.f.cdf(MSinteraction / MSwithin, DFinteraction, DFwithin)

    print "Pcols", Pcols, stars(Pcols)
    print "Prows", Prows, stars(Prows)
    print "Pinteraction", Pinteraction, stars(Pinteraction)
