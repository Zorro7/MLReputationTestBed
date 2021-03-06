import os


def tikzheader():
    return "\\begin{tikzpicture}\n"


def tikzfooter():
    return "\\end{tikzpicture}\n"


def kwargsaslines(header, footer, *args, **kwargs):
    lines = [header]
    for a in args:
        lines.append("\t" + str(a) + ",")
    for k, v in kwargs.iteritems():
        lines.append("\t" + str(k) + "=" + str(v) + ",")
    lines.append(footer)
    return '\n'.join(lines) + "\n"


def axisheader(*args, **kwargs):
    return kwargsaslines("\\begin{axis}[", "]", *args, **kwargs)


def axisfooter():
    return "\end{axis}\n"


def plotheader(*args, **kwargs):
    return kwargsaslines("\\addplot+[", "]", *args, **kwargs)


def coordinatesheader():
    return "coordinates {"


def coordinatesfooter():
    return "};\n"


def format(number, df):
    if isinstance(number, int):
        return str(number)
    else:
        return df.format(number)


def coordline(x, y, xerr=None, yerr=None, df="{0:.2f}"):
    line = "(" + format(x, df) + "," + format(y, df)
    if xerr is not None:
        line += ") +- (" + format(xerr, df) + ","
    elif yerr is not None:
        line += ") +- (0,"
    if yerr is not None:
        line += format(yerr, df)
    elif xerr is not None:
        line += "0"
    line += ")"
    return line


def coordinates(X, Y, Xerr=None, Yerr=None, df="{0:.2f}"):
    lines = [coordinatesheader()]
    if Xerr is None and Yerr is None:
        lines.extend([coordline(x, y, df=df) for x, y in zip(X, Y)])
    elif Xerr is not None:
        lines.extend([coordline(x, y, xerr=xerr, df=df) for x, y, xerr in zip(X, Y, Xerr)])
    elif Yerr is not None:
        lines.extend([coordline(x, y, yerr=yerr, df=df) for x, y, yerr in zip(X, Y, Yerr)])
    else:
        lines.extend([coordline(x, y, xerr=xerr, yerr=yerr, df=df) for x, y, xerr, yerr in zip(X, Y, Xerr, Yerr)])
    lines.append(coordinatesfooter())
    return '\n'.join(lines)


def legend(entries):
    line = "\\legend{"
    for x in entries:
        line += "" + str(x) + ","
    line = line[:-1]
    line += "}"
    return line


def latexheader(packages, tikzlibraries):
    lines = ["\\documentclass{standalone}"]
    lines.append("\\usepackage{pgfplots,amsmath}")
    lines.append("\\usepackage{color}")
    for package in packages:
        lines.append("\\usepackage{"+package+"}")
    for package in tikzlibraries:
        lines.append("\\usetikzlibrary{"+package+"}")
    lines.append("\\begin{document}")
    return '\n'.join(lines) + "\n"


def latexfooter():
    return "\\end{document}\n"


def viewpdf(texstr):
    import subprocess
    import tempfile
    import shutil
    current = os.getcwd()
    temp = tempfile.mkdtemp()
    os.chdir(temp)
    texF = open('plot.tex', 'w')
    texF.write(texstr)
    texF.close()
    p = subprocess.Popen('pdflatex plot.tex', shell=True)
    p.wait()
    p = subprocess.Popen(['open', 'plot.pdf'])
    p.wait()
    shutil.rmtree(temp)


if __name__ == "__main__":
    texstr = latexheader()
    texstr += tikzheader()
    texstr += axisheader(title="a title", xlabel="$X$", ylabel="$Y$")
    texstr += plotheader("blue", "mark size=1, error bars/.cd, y dir=both, y explicit")
    texstr += coordinates([1, 2, 3], [1, 15, 20], Yerr=[1, 2, 1.5])
    texstr += axisfooter()
    texstr += tikzfooter()
    texstr += latexfooter()

    print texstr
# viewpdf(texstr)
