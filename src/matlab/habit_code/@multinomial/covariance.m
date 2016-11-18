% Returns the distribution covariance
function c = covariance(t)

mu = mean(t);

x = (t.v-mean(t)).^2

c = sum(x.*t.p);

