% Defines a distribution over utiliy functions.
% The distribution is defined by a gaussian process, with randomly
% generated hyperparameters.
%
% Usage: t = utilgen(covFunc,muDist,muParams,hyperDist,hyperParams)
%
% The mean of the gaussian process is a linear function.
% muDist and muParams specified the distribution and parameters used
% to generate the gradient and bias of the mean.
%
% covFunc is the gaussian covariance function used
%
% hyperDist and hyperParams specified the distribution and parameters
% used to generate random hyperparameters.
%
%
function t = utilgen(covFunc,muDist,muParams,hyperDist,hyperParams)

%*************************************************************************
%   construct object
%*************************************************************************
t.covFunc = covFunc;
t.muDist = muDist;
t.muParams = muParams;
t.hyperDist = hyperDist;
t.hyperParams = hyperParams;
t = class(t,'utilgen',distribution);

