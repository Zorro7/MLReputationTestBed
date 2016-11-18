% p = pdf(t,x)
% Returns the probability density of x
% This is a wrapper function for mvnpdf(x)
function p = pdf(t,x)

p = mvnpdf(x,mean(t),covariance(t));

