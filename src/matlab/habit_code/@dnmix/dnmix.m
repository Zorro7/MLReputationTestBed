% Constructor for Dirichlet-NIG Model
%
% Usage: t = dnmix(d,a,b,v,m) where d is dirichlet vector and 
% a,b,v,m are the NIG parameters. The latter are used as prior for
% all the NIG conditionals.
%
% or: t = dnmix; -> equivalent to n=dnmix([1 1],1,1,1,0)
%
% or: t = dnmix(n,m); -> n is size of discrete set
%                            m is number of normal dimensions
%
function t = dnmix(varargin)

%*************************************************************************
%    Set defaults
%*************************************************************************
if nargin>0
   t.d=dirchlet(varargin{1});
else
   t.d=dirchlet;
end

t.n=cell(1,dims(t.d));  
for i=1:numel(t.n)
   % generate with cholesky decompositions
   t.n{i} = chol(nig(varargin{2:end}));
end

   
%*************************************************************************
%   construct object
%*************************************************************************
t = class(t,'dnmix',distribution);

