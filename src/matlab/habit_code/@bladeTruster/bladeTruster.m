% BLADETRUSTER/BLADETRUSTER Implementation of Kevin Regan's Blade Model
%
%  Usage: t = bladeTruster(s); or t = bladeTruster;
%
%  If s is provided, it is a structure containing any initial values
%  for the class instance variables detailed below. Note that inital
%  values for directModels and repModels are always ignored.
%
%   instance variables :
%
%    noTrustees : initially assumed number of trustees
%    noSources : initially assumed number of reputation sources
%
%    dirDims : number of behaviour dimensions
%    repDims : number of reputation dimensions
%
function t = bladeTruster(s)

%******************************************************************************
%   Initial input structure if none provided
%******************************************************************************
if isequal(nargin,0)
   s = struct;
end
t = struct;

%******************************************************************************
%   Set default number of trustees and number of sources
%******************************************************************************
if ~isfield(s,'noSources')
   t.noSources = 1;
else
   t.noSources = s.noSources;
end

if ~isfield(s,'noTrustees')
   t.noTrustees = 1;
else
   t.noTrustees = s.noTrustees;
end

%******************************************************************************
%   Set default belief model priors if none provided
%******************************************************************************
if ~isfield(s,'dirDims')
   s.dirDims = 3;
end

if ~isfield(s,'repDims')
   s.repDims = 3;
end

t.dirModelPrior = dirichlet(s.dirDims);
t.repModelPrior = dirichlet(s.repDims);

%******************************************************************************
%   Initialise direct and reputation models
%******************************************************************************
t.directModels = cell(1,t.noTrustees);
   t.repModels = cell(t.noTrustees,t.noSources);

[t.directModels{:}] = deal(t.dirModelPrior);
   [t.repModels{:}] = deal(t.repModelPrior);

%******************************************************************************
%   Create and return object
%******************************************************************************
t = class(t,'bladeTruster');


