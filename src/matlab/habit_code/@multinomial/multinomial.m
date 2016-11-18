% Constructor Multinomial Distribution
%
% Usage: t = multinomial(p,v) where p is parameter vector
%                             and v is corresponding values]
%                             v is optional (default 1:n)
%
% or: t = multinomial; -> equivalent to n=multinomial(1,1)
%
% or: t = multinomial(n); -> n is number of dimensions
%
function t = multinomial(p,v)

%*************************************************************************
%    Set defaults
%*************************************************************************
if isequal(nargin,0)
   p=[0.5 0.5];
   v=[1 2];

elseif 2<nargin
   error('Wrong number of arguments for multinomial constructor');
end

if isscalar(p)
   t.p = ones(1,p)/p;
else
   t.p = p;
end

if any(t.p(:)<0) || any(t.p(:)>1) 
   error('p parameter vector must between 0 and 1 ');
end

if nargin<2
   t.v = 1:size(t.p,2);
else
   t.v = v;
end
   
%*************************************************************************
%   construct object
%*************************************************************************
t = class(t,'multinomial',distribution);

