% Draws Samples from multinomial distribution
function [values t] = sample(t,varargin)

%*************************************************************************
%   Default number of samples is one per distribution
%*************************************************************************
if nargin<2
   noSamples = {1};
else
   noSamples = varargin;
end

%*************************************************************************
%   Collect number of samples arguments
%*************************************************************************
s = [noSamples{:}];
totalNoSamples = prod(s);

%*************************************************************************
%   From culumative distribution functions for each parameter vector
%*************************************************************************
cdf = cumsum(t.p');

%*************************************************************************
%   Generate required number of uniform random variables and repeat
%   along each cdf
%*************************************************************************
uniformSamples = rand([1 size(cdf,2) totalNoSamples]);
uniformSamples = repmat(uniformSamples,size(cdf,1),1);

cdf = repmat(cdf,[1 1 totalNoSamples]);

%*************************************************************************
%   Form domain index for each sample, by counting the number of steps
%   in the cumulative distribution function that are less that the
%   uniform random variable.
%*************************************************************************
indices = uniformSamples > cdf;
indices = sum(indices)+1;

%*************************************************************************
%   Retrieve values for domain
%*************************************************************************
values = t.v(indices);

%*************************************************************************
%   Reshape as desired (sample indices first, distribution index last,
%   squeeze out any leading singleton dimensions.
%*************************************************************************
values = permute(values,[3 2 1]);
values = reshape(values,[s size(t.p,1)]);

if isvector(values)
   values = reshape(values,[1,numel(values)]);
end







