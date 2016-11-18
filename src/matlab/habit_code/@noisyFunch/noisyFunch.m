% TRAVOSC/NOISYFUNCH Adaptive Function handle with noise
% Used to represent reputation sources and utility functions.
%
% Usage: f = noisyFunch; - implements one-to-one mapping.
%
% f = noisyFunch(meanFunch,noiseDist,condFunc)
%
% meanFunc  - function providing mean.
% noiseDist - distribution object from which to sample noise
% condFunc  - (optional) regular function handle of form:
%
% condNoiseDist = condFunc(noiseDist,x,y);
%
% returns noise distribution conditioned on requested values.
% 
%
function f = noisyFunch(meanFunch,noiseDist,condFunc);

%******************************************************************************
%   Set defaults
%******************************************************************************
if nargin < 1
   meanFunch = funch;
end

if nargin < 2;
   noiseDist = 0;
end

if nargin < 3
   condFunc = @(noiseDist,x,y) noiseDist;
end

%******************************************************************************
%   Generate Object
%******************************************************************************
f.meanFunch = meanFunch;
f.noiseDist = noiseDist;
f.condFunc = condFunc;

f = class(f,'noisyFunch',funch);

