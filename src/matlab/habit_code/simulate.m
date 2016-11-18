% SIMULATE: calls episode, collects and stores data
%
% Usage: [errData timeData] = simulate(simData,noAgents,expName);
%
% where p is N-D array of episode p structures.
%       prefix for file name, if requested.
%
% If expName is presented then data is saved to file 
% called expName-[timestamp].m
%
% Running the experiments
% -----------------------
% Compile the episode function.
% 
% Generate all input data for episode off-line and put into
% structure array.
% 
% Dimensions of structure array correspond to control variables.
% 1st dimension is always the run.
% 
% Output is aggregrated along first dimension and squeezed.
% Output is in the form of the sufficient statistics:
% 
% m - nanmean(x,1)     - mean
% s - nanstd(x,0,1)    - standard deviation
% n - sum(~isnan(x),1) - number of samples (minus missing data)
% 
% where x is the generated data (estimation error and time data)
%
%
function [errData timeData] = simulate(simData,noAgents,expName)

%******************************************************************************
%   Initialise space for results
%******************************************************************************
arraySize = size(simData.conditionCommons);

errData.m = nan([noAgents arraySize]);
errData.s = nan([noAgents arraySize]);
errData.n = nan([noAgents arraySize]);

timeData.m = nan([noAgents arraySize]);
timeData.s = nan([noAgents arraySize]);
timeData.n = nan([noAgents arraySize]);

%******************************************************************************
%   Initialise file is requested
%******************************************************************************
if nargin > 2
   fileEnabled=true;
   filename = [expName datestr(now,'yymmddHHMMSS')];
   save(filename, 'errData', 'timeData');
else
   fileEnabled=false;
end

%******************************************************************************
%   Run simulation and save iteratively
%******************************************************************************
noRuns = size(simData.episodeOnly,1);
noConditions = prod(arraySize(:));
for condition=1:noConditions

   %***************************************************************************
   %   Initialise storage for condition data
   %***************************************************************************
   disp(sprintf('condition: %d',condition));
   curTime = nan(1,noAgents+1);
   estErr  = nan(1,noAgents+1);
   curEstimates = nan(1,noAgents+1);

   curErrData = nan(noRuns,noAgents);
   curTimeData = nan(noRuns,noAgents);

   %***************************************************************************
   %   Try to run episode for required number of runs
   %***************************************************************************
   for run=1:noRuns

      %************************************************************************
      %   Construct input parameters for episode
      %************************************************************************
      p = aggregateStructs(simData.common, ...
            simData.conditionCommons{condition}, ...
            simData.episodeOnly{run,condition});

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
      save(filename, 'errData', 'timeData');
   end

end % condition loop









