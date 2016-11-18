% NIG/LOGWEIGHT log weight for NIG trust model
%
% Usage: lw = logweight(t1,t2) where t1 and t2 are two nig models.
%
function lw = logweight(t1,t2)

%******************************************************************************
%   Check that the second parameter is also a nig model
%******************************************************************************
if ~isa(t2,'nig')
   error('nig/logweight: both arguments must be nig objects');
end

%******************************************************************************
%  Generate the combined data model based on both sets of evidence.
%  Assuming we use the standard improper prior in at least one of the input
%  models, the prior beliefs will not be double counted. Even if they are,
%  it should matter for uncertain priors.
%******************************************************************************
a = t1.a + t2.a;
v = t1.v + t2.v;

if v>0
   m = (t1.v*t1.m + t2.v*t2.m)/v; 
else
   m = 0;
end

b = t1.b + t1.v*t1.m*t1.m' + ...
    t2.b + t2.v*t2.m*t2.m' - v*m*m';

combinedModel = nig(a,b,v,m);

%******************************************************************************
%   Calculate weight according to its definition in terms of the
%   marginal likelihoods
%******************************************************************************
lw = logMarginal(t1,t2,combinedModel);

%******************************************************************************
%******************************************************************************
%   Sub function for calculating the log marginal data likelihood
%******************************************************************************
%******************************************************************************
function result = logMarginal(s1,s2,t)

p = length(t.m); % no. dimensions;
n = t.v - s1.v - s2.v; % no. data points;

piPart = -n*p*log(2*pi)/2;

gammaPart = gammalnMultiDim(t.a/2,p) ...
   - gammalnMultiDim(s2.a/2,p) - gammalnMultiDim(s2.a/2,p);

betaPart = s1.a*log(det(s1.b))/2 + s2.a*log(det(s2.b))/2 ...
   - t.a*log(det(t.b))/2;

if ~isfinite(betaPart)
   betaPart = 0;
end

vPart = p*(log(s2.v) + log(s2.v) - log(t.v))/2;

result = piPart + gammaPart + betaPart + vPart;

if isnan(result)
   result = 0;
end


