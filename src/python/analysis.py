import sys
from copy import copy
from operator import *

import util


def search(results, **kwargs):
    ret = []
    singlekwargs = {k: v for k, v in kwargs.iteritems() if not isinstance(v, list) and not isinstance(v, set)}
    listkwargs = {k: set(v) for k, v in kwargs.iteritems() if k not in singlekwargs}
    for res in results:
        if all(res[key] == value for key, value in singlekwargs.iteritems()) and \
                all(res[key] in value for key, value in listkwargs.iteritems()):
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


## Orderings is a list of tuples. The mapping should be from measurement_name -> comparison_operator (le, ge, lt, gt)
## An ordered dict may be appropriate?
## returns a.compare(b), True if a *comp* b, False otherwise.
def compare(a, b, ndp, *orderings):
    for o, c in orderings:
        if c not in [le, ge, lt, gt]:
            print "That is not a comparison operator", orderings
            asdf
            ##if for all orderings a are 'better' than we have a winner (a)
            # c(a, b) is equivalent to a *c* b
    # print [(o,":",round(a[o],ndp),c.__name__,round(b[o],ndp),c(a[o], b[o])) for o,c in orderings]
    for o, c in orderings:
        if c(round(a[o], ndp), round(b[o], ndp)):
            return True
        elif round(a[o], ndp) != round(b[o], ndp):
            break
    return False


def findbest(results, ndp, *orderings):
    mx = results[0]  ##init
    for cand in results:  ##for all candidates
        if compare(cand, mx, ndp, *orderings):
            mx = cand
    return mx


def findmean(results, *meanof):
    avg = copy(results[0])  ##init
    for m in meanof:
        avg[m] = 0
    for cand in results:  ##for all candidates
        for m in meanof:
            avg[m] = calc(avg[m], cand[m], add)
    for m in meanof:
        avg[m] = calc(avg[m], float(len(results)), div)
    return avg


def calc(a, b, op):
    if isinstance(a, list) and isinstance(b, list):
        return [op(x, y) for x, y in zip(a, b)]
    elif isinstance(a, list):
        return [op(x, b) for x in a]
    elif isinstance(b, list):
        return [op(a, y) for y in b]
    else:
        return op(a, b)


def findstd(results, *stdof):
    avg = findmean(results, *stdof)  ##init
    std = copy(results[0])
    for m in stdof:
        std[m] = 0
    for cand in results:  ##for all candidates
        for m in stdof:
            std[m] = calc(std[m], calc(calc(cand[m], avg[m], sub), 2, pow), add)
    for m in stdof:
        std[m] = calc(calc(std[m], float(len(results)), div), 0.5, pow)
    return std


def findstderr(results, *stdof):
    stderr = findstd(results, *stdof)
    for m in stdof:
        stderr[m] = stderr[m] / (float(len(results)) ** 0.5)
    return stderr


def loadprocessed(filename, num=-1):
    results = []
    with open(filename) as resF:
        for line in resF:
            results.append(util.numbers(util.lists(util.longdic(line[:-1]))))
            num -= 1
            if num == 0:
                return results
    return results


if __name__ == "__main__":

    args = sys.argv[1:]

    filename = args[0]
    results = loadprocessed(filename)

    for res in results:
        print res
