% Draws Samples from dirichlet distribution
function [samples t] = sample(t,varargin)

%*************************************************************************
%   Default number of samples is one per distribution
%*************************************************************************
if nargin<2
   noSamples = 1;
else
   noSamples = varargin{1};
end

%*************************************************************************
%   First generate independent gamma samples
%*************************************************************************
samples = gamrnd(repmat(t.a,noSamples,1),1);

%*************************************************************************
%   Now divide each sample by its sum
%*************************************************************************
ss = repmat(sum(samples')',1,numel(t.a));

samples = samples./ss;




