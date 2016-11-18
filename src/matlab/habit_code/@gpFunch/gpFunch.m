% TRAVOSC/GPFUNCH Gaussian Process Function
% Used to represent reputation sources and utility functions.
% Base implementation is N-degree polynomial.
%
% Usage: f = gpFunch  - implements one-to-one mapping.
%
% f = funch(m,covf,loghyper)
%
% 
%
function f = gpFunch(m,covf,loghyper)

%******************************************************************************
%   Set defaults
%******************************************************************************
if nargin < 1
   m = funch(0,1);
end

if nargin < 2
   covf = 'covSEiso';
end

if nargin < 3
   loghyper = log([1 1]);
end

%******************************************************************************
%   Create object
%******************************************************************************
f.covf = covf;
f.loghyper = loghyper;
f.prevX = [];
f.prevY = [];
f.m = m;
f = class(f,'gpFunch',funch);

