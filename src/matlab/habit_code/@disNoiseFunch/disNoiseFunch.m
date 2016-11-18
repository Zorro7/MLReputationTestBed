% TRAVOSC/DISNOISEFUNCH discrete function with discretly distributed noise
% Used to represent reputation sources and utility functions.
% Base implementation is N-degree polynomial.
%
% Usage: f = disNoiseFunch  - implements one-to-one mapping.
%
% f = disNoiseFunch(mainFunc,noise,noiseWeight)
%
% where mainFunc is the underlying (non-noisy) function; this can be a
% funch object or a standard function handle.
% noise is an independent noise distribution
% noiseWeight is the probability of sampling from the noise distribution
% rather than returning the bias mapping
%
% 
%
function f = disNoiseFunch(mainFunc,noise,noiseWeight)

%******************************************************************************
%   Set defaults
%******************************************************************************
if nargin < 1
   mainFunc = funch(0,1);
end

if nargin < 2
   noise = 0;
end

if nargin < 3
   noiseWeight = 0;
end

%******************************************************************************
%   Create object
%******************************************************************************
f.mainFunc = mainFunc;
f.noiseDist = noise;
f.noiseWeight = noiseWeight;
f = class(f,'disNoiseFunch',funch);

