% Draws Samples from distribution
% Implemented from scratch because we want to generate
% Cholesky decompositions of precision matrix.
function [samples t] = sample(t,varargin)


% Error checking originally performed in wishrnd
if nargin<2
   noSamples = 1;
else
   noSamples = varargin{1};
end

noDiag    = size(t.b,1); % number of diagonal terms
noMus     = noDiag;
noOffDiag = (noDiag^2-noDiag)/2;

% Check alpha is valid
if t.a<noMus
    error('alpha parameter must be > no. dimensions');
end

% inverse beta parameter
binv = inv(t.b);

%**********************************************************************
%   Generate inv(b) cholesky first, if not done already
%**********************************************************************
if isequal(t.binvCholesky,-1)
   [t.binvCholesky,p] = cholcov(binv,1);
   if p~=0
      t.binvCholesky = chol(binv);
   end
end

%*************************************************************************
%   Generate each sample individually
%*************************************************************************
samples = zeros(noSamples,noMus+noDiag+noOffDiag);
for i=1:noSamples

   %**********************************************************************
   %   Generate Cholesky decomposition of precision
   %**********************************************************************
   L = wishcholrnd(binv,t.a,t.binvCholesky);
   
   covariance = inv(L*L');
   covdev = chol(covariance,'lower');

   %**********************************************************************
   %   Generate mean samples using inverse precision cholesky
   %**********************************************************************
   %Linv = inv(sqrt(t.v)*L);
   %m = Linv'*randn(noDiag,1)+t.m(:); % mean samples
   m = covdev*randn(noDiag,1)+t.m(:); % mean samples

   %**********************************************************************
   %  Place Samples where they belong
   %**********************************************************************
   samples(i,1:noDiag) = m;
   samples(i,(noDiag+1:end)) = vech(L);


end % sample loop


function x = wishcholrnd(sigma,df,d)
%WISHCHOLRND Generate Cholesky Decomposition of Wishart random matrix
%   Modified from WISHRND BY Luke Teacy
%   W=WISHCHOLRND(SIGMA,DF) generates the Cholesky decomposition of a
%   random matrix W having the Wishart
%   distribution with covariance matrix SIGMA and with DF degrees of
%   freedom.
%
%   W=WISHRND(SIGMA,DF,D) expects D to be the Cholesky factor of
%   SIGMA.  If you call WISHRND multiple times using the same value
%   of SIGMA, it's more efficient to supply D instead of computing
%   it each time.
%
%   [W,D]=WISHRND(SIGMA,DF) returns D so it can be used again in
%   future calls to WISHRND.
%
%   See also IWISHRND.

%   References:
%   Krzanowski, W.J. (1990), Principles of Multivariate Analysis, Oxford.
%   Smith, W.B., and R.R. Hocking (1972), "Wishart variate generator,"
%      Applied Statistics, v. 21, p. 341.  (Algorithm AS 53)

%   Copyright 1993-2006 The MathWorks, Inc. 
%   $Revision: 1.4.4.5 $  $Date: 2006/11/11 22:55:58 $

[n,m] = size(sigma);

% Use the Smith & Hocking procedure
% Load diagonal elements with square root of chi-square variates
a = diag(sqrt(chi2rnd(df-(0:n-1))));

% Load upper triangle with independent normal (0, 1) variates
a(itriu(n)) = randn(n*(n-1)/2,1);

% Desired matrix is D'(A'A)D
x = (a*d)';

% We now return x instead of a (Luke Teacy)
%a = x' * x;


% --------- get indices of upper triangle of p-by-p matrix
function d=itriu(p)

d=ones(p*(p-1)/2,1);
d(1+cumsum(0:p-2))=p+1:-1:3;
d = cumsum(d);
