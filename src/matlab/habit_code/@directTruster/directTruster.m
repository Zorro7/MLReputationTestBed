% GDTRUSTER/GDTRUSTER Dirichlet Truster with any parameter model
%
%  Usage: t = directTruster(s); or t = directTruster;
%
%  If s is provided, it is a structure containing any initial values
%  for the class instance variables detailed below. Note that inital
%  values for directModels and repModels are always ignored.
%
%   instance variables :
%
%    noTrustees : initially assumed number of trustees
%
%    dirDims : number of possible behaviour values
%
function t = directTruster(s)

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
if ~isfield(s,'noTrustees')
   t.noTrustees = 1;
else
   t.noTrustees = s.noTrustees;
end

%******************************************************************************
%   Set default belief model priors if none provided.
%   The paramModel prior has noSources+1 less dimensions than the actual
%   number of parameters. This is because each set of discrete parameters
%   must add to 1, so for each distribution over n possible values, only
%   (n-1) probabilities need to be modelled.
%******************************************************************************
if ~isfield(s,'dirDims')
   s.dirDims = 3;
end

t.dirModelPrior = dirichlet(s.dirDims);


%******************************************************************************
%   Initialise direct and reputation models
%******************************************************************************
t.directModels = cell(1,t.noTrustees);

[t.directModels{:}] = deal(t.dirModelPrior);

%******************************************************************************
%   Create and return object
%******************************************************************************
t = class(t,'directTruster');


