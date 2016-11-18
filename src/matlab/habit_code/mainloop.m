% MAINLOOP: Main loop for new TRAVOS-C Experiments
%
% Usage: [m t e] = mainloop(s); - where s is a structure with the following
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
function [m t e] = mainloop(s)

%******************************************************************************
%   Allocate space for results
%******************************************************************************
m = zeros(s.noRuns,s.noConditions,s.noTrusters+1);
e = zeros(s.noRuns,s.noConditions,s.noTrusters+1);
t = zeros(s.noRuns,s.noConditions,s.noTrusters+1);

%******************************************************************************
%   For each set of conditions
%******************************************************************************
for condition=1:s.noConditions
   
   %***************************************************************************
   %   Set up condition parameters
   %***************************************************************************
   p.noPIEUObs = s.noPIEUObs;
   p.utilFh = s.utilFh;
   
   p.noDirectObs = s.noDirectObs{condition};
   p.noRepObs = s.noRepObs{condition};
   
   p.noSources = s.noSources(condition);
   p.noTrustees = s.noTrustees(condition);

   %***************************************************************************
   %   For each run ....
   %***************************************************************************
   for run=1:s.noRuns
      
      try
      
         %*********************************************************************
         %   Display status
         %*********************************************************************
         disp(sprintf('condition: %d run number: %d',condition,run));

         %*********************************************************************
         %   Set up trustees
         %*********************************************************************
         p.trustees = s.trusteeGenerator(s,p,condition,run);

         %*********************************************************************
         %   Set up reputation sources
         %*********************************************************************
         p.repSources = s.sourceGenerator(s,p,condition,run);

         %*********************************************************************
         %   Set up trusters
         %*********************************************************************
         p.trusters = s.trusterGenerator(s,p,condition,run);

         %*********************************************************************
         %   Run episode
         %*********************************************************************
         [m(run,condition,:) e(run,condition,:) t(run,condition,:)]=episode(p);
         
      catch
         err = lasterror;
         disp(['Caught error: ' err.message]);
         disp(['file: ' err.stack.file]);
         disp(['name: ' err.stack.name]);
         disp(['line: ' err.stack.line]);
         m(run,condition,:) = nan;
         e(run,condition,:) = nan;
         t(run,condition,:) = nan;
      end

   end

end



