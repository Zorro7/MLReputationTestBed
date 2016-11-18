% Evaluate adaptive function handle.
%
% Usage: [y fhu] = fheval(fh,x);
%
% x   - input
% y   - output
% fh  - function handle
% fhu - function handle with updated state information
%
function [y fh] = fheval(fh,x)

%******************************************************************************
%   Calculate mean value first
%******************************************************************************
[y fh.meanFunch] = fheval(fh.meanFunch,x);

%******************************************************************************
%   Get noise distribution, or noise samples, conditioned on mean points.
%******************************************************************************
noise = fh.condFunc(fh.noiseDist,x,y);

%******************************************************************************
%   Sample conditional noise if distribution is returned.
%   Here, we need to be smart about the number of samples we need, and
%   whether or not our function returns multidimensional output.
%   For the time being we'll assume i.i.d. noise is added to all outputs.
%******************************************************************************
if isa(noise,'distribution')

   y(:) = y(:) + sample(noise,numel(y));


%******************************************************************************
%   Otherwise, if we have numerical output, we assume that noise samples
%   have been returned.
%******************************************************************************
elseif isnumeric(noise)
   y = y + noise;

end
