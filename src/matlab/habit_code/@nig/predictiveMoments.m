% NIG/PREDICTIVESAMPLE Sample from conditional predictive distribution.
%
% Usage: [mu Sigma] = predictiveMoments(model,observed,observedInd)
%
% where observed is any values observed dimensions of the domain,
% observedInd is the indices corresponding to the observed values,
%
function [mu Sigma diagCorr] = predictiveMoments(t,observed,observedInd)

%******************************************************************************
%   Observe no dimensions by default.
%******************************************************************************
if nargin<2
   observed = [];
   observedInd = [];
end

%******************************************************************************
%   Check that there are the sample number of observed values as
%   observed indices
%******************************************************************************
if ~isequal(size(observed,2),numel(observedInd))
   error('There must be the same number of observed values and indices.');
end

%******************************************************************************
%   First, test alpha to see it can result in a valid expectation of the
%   covariance. If it can't, then we probably don't have enough data yet,
%   so the best we can do is return inf, to indicate that we can't make a
%   reasonable estimate.
%******************************************************************************
if 0 >= (t.a-numel(t.m)-1)
    mu = t.m';
    Sigma = inf;
    diagCorr = inf;
    return;
end

%******************************************************************************
%   Calculate the expected mean and covariance matrix and use them to
%   create the expected normal distribution
%******************************************************************************
tmpCov = t.b/(t.a-numel(t.m)-1);
tmpPrec = inv(tmpCov);
expPrecChol = cholcov(tmpPrec)';

% attempt cholesky decomposition if decomposition failed
if isequal(0,numel(expPrecChol))
   expPrecChol = chol(tmpPrec)';
end

meanParams = [t.m' vech(expPrecChol)'];
jointDistribution = multinormal(meanParams);

%******************************************************************************
% Figure out the diagnostic correlation
%******************************************************************************
diagCov = covariance(jointDistribution);
diagCorr = diagCov ./ (sqrt(diag(diagCov))*sqrt(diag(diagCov))');

if any(observedInd>dims(jointDistribution))
   error('multinormal/predictiveMoments: some indices are out of range');
end

%******************************************************************************
%   Generate the expected conditional distribution
%******************************************************************************
if ~isequal(0,numel(observed))
   
   conditionalDistributions = ...
      conditional(jointDistribution,observed,observedInd);
   
else
   
   conditionalDistributions = jointDistribution;
   
end

%******************************************************************************
%   Finally, the mean and variance of the predictive (multivariate t)
%   distribution is just the mean and variance of the expected normal
%   distribution.
%******************************************************************************
mu = mean(conditionalDistributions);
Sigma = covariance(conditionalDistributions);






