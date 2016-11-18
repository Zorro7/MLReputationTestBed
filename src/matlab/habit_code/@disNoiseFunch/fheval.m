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
%   Return independent value with probability noiseWeight
%******************************************************************************
noiseIndices = fh.noiseWeight > rand([numel(x) 1]);
y = zeros([size(x) 1]);
y(noiseIndices) = sample(fh.noiseDist,sum(noiseIndices));

%******************************************************************************
%   Otherwise return the true function value
%******************************************************************************
if isa(fh.mainFunc,'function_handle')
   y(~noiseIndices) = feval(fh.mainFunc,x(~noiseIndices));
else
   [y(~noiseIndices) fh] = fheval(fh.mainFunc,x(~noiseIndices));
end


