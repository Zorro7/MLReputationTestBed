% CORRECT NORMAL MARGINAL DATA LIKELIHOOD
function result = gausLogMarginalDataLikelihood(prior,data)

s = prior;
t = observe(prior,data);

p = length(t.m); % no. dimensions;
n = t.v-s.v; % no. data points;

piPart = -n*p*log(2*pi)/2;

gammaPart = gammalnMultiDim(t.a/2,p) - gammalnMultiDim(s.a/2,p);

betaPart = s.a*log(det(s.b))/2 - t.a*log(det(t.b))/2;

if ~isfinite(betaPart)
   betaPart = 0;
end

vPart = p*(log(s.v) - log(t.v))/2;

result = piPart + gammaPart + betaPart + vPart;

if isnan(result)
   result = -inf;
end

