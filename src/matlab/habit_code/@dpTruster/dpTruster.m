% DPTRUSTER/DPTRUSTER Dirichlet Process truster with dirichlet opinions.
%
%  Usage: t = dpTruster(s); 
%
%  If s is present, it should be a structure containing initial values
%  for any of the following instance variables:
%
%  priorDist      - conjugate prior distribution
%  behModelClass  - behaviour distribution class (as string)
%  alpha          - DP scale parameter (weight of prior distribution)
%  logweight      - function handle for calculating weights
%  noSources      - number of reputation sources
%  noTrustees     - number of trustees
%
%  logweight should accept two conjugate parameter distributions as
%  arguments. One specfies the current trustee's parameter distribution
%  and the other specifies the parameter distribution of another trustee.
%  The function should be symmetric w.r.t. to its arguments.
%   
function t = dpTruster(s)

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
   t.behModelClass = 'multinomial';
else
   t.behModelClass = s.behModelClass;
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
%   Set default dirichlet process scale parameter (essentially the weight
%   applied to the prior distribution)
%******************************************************************************
if ~isfield(s,'alpha')
   t.alpha = 1;
else
   t.alpha = s.alpha;
end

%******************************************************************************
%   Set default log weight calculation function
%******************************************************************************
if ~isfield(s,'logweight')
   t.logweight = 'logweight';
else
   t.logweight = s.logweight;
end

%******************************************************************************
%   Set default belief model priors if none provided
%******************************************************************************
priors = {'priorDist'};
defaultPrior = dirichlet;

for i = 1:numel(priors)
   if ~isfield(s,priors{i})
      t = setfield(t,priors{i},defaultPrior);
   else
      t = setfield(t,priors{i},getfield(s,priors{i}));
   end
end

%******************************************************************************
%   Initialise direct and reputation models
%******************************************************************************
t.directObs    = cell(1,t.noTrustees);
   t.repModels = cell(t.noTrustees,t.noSources);

[t.repModels{:}] = deal(t.priorDist);

%******************************************************************************
%   Create and return object
%******************************************************************************
t = class(t,'dpTruster');


