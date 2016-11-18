% POLLINGBATCH Runs a batch of experiments on the polling data set.
% Usage: pollingBatch inDataFile outDataFile
%    or: output = pollingBatch(p,data)
% where p contains the following control variables
%
%     randSeed    : 1xN vector of random seeds, or scalar (see doc RandStream)
%     noSamples   : (scalar)
%     noTrustees  : (1xN vector or scalar)
%     repSource   : (1xN vector or scalar) [TBD use this instead of repId]
%     windowSize  : (1xN vector or scalar) [only used if windowId is not nan]
%     windowId    : (1xN vector or scalar) [if nan WindowSize used instead]
%     repId       : (1xN vector or scalar) [seems to make repSource redundant]
%
%     Output contains all input, plus
%     1xM cell array of truster names
%     NxM matrices: Estimates, Errors, Times
%     results for trusters 1:M
%     
%     suggested data usage: create conditions as follows
%     condIndices = (windowSize==A) & (repSource==B) & ...
%     selectedData = M(find(condIndices),:);
%     plotPollingData(selectedData)
%
function varargout = pollingBatch(varargin)

warning('off','MATLAB:nearlySingularMatrix');

%******************************************************************************
%  Ensure correct number of parameters
%******************************************************************************
if ~isequal(2,nargin)
   error('Usage: pollingBatch inFile outFile; or pollingBatch(data,p)');
end
   
%******************************************************************************
%  If called using command line syntax, load parameters from file
%******************************************************************************
if ischar(varargin{1}) && ischar(varargin{2})

   outFile = varargin{2};
   load(varargin{1});
   load(varargin{2}); % bit of a hack to use both files as input

%******************************************************************************
%  Otherwise, retrieve the parameters directly
%******************************************************************************
elseif isstruct(varargin{1}) && isnumeric(varargin{2})

   outFile = nan; % null output file - result returned, not stored.
   p = varargin{1};
   data = varargin{2};

%******************************************************************************
%  throw an error if in correct parameter types
%******************************************************************************
else
   error('Unexpected argument types');
end

%******************************************************************************
%  The index of Rama's data. We use this as the source for the truster's
%  own direct observations due to the variation in correlation with
%  simon, alex, alba. In particular, simon is on the same subnet, alex the
%  same building, and alba is 400 miles away on an unreliable rural
%  broadband connection.
%******************************************************************************
p.ramaId = 1;

%******************************************************************************
%  Replicate any scalar parameters (NOT INCLUDING SEEDS OR NOSAMPLES!!!)
%******************************************************************************
noEpisodes = max([numel(p.noTrustees), ...
                  numel(p.repId), ...
                  numel(p.windowSize), numel(p.windowId)]);

if isscalar(p.noTrustees)
   p.noTrustees = repmat(p.noTrustees,1,noEpisodes);
end

% if isscalar(p.repSource)
%    p.repSource = repmat(p.repSource,1,noEpisodes);
% end

if isscalar(p.windowSize)
   p.windowSize = repmat(p.windowSize,1,noEpisodes);
end

if isscalar(p.windowId)
   p.windowId = repmat(p.windowId,1,noEpisodes);
end

if isscalar(p.repId)
   p.repId = repmat(p.repId,1,noEpisodes);
end

%******************************************************************************
%  Generate the Trusters
%******************************************************************************
%trusters = {genDPTruster(max(p.noTrustees))};
%trusters = {ggTruster(max(p.noTrustees))};
trusters = {genDPTruster(max(p.noTrustees)),ggTruster(max(p.noTrustees))};
noTrusters = numel(trusters);

%******************************************************************************
%  Allocate space for results
%******************************************************************************
NO_OBS_TYPES = 5; % number of different types of source information (see below)
trueValue = nan(p.noSamples,noEpisodes);
estimates = nan(p.noSamples,noEpisodes,noTrusters,NO_OBS_TYPES);
estErrors = nan(p.noSamples,noEpisodes,noTrusters,NO_OBS_TYPES);
estTimers = nan(p.noSamples,noEpisodes,noTrusters,NO_OBS_TYPES);
estWeight = nan(max(p.noTrustees),p.noSamples,noEpisodes,noTrusters,NO_OBS_TYPES);
estSeeds = cell(p.noSamples,noEpisodes);

%******************************************************************************
%  Set the random seeds
%******************************************************************************
RandStream.setDefaultStream(RandStream('mt19937ar','Seed',p.randSeed(1)));

