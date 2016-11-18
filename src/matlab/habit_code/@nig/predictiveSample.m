% NIG/PREDICTIVESAMPLE Sample from conditional predictive distribution.
%
% Usage: samples = predictiveSample(model,observed,observedInd,noSamples)
%
% where observed is any values observed dimensions of the domain,
% observedInd is the indices corresponding to the observed values,
% noSamples is the required number of samples.
%
function samples = predictiveSample(model,observed,observedInd,noSamples)

%******************************************************************************
%   Observe no dimensions by default.
%******************************************************************************
if nargin<3
   observed = [];
   observedInd = [];
end

if nargin<4
   noSamples = size(observed,1);
end

%******************************************************************************
%   Check that there are the sample number of observed values as
%   observed indices
%******************************************************************************
if ~isequal(size(observed,2),numel(observedInd))
   error('There must be the same number of observed values and indices.');
end

%******************************************************************************
%   Generate the required number of parameter samples, and use them to
%   initialise the joint distribution object.
%******************************************************************************
paramSamples = sample(model,noSamples);
jointDistribution = multinormal(paramSamples);

%******************************************************************************
%   Generate the conditional distributions
%******************************************************************************
if ~isequal(0,numel(observed))
   
   conditionalDistributions = ...
      conditional(jointDistribution,observed,observedInd);
   
else
   
   conditionalDistributions = jointDistribution;
   
end

%******************************************************************************
%   Finally, sample from the conditional distributions
%******************************************************************************
samples = sample(conditionalDistributions);






