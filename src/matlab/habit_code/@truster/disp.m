%  TRUSTER/DISP displays interesting instance variables for this truster.
%
%   instance variables displayed :
%
%   directSampleIncrement : number of direct observation samples to
%                           simulate on each iteration before stopping
%                           condition is reached.
%
%   directStopCondition : function handle that accepts utility samples
%                         and checks that stopping condition is met for
%                         the direct observation estimate
%
%   repStopCondition, repSampleIncrement: (as above for reputation)
%
%    repModelPrior : prior distribution for repModels (display class only)
%    dirModelPrior : prior distribution for directModels       ''
%  paramModelPrior : prior distribution for parameter model    ''
%
function disp(t)

%******************************************************************************
%   Fields to display
%******************************************************************************
toDisplay = {'directSampleIncrement','repSampleIncrement', ...
             'noTrustees','noSources', ...
             'directStopCondition','repStopCondition','behModelClass'};

classDisplay = {'repModelPrior', 'dirModelPrior','paramModelPrior'};

for i = 1:numel(toDisplay)
   disp(toDisplay{i});
   disp(getfield(t,toDisplay{i}));
end

for i = 1:numel(classDisplay)
   c = eval(['class(t.' classDisplay{i} ');']);
   disp([classDisplay{i} ' is a ' c]);
end



