% Draws Samples from distribution
function s = sample(t,varargin)

%*************************************************************************
%   Wrapper for standard normal random number generator
%*************************************************************************
s = normrnd(0,1,varargin{:});
