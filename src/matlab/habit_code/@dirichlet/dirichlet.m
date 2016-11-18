% Constructor Dirichlet Model
%
% Usage: t = dirichlet(a) where a is parameter vector
%
% or: t = dirichlet; -> equivalent to n=dirichlet([1,1])
%
% or: t = dirichlet(n); -> n is number of dimensions
%
% or: t = dirichlet(...,d) d is the domain vector (default 1:n)
%
function t = dirichlet(a,d)

%*************************************************************************
%    Check for correct number of arguments
%*************************************************************************
if nargin>2
   error('Wrong number of arguments for dirichlet constructor');
end

%*************************************************************************
%   Initialise alpha parameter according to arguments
%*************************************************************************
if isequal(nargin,0)
   t.a=[1 1];

elseif isscalar(a)
   t.a = ones(1,a);
else
   t.a = a;
end

if any(t.a<0)
   error('Parameter vector must be positive');
end

%*************************************************************************
%   initialise domain according to arguments
%*************************************************************************
if nargin<2
   t.d = [1:numel(t.a)];
else
   if ~isequal(size(d),size(t.a))
      error('domain and parameter size mismatch');
   end
   t.d = d;
end

   
%*************************************************************************
%   construct object
%*************************************************************************
t = class(t,'dirichlet',distribution);






