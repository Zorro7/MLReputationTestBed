function aggregatePollingResults(outFile, varargin)
% AGGREGATEPOLLINGRESULTS aggregates results of polling experiments from cluster

% list of variables that we wish to load from each file
VARS = {'allParams', 'estErrors', 'estSeeds', 'estTimers', 'estWeight',...
   'estimates', 'trueValue'};

%******************************************************************************
% For each input file
%******************************************************************************
 allParams = []; % parameters used in this set of experiments
batchSeeds = zeros(1,nargin-1); % random seeds used at start of each batch
 estErrors = cell(1,nargin-1);  % cell arrays to store all data from each batch
  estSeeds = cell(1,nargin-1);
 estTimers = cell(1,nargin-1);
 estWeight = cell(1,nargin-1);
 estimates = cell(1,nargin-1);
 trueValue = cell(1,nargin-1);
    
 for k=1:numel(varargin)
   
   %***************************************************************************
   %  Load the next file
   %***************************************************************************
   curFile = load(varargin{k},VARS{:});
   
   try
   
   %***************************************************************************
   %  Retrieve parameters used for this experiment
   %***************************************************************************
   curParams = rmfield(curFile.allParams,'randSeed');
   
   %***************************************************************************
   %  Store the parameters for the first file for future reference, and ensure
   %  that all subsequent files have matching parameters
   %***************************************************************************
   if isempty(allParams)
      
      allParams = curParams;
      
   elseif ~isequalwithequalnans(allParams,curParams)
      
      error('Trying to merge results based on different control conditions');
      
   end
   
   %***************************************************************************
   %  Store random seeds that where used at the start of each batch
   %***************************************************************************
   batchSeeds(k) = curFile.allParams.randSeed;
   
   %***************************************************************************
   % For now, store all other data in cell arrays (we aggregate these later
   % once we know how much space to allocate).
   %***************************************************************************
   estErrors{k} = curFile.estErrors;
   estSeeds{k} = curFile.estSeeds;
   estTimers{k} = curFile.estTimers;
   estWeight{k} = curFile.estWeight;
   estimates{k} = curFile.estimates;
   trueValue{k} = curFile.trueValue;
   
   catch
      warning('agg:missing','Some results missing.');
   end
   
 end

 %*****************************************************************************
 % Stick the batch seeds into the allParams structure, so that everything
 % is in the same place as in the input file.
 %*****************************************************************************
 allParams.randSeed = batchSeeds;
 
 %*****************************************************************************
 % Now concatenate all the results from all files along the sampling dimension
 % In most cases, this is the 1st dimension
 %*****************************************************************************
 estErrors = cat(1,estErrors{:});
 estSeeds  = cat(1,estSeeds{:});
 estTimers = cat(1,estTimers{:});
 estWeight = cat(2,estWeight{:});
 estimates = cat(1,estimates{:});
 trueValue = cat(1,trueValue{:});
 
 %*****************************************************************************
 % Now save the results
 %*****************************************************************************
 save(outFile,VARS{:});
 