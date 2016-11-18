% Experimental run for single DP Dirichlet truster
function [m e] = DPDirichletRun100408

%******************************************************************************
%   Set up run parameters
%******************************************************************************
noRuns = 100;

noDirectObs = [1:noTrustees];
noRepObs = ones(noSources,noTrustees)*10;
noPIEUObs = 200;
noSources = 5;
noTrustees = 10;

%******************************************************************************
%   Set up truster's utility function
%******************************************************************************
p.utilFh = funch;

%******************************************************************************
%   Allocate space for results
%******************************************************************************
m = zeros(s.noRuns,s.noConditions,s.noTrusters+1);
e = zeros(s.noRuns,s.noConditions,s.noTrusters+1);

%******************************************************************************
%   For each set of conditions
%******************************************************************************
for condition=1:s.noConditions
   
   %***************************************************************************
   %   Set up condition parameters
   %***************************************************************************
   p.noDirectObs = s.noDirectObs{condition};
   p.noRepObs = s.noRepObs{condition};
   p.noPIEUObs = s.noPIEUObs{condition};
   p.noSources = s.noSources{condition};
   p.noTrustees = s.noTrustees{condition};
   p.utilFh = s.utilFh{condition};

   %***************************************************************************
   %   For each run ....
   %***************************************************************************
   for run=1:noRuns

      %************************************************************************
      %   Set up trustees
      %************************************************************************
      p.trustees = s.trusteeGenerator(condition,i);
      
      %************************************************************************
      %   Set up reputation sources
      %************************************************************************
      p.repSources = s.sourceGenerator(condition,i);

      %************************************************************************
      %   Set up trusters
      %************************************************************************
      p.trusters = s.trusterGenerator(condition,i);

      %************************************************************************
      %   Run episode
      %************************************************************************
      [m(run,condition,:) e(run,condition,:)] = episode(p);

   end

end

%******************************************************************************
%   Record statistics for run
%******************************************************************************


