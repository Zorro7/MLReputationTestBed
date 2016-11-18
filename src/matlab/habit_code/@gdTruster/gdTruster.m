% GDTRUSTER/GDTRUSTER Dirichlet Truster with any parameter model
%
%  Usage: t = gdTruster(s); or t = gdTruster;
%
%  If s is provided, it is a structure containing any initial values
%  for the class instance variables detailed below. Note that inital
%  values for directModels and repModels are always ignored.
%
%   instance variables :
%
%    noParamSamples: number parameters to sample
%    noSamplesPerModel: number parameters per sampled parameter model
%
%    noTrustees : initially assumed number of trustees
%    noSources : initially assumed number of reputation sources
%
%    repDims : number of possible opinion values
%    dirDims : number of possible behaviour values
%
%  paramModelPrior : prior distribution for parameter model
%
function t = gdTruster(s)

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
%   Set default belief model priors if none provided.
%   The paramModel prior has noSources+1 less dimensions than the actual
%   number of parameters. This is because each set of discrete parameters
%   must add to 1, so for each distribution over n possible values, only
%   (n-1) probabilities need to be modelled.
%******************************************************************************
if ~isfield(s,'repDims')
   s.repDims = 3;
end

if ~isfield(s,'dirDims')
   s.dirDims = 3;
end

t.repModelPrior = dirichlet(s.repDims);
t.dirModelPrior = dirichlet(s.dirDims);

if ~isfield(s,'paramModelPrior')
   noParams = (s.repDims-1)*t.noSources+s.dirDims-1;
   t.paramModelPrior = nig(noParams);
else
   t.paramModelPrior = s.paramModelPrior;
end

%******************************************************************************
%   Set default number of parameter samples
%******************************************************************************
if ~isfield(s,'noParamSamples')
   t.noParamSamples = 50;
else
   t.noParamSamples = s.noParamSamples;
end

if ~isfield(s,'noSamplesPerModel')
   t.noSamplesPerModel = 1;
else
   t.noSamplesPerModel = s.noSamplesPerModel;
end

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
t = class(t,'gdTruster');


