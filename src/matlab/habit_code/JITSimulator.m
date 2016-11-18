% JITSIMULATOR: simulator that generates input data just in time.
%
% Usage: [errData timeData] = JITSimulator(s,expName);
% - where s is a structure with the following fields:
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
function [errData timeData] = JITSimulator(s,expName,conditions)

%******************************************************************************
%   Initialise space for results
%******************************************************************************
arraySize = [s.noConditions,1];
noAgents = s.noTrusters;

errData.m = nan([noAgents arraySize]);
errData.s = nan([noAgents arraySize]);
errData.n = nan([noAgents arraySize]);

timeData.m = nan([noAgents arraySize]);
timeData.s = nan([noAgents arraySize]);
timeData.n = nan([noAgents arraySize]);

rseeds = s.initialseeds;

%******************************************************************************
%   Name the trusters
%******************************************************************************
tmp.noDirectObs = s.noDirectObs{1};
tmp.noRepObs = s.noRepObs{1};
tmp.noSources = s.noSources(1);
tmp.noTrustees = s.noTrustees(1);
if isfield(s,'noParamSamples')
   tmp.noParamSamples = s.noParamSamples(1);
end
tmp.trusters = s.trusterGenerator(s,tmp,1,1);

trusters = cell(1,numel(tmp.trusters));
for i=1:numel(tmp.trusters)
   trusters{i} = [class(tmp.trusters{i}) num2str(i)];
end

clear tmp;
   

%******************************************************************************
%   Initialise file is requested
%******************************************************************************
if nargin > 1
   fileEnabled=true;
   filename = [expName '.mat'];
   save(filename, 'errData', 'timeData','rseeds','trusters');
else
   fileEnabled=false;
end

%******************************************************************************
%   For each set of conditions
%******************************************************************************
for condition=1:s.noConditions
   
   %***************************************************************************
   %   Initialise storage for condition data
   %***************************************************************************
   curTime = nan(1,noAgents+1);
   estErr  = nan(1,noAgents+1);
   curEstimates = nan(1,noAgents+1);

   %curEstData = nan(s.noRuns,noAgents+1);
   curErrData = nan(s.noRuns,noAgents);
   curTimeData = nan(s.noRuns,noAgents);
   
   %***************************************************************************
   %   Set up condition parameters
   %***************************************************************************
   p.noDirectObs = s.noDirectObs{condition};
   if isfield(s,'noParamSamples')
      p.noParamSamples = s.noParamSamples(condition);
   end
   if isfield(s,'noSeedObs')
      p.noSeedObs = s.noSeedObs(condition);
   end
   p.noRepObs = s.noRepObs{condition};
   p.noSources = s.noSources(condition);
   p.noTrustees = s.noTrustees(condition);
   p.trusters = s.trusterGenerator(s,p,condition,1);
   p.noPIEUObs = s.noPIEUObs;
	p.utilFh = s.utilFh;
      
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
      p.trustees = s.trusteeGenerator(s,p,condition,run);

      %************************************************************************
      %   Set up reputation sources
      %************************************************************************
      p.repSources = s.sourceGenerator(s,p,condition,run);
      
      %************************************************************************
      %   Run episode
      %************************************************************************
      try
         [curEstimates estErr curTime] = episode(p);
      catch
         err = lasterror;
         disp(['Caught error in condition ' num2str(condition) ...
               ' run ' num2str(run) ' : ' err.message]);
         try
            disp(['file: ' err.stack.file]);
            disp(['name: ' err.stack.name]);
            disp(['line: ' err.stack.line]);
         catch
            disp(['can''t print err stack data']);
         end
      end

      %************************************************************************
      %   Store data for this run. The first estimate is
      %   always the true value, so we use that to calculate the absolute
      %   error.
      %************************************************************************
      curErrData(run,:) = abs(curEstimates(2:end)-curEstimates(1));
      %curEstData(run,:) = curEstimates;
      curTimeData(run,:) = curTime(2:end);

   end % run loop
   
   %***************************************************************************
   %   Update statistics for this condition
   %***************************************************************************
   timeData.m(:,condition) =    nanmean(curTimeData,1)';
   timeData.n(:,condition) = sum(~isnan(curTimeData),1)';
   timeData.s(:,condition) =     nanstd(curTimeData,0,1)';

    errData.m(:,condition) =     nanmean(curErrData,1)';
    errData.n(:,condition) =  sum(~isnan(curErrData),1)';
    errData.s(:,condition) =      nanstd(curErrData,0,1)';

   %***************************************************************************
   %   save updated data
   %***************************************************************************
   if fileEnabled
      save(filename, 'errData', 'timeData','conditions','rseeds','trusters');
   end

end % condition loop





