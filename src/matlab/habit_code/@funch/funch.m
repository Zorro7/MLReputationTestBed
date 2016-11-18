% TRAVOSC/FUNCH Base class for adaptive functions.
% Used to represent reputation sources and utility functions.
% Base implementation is N-degree polynomial, in which cross terms
% always have zero weight. That is, all terms have the form 'a*x.^n'.
%
% Usage: f = funch;   - implements one-to-one mapping.
%
% f = funch(a) - constant function (always return a)
%
% f = funch(a,b,c,..,'scalar') or
% f = funch(a,b,c,..) - implements polynomial mapping.
%
% output is a + b*x + c*x.^2 + ... 
%
% if the last argument is 'scalar' then the function expects scalar input.
% In this case, vectors are interpreted as multiple inputs rather than a
% single input vector.
% 
%
function f = funch(varargin);

%******************************************************************************
%   Set Defaults
%******************************************************************************
f.scalarFlag = false;
if isequal(nargin,0)
   f.w = {0 1};

%******************************************************************************
%    Here, we need to check the last argument to see if we should set
%    the scalar flag.
%******************************************************************************
else

   if isequal(varargin{end},'scalar')
      f.scalarFlag = true;
      f.w = varargin(1:(end-1));
   else 
      f.w = varargin;
   end

end

%******************************************************************************
%   Generate object
%******************************************************************************
f = class(f,'funch');

