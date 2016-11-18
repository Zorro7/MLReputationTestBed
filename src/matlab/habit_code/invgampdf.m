function p = invgampdf(x,a,b)

p = exp(log(b).*a - gammaln(a) - log(x).*(a+1) - b./x);

