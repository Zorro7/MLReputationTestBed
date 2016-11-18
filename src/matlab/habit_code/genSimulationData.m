% GENSIMULATIONDATA: Generate data for running simulation
%
% Usage: simData = genSimulationData(s); - where s is a structure with the following
% fields:
%
%           noRuns : number of runs per condition
%     noConditions : number of conditions
%       noTrusters : number of trusters
%        noPIEUObs : number of observations to estimate actual expected utility
%           utilFh : utility function handle
%
%      noDirectObs : number of direct observations for each condition (cell)
%         noRepObs : number of reputation obs for each condition (cell)
%
%        noSources : number of sources for each condition (vector)
%       noTrustees : number of trustees for each condition (vector)
%
% trusteeGenerator : function handle for generating trustees
%  sourceGenerator : function handle for generating sources
% trusterGenerator : function handle for generating trusters
%
% Each of the function handles for generating agents should have the following
% form:
%
%   agents = fh(p,condition,run) where
%
%    condition is the condition number
%          run is the run number
%       agents is the cell array of agents
% 
%
function simData = genSimulationData(s)

%******************************************************************************
%   Allocate space for episode data
%******************************************************************************
simData.episodeOnly  = cell(s.noRuns,s.noConditions);
simData.conditionCommons = cell(s.noConditions,1);
simData.common = struct;

%******************************************************************************
%   Set up common parameters
%******************************************************************************
simData.common.noPIEUObs = s.noPIEUObs;
simData.common.utilFh = s.utilFh;

%******************************************************************************
%   For each set of conditions
%******************************************************************************
for condition=1:s.noConditions
   
   %***************************************************************************
   %   Set up condition parameters
   %***************************************************************************
   
   simData.conditionCommons{condition}.noDirectObs = s.noDirectObs{condition};
   simData.conditionCommons{condition}.noRepObs = s.noRepObs{condition};
   
   simData.conditionCommons{condition}.noSources = s.noSources(condition);
   simData.conditionCommons{condition}.noTrustees = s.noTrustees(condition);
   
   p = aggregateStructs(simData.common,simData.conditionCommons{condition});
   
   simData.conditionCommons{condition}.trusters = ...
      s.trusterGenerator(s,p,condition,1);
      
   %***************************************************************************
   %   Display status
   %***************************************************************************
   disp(sprintf('condition: %d',condition));

   %***************************************************************************
   %   For each run ....
   %***************************************************************************
   for run=1:s.noRuns
      
      %************************************************************************
      %   Set up trustees
      %************************************************************************
      simData.episodeOnly{run,condition}.trustees = ...
         s.trusteeGenerator(s,p,condition,run);

      %************************************************************************
      %   Set up reputation sources
      %************************************************************************
      simData.episodeOnly{run,condition}.repSources = ...
         s.sourceGenerator(s,p,condition,run);

   end

end



