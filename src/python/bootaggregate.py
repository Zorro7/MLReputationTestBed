from analysis import *
from util import *

if __name__ == "__main__":

    if len(sys.argv) > 1:
        args = sys.argv[1:]
        filename = args[0]

    results = loadprocessed(filename)

    iterationsWanted = 100
    aggkeys = ["utility", "utilities", "gain100", "utility100"]

    if "utilities" not in aggkeys:
        for res in results:
            del res["utilities"]

    header = results[0].keys()
    spltkeys = [h for h in header if h not in aggkeys]

    aggregated = []

    splt = split(results, *spltkeys)
    for key in splt.iterkeys():
        if len(splt[key]) > iterationsWanted:
            splt[key] = splt[key][:iterationsWanted]
        mn = findmean(splt[key], *aggkeys)
        st = findstd(splt[key], *aggkeys)
        aggregate = {k: mn[k] for k in spltkeys}
        for k in aggkeys:
            aggregate[k + "_mean"] = mn[k]
            aggregate[k + "_std"] = st[k]
        aggregate["iterations"] = len(splt[key])
        aggregated.append(aggregate)

    df = "{0:.3f}"
    for res in aggregated:
        if "utilities" in aggkeys:
            res["utilities_mean"] = shortlist([df.format(x) for x in res["utilities_mean"]])
            res["utilities_std"] = shortlist([df.format(x) for x in res["utilities_std"]])
        print util.shortdic(res)
