% TRUSTER/euEstimate
%
% Usage: [m e] = euEstimate(t,trustee,utilFh);
%
function [dirM dirVar] = euEstimate(t,trustee,utilFh)

%******************************************************************************
%   Retrieve appropriate direct observation model
%******************************************************************************
model = t.directModels{trustee};
distMu = mean(model);
distCov = covariance(model);

%******************************************************************************
%   Calculate utility for each element of domain
%******************************************************************************
utilDomain = fheval(utilFh,t.dirModelPrior.d);

%******************************************************************************
%   Calculate expected utility using the predictive distribution
%******************************************************************************
dirM = sum( utilDomain .* distMu );

%******************************************************************************
%   Calculate expected utility variance according to definition
%******************************************************************************
squUtil = utilDomain' * utilDomain;
utilCov = squUtil .* distCov;
dirVar = sum(utilCov(:));




