% dirichlet/marginalLogLikelihood
%
% Usage p = marginalLogLikelihood(model,data)
%
function p = marginalLogLikelihood(model,data)

if isempty(data)
   p=0;
   return;
end

updatedModel = observe(model,data);

priorLikely = logMarginal(model);
p = logMarginal(updatedModel);

% if the prior is improper, it can safely be ignored.
% infact doing so avoids any nasty limits.
if(isfinite(priorLikely))
   p = p - priorLikely;
end

%******************************************************************************
%******************************************************************************
%   Sub function for calculating the log of the multivariate beta function
%******************************************************************************
%******************************************************************************
function result = logMarginal(t)

p = length(t.m);

normalPart = p*(1-t.v)*log(pi)./2 - log(t.v)/2;

gammaPart = t.a*p*log(2)/2 + gammalnMultiDim(t.a,p) - t.a*log(det(t.b))/2;

result = normalPart + gammaPart;


