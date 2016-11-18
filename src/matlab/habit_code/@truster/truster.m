% TRUSTER/TRUSTER Base implementation for new TRAVOS-C reputation model.
%
%  Usage: t = truster(s); or t = truster;
%
%  If s is provided, it is a structure containing any initial values
%  for the class instance variables detailed below. Note that inital
%  values for directModels and repModels are always ignored.
%
%   instance variables :
%
%   directModels: cell array of trustee belief models based on direct
%                 observations.
%
%   directSampleIncrement : number of direct observation samples to
%                           simulate on each iteration before stopping
%                           condition is reached.
%
%   directStopCondition : function handle that accepts utility samples
%                         and checks that stopping condition is met for
%                         the direct observation estimate
%
%   repModels: cell array of source/trustee opinion models indexed as
%              repModels{trustee,source} 
%
%   repStopCondition, repSampleIncrement: (as above for reputation)
%
%    noTrustees : initially assumed number of trustees
%    noSources : initially assumed number of reputation sources
%
%    repModelPrior : prior distribution for repModels
%    dirModelPrior : prior distribution for directModels
%  paramModelPrior : prior distribution for parameter model
%
%    behModelClass : class representing behaviour distribution
%  paramModelClass : class representing parameter distribution
%
%  Note, beModelClass and paramModelClass could easily be function handles
%  to any function that returns an appropriate distribution object.
%  For example, a function that supplies other default arguments to the main
%  constructor.
%   
function t = truster(s)

%******************************************************************************
%   Initial input structure if none provided
%******************************************************************************
if isequal(nargin,0)
   s = struct;
end
t = struct;

%******************************************************************************
%   set default behaviour model class
%******************************************************************************
if ~isfield(s,'behModelClass')
   t.behModelClass = 'multinormal';
else
   t.behModelClass = s.behModelClass;
end

if ~isfield(s,'paramModelClass')
   t.paramModelClass = 'multinormal';
else
   t.paramModelClass = s.paramModelClass;
end

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
priors = {'repModelPrior', 'dirModelPrior'};
defaultPrior = nig(1);

for i = 1:numel(priors)
   if ~isfield(s,priors{i})
      t = setfield(t,priors{i},defaultPrior);
   else
      t = setfield(t,priors{i},getfield(s,priors{i}));
   end
end

if ~isfield(s,'paramModelPrior')
   t.paramModelPrior = nig(2*t.noSources+2);
else
   t.paramModelPrior = s.paramModelPrior;
end

%******************************************************************************
%   Set default increments if none provided
%******************************************************************************
if ~isfield(s,'directSampleIncrement')
   t.directSampleIncrement = 100;
else
   t.directSampleIncrement = s.directSampleIncrement;
end

if ~isfield(s,'repSampleIncrement')
   t.repSampleIncrement = 100;
else
   t.repSampleIncrement = s.repSampleIncrement;
end

%******************************************************************************
%   Set default stopping condition if none provided
%******************************************************************************
if ~isfield(s,'directStopCondition')
   t.directStopCondition = @(s) numel(s) >= 100;
else
   t.directStopCondition = s.directStopCondition;
end

if ~isfield(s,'repStopCondition')
   t.repStopCondition = @(s) numel(s) >= 100;
else
   t.repStopCondition = s.repStopCondition;
end

%******************************************************************************
%   Initialise direct and reputation models
%******************************************************************************
t.directModels = cell(1,t.noTrustees);
   t.repModels = cell(t.noTrustees,t.noSources);

[t.directModels{:}] = deal(t.dirModelPrior);
   [t.repModels{:}] = deal(t.repModelPrior);
   
   disp(t)

%******************************************************************************
%   Create and return object
%******************************************************************************
t = class(t,'truster');