%******************************************************************************
%  Run each episode
%******************************************************************************
allParams = p;
for episode=1:noEpisodes
      
      p.repId = allParams.repId(episode);
      p.windowId = allParams.windowId(episode);
      p.windowSize = allParams.windowSize(episode);
      %p.repSource = allParams.repSource(episode);
      p.noTrustees = allParams.noTrustees(episode);

      %************************************************************************
      %  If we've got one random seed per episode, reset them now
      %************************************************************************
      if ~isscalar(p.randSeed)
         rstream = RandStream('mt19937ar','Seed',p.randSeed(episode));
         RandStream.setDefaultStream(rstream);
      end

      %************************************************************************
      %  Run Episode for each sample
      %************************************************************************
      for sample=1:p.noSamples
         
         try

         fprintf('episode %d sample %d\n',episode,sample);
         %*********************************************************************
         %  Save data from last round, if necessary
         %*********************************************************************
         if ischar(outFile)
            save(outFile,'-V7.3');
         end

         %*********************************************************************
         %  Store the seeds used to generate this sample
         %*********************************************************************
         estSeeds{sample,episode} = RandStream.getDefaultStream.State;

         %*********************************************************************
         %  Select trustees for test without replacement
         %*********************************************************************
          totalNoTrustees = size(data,2);
              allTrustees = randsample(totalNoTrustees,p.noTrustees);
              testTrustee = allTrustees(1);

         %*********************************************************************
         %  Strip out trustee training data and the value of the test point
         %*********************************************************************
         if isnan(p.windowId)
            noWindows = floor(size(data,1)/p.windowSize);
            sampleWindow = randsample(noWindows,1);
         else
            sampleWindow = p.windowId;
         end
         
         startIndex = p.windowSize*(sampleWindow-1)+1;
         endIndex = p.windowSize*(sampleWindow)-1; % -1 for test point
         trainingData = data(startIndex:endIndex,allTrustees,:);

         trueValue(sample,episode) = data(endIndex+1,testTrustee,p.ramaId);

         %*********************************************************************
         %  Generate results for the episode
         %  The last argument (the test trustee id) is always one, because
         %  this index is relative to the training data.
         %*********************************************************************
         for trusterId=1:noTrusters

            theTruster = trusters{trusterId};
            
            % hack to stop reputation model giving up when some trustees
            % are not observed.
            if isa(theTruster,'ggTruster')
               theTruster = ggTruster(p.noTrustees);
            elseif isa(theTruster,'dpTruster')
               theTruster = genDPTruster(p.noTrustees);
            end

            m = nan(1,NO_OBS_TYPES);
            e = nan(1,NO_OBS_TYPES);
            t = nan(1,NO_OBS_TYPES);
            w = nan(p.noTrustees,NO_OBS_TYPES);

            % prior information only
            [m(1) e(1) t(1) w(:,1)] = ...
               genEstimate(theTruster,1);

            % group observations only
            [m(2) e(2) t(2) w(:,2)] = ...
               groupObsEstimate(trainingData,theTruster,p,1);

            % reputation information only
            [m(3) e(3) t(3) w(:,3)] = ...
               repSourceEstimate(trainingData,theTruster,p,1);
            
            % direct information only
            [m(4) e(4) t(4) w(:,4)] = ...
               directObsEstimate(trainingData,theTruster,p,1);

            % reputation and direct information
            [m(5) e(5) t(5) w(:,5)] = ...
               repDirectEstimate(trainingData,theTruster,p,1);

            estimates(sample,episode,trusterId,:) = m;
            estErrors(sample,episode,trusterId,:) = e;
            estTimers(sample,episode,trusterId,:) = t;
            estWeight(1:size(w,1),sample,episode,trusterId,:) = w;

         end
         
         
         %*********************************************************************
         %  Print any errors and skip to the next episode
         %*********************************************************************
         catch err
            msg = err.message;
            fprintf('Error in sample %d epsiode %d: %s\n',sample,episode,msg);
            disp('stack:');
            for f=1:numel(err.stack)
               stack = err.stack(f);
               fprintf('In %s, line %d of %s\n',...
                  stack.name,stack.line,stack.file);
            end
            
         end % try-catch block
         
      end % sample loop


end % episode loop

%*********************************************************************
%  Save data from last round, if necessary
%*********************************************************************
if ischar(outFile)
   save(outFile,'-V7.3');
end

%******************************************************************************
%  Finally, we return the results if called as a function
%******************************************************************************
if 0<nargout
   varargout{1} = trueValue;
end

if 1<nargout
   varargout{2} = estimates;
end

if 2<nargout
   varargout{3} = estErrors;
end

if 3<nargout
   varargout{4} = estTimers;
end

