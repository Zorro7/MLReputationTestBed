% FUNCH/ISLINEAR checks to see if this funch is linear
% Usage b = islinear(f);
%
function b = islinear(f)

b = length(f.w) < 3;

