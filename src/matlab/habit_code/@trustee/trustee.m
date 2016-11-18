% Constructor for trustee class
% Base implementation uses a mixture of pearson system distributions
% to generate its behaviour
%
% Usage: t = trustee(name,mix,mu,sigma,skew,kurt) 
%
% name -> string identifier for this trustee
% mix  -> mixture probabilities for mixture components
% mu
% 
%
function t = trustee(name,mix,mu,sigma,skew,kurt)

%*************************************************************************
%   Validate input and set defaults if necessary
%*************************************************************************
nargs=1;

if nargs > nargin
    name = 'standard trustee';
end

%*************************************************************************
%   construct object
%*************************************************************************
t.name = name;

t = class(t,'trustee');