if 4<nargout
   varargout{5} = estWeight;
end

if 5<nargout
   varargout{6} = estSeeds;
end



%******************************************************************************
%******************************************************************************
%  Generate dpTruster
%******************************************************************************
%******************************************************************************
function theTruster = genDPTruster(noTrustees)

prior = nig(10^-10,10^-20,10^-50,0);   % proper uninformative prior

s.behModelClass = 'multinormal';             % behaviour model class
   s.noTrustees = noTrustees;   % no. trustees
    s.noSources = 1;                         % no. reputation sources
    s.priorDist = prior;                     % Prior for behaviour model
    s.alpha = 2;

     theTruster = dpTruster(s);  % set up theTruster based on these parameters


%******************************************************************************
%******************************************************************************
%  Generate Estimate for specified Model
%******************************************************************************
%******************************************************************************
function [m e t w] = genEstimate(theTruster,theTrustee)

   tic;
   [m e w] = euEstimate(theTruster,theTrustee,funch);
   t = toc;
   
%******************************************************************************
%******************************************************************************
%  Generate Reputation Estimate
%******************************************************************************
%******************************************************************************
function [m e t w] = repSourceEstimate(data,theTruster,p,trusteeId)

   %***************************************************************************
   %  Train the theTruster on all but the trustee's data
   %***************************************************************************
   noTrustees = size(data,2);
   for te=[1:(trusteeId-1), (trusteeId+1):noTrustees]

      obs = data(:,te,p.ramaId); 
      obs = obs(isfinite(obs)); % remove missing data
      theTruster = directObserve(theTruster,te,obs);
      
   end

   %***************************************************************************
   %  Train based on the Reputation Sources data
   %  Notice, here, we only have one reputation source, which theTruster
   %  always refers to as index 1.
   %***************************************************************************
   for te=1:noTrustees

      obs = data(:,te,p.repId);
      obs = obs(isfinite(obs)); % remove missing data
      if isempty(obs)
         continue;
      end
      theTruster = repReceive(theTruster,te,1,obs);

   end

   %***************************************************************************
   %  Return the estimate
   %***************************************************************************
   [m e t w] = genEstimate(theTruster,trusteeId);

%******************************************************************************
%******************************************************************************
%  Generate Reputation & Direct Observation Estimate
%******************************************************************************
%******************************************************************************
function [m e t w] = repDirectEstimate(data,theTruster,p,trusteeId)

   %***************************************************************************
   %  Train the theTruster on all INCLUDING!! the trustee's data
   %***************************************************************************
   noTrustees = size(data,2);
   for te=1:noTrustees

      obs = data(:,te,p.ramaId); 
      obs = obs(isfinite(obs)); % remove missing data
      theTruster = directObserve(theTruster,te,obs);
      
   end

   %***************************************************************************
   %  Train based on the Reputation Sources data 
   %***************************************************************************
   for te=1:noTrustees

      obs = data(:,te,p.repId);
      obs = obs(isfinite(obs)); % remove missing data
      theTruster = repReceive(theTruster,te,1,obs);

   end

   %***************************************************************************
   %  Return the estimate
   %***************************************************************************
   [m e t w] = genEstimate(theTruster,trusteeId);

%******************************************************************************
%******************************************************************************
%  Generate Group Behaviour Estimate
%******************************************************************************
%******************************************************************************
function [m e t w] = groupObsEstimate(data,theTruster,p,trusteeId)

   %***************************************************************************
   %  Train the theTruster on all but the trustee's data
   %***************************************************************************
   noTrustees = size(data,2);
   for te=[1:(trusteeId-1), (trusteeId+1):noTrustees]

      obs = data(:,te,p.ramaId); 
      obs = obs(isfinite(obs)); % remove missing data
      theTruster = directObserve(theTruster,te,obs);
      
   end

   %***************************************************************************
   %  Return the estimate
   %***************************************************************************
   [m e t w] = genEstimate(theTruster,trusteeId);

%******************************************************************************
%******************************************************************************
%  Generate Direct Observation Estimate
%******************************************************************************
%******************************************************************************
function [m e t w] = directObsEstimate(data,theTruster,p,trusteeId)

   %***************************************************************************
   %  Train the theTruster using Rama's data for the trustee on its own
   %***************************************************************************
   obs = data(:,trusteeId,p.ramaId); 
   obs = obs(isfinite(obs)); % remove missing data
   theTruster = directObserve(theTruster,trusteeId,obs);

   %***************************************************************************
   %  Return the estimate
   %***************************************************************************
   [m e t w] = genEstimate(theTruster,trusteeId);

