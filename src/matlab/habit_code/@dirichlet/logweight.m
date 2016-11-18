% DIRICHLET/LOGWEIGHT log weight for DP trust model
%
% Usage: lw = logweight(t1,t2) where t1 and t2 are two dirichlet models.
%
function lw = logweight(t1,t2)

%******************************************************************************
%   Check that the second parameter is also a Dirichlet model
%******************************************************************************
if ~isa(t2,'dirichlet')
   error('dirichlet/logweight: both arguments must be dirichlet objects');
end

%******************************************************************************
%   Calculate weight according to its definition in terms of the
%   multivariate beta function
%******************************************************************************
alpha1 = t1.a;
alpha2 = t2.a;

lw = lnbeta(alpha1+alpha2+1) - lnbeta(alpha1) - lnbeta(alpha2);

%******************************************************************************
%******************************************************************************
%   Sub function for calculating the log of the multivariate beta function
%******************************************************************************
%******************************************************************************
function b = lnbeta(alpha)

b = sum(gammaln(alpha)) - gammaln(sum(alpha));


