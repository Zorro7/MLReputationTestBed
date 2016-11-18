% TRUSTER/euEstimate
%
% Usage: [m e] = euEstimate(t,trustee,utilFh);
%
function [m e] = euEstimate(t,trustee,utilFh)

MAX_SAMPLES = 1000; % maximum allowable number of samples of any kind.

%******************************************************************************
%   Generate estimate based on direct experience.
%******************************************************************************
[dirM dirVar] = directEstimateInd(t,trustee,utilFh,MAX_SAMPLES);

%******************************************************************************
%   Generate estimate based on reputation
%******************************************************************************
[repM repVar] = reputationEstimateInd(t,trustee,utilFh,MAX_SAMPLES);

%******************************************************************************
%   Calculate estimate and approximate standard error.
%******************************************************************************
totPrec = 1/dirVar + 1/repVar;
      m = totPrec*(dirM/dirVar + repM/repVar);
      e = sqrt(1/totPrec);

%******************************************************************************
%******************************************************************************
%   Subfunction for direct estimation with independent samples
%******************************************************************************
%******************************************************************************
function [dirM dirVar] = directEstimateInd(t,trustee,utilFh,MAX_SAMPLES)

utilSamples = []; % utility samples
model = t.directModels{trustee}; % belief model based on direct obs of trustee
stopConditionReached = false; % true when we've reached sufficient precision

while ~stopConditionReached

   %***************************************************************************
   %   Sample behaviour distributions from belief model
   %***************************************************************************
   paramSamples = sample(model,t.directSampleIncrement);

   sampledDistributions = feval(t.behModelClass,paramSamples);

   %***************************************************************************
   %   Generate behaviour samples from sampled behaviour distributions
   %***************************************************************************
   behaviourSamples = sample(sampledDistributions);
     newUtilSamples = fheval(utilFh,behaviourSamples);
        utilSamples = [utilSamples newUtilSamples];

   %***************************************************************************
   %   Update stopping condition
   %***************************************************************************
   stopConditionReached = (numel(utilSamples)>MAX_SAMPLES) || ...
                          feval(t.directStopCondition,utilSamples);

end % while loop

%******************************************************************************
%   Warn if we stopped before the stopping condition was reached.
%******************************************************************************
if (numel(utilSamples)>MAX_SAMPLES)
   warn('euEstimate/directEstimateInd: Maximum number of samples reached');
end

%******************************************************************************
%   Return the estimate along with approximate standard error.
%******************************************************************************
  dirM = mean(utilSamples);
dirVar = var(utilSamples)/numel(utilSamples);

%******************************************************************************
%******************************************************************************
%   Subfunction for reputation based estimation with indepedent samples
%******************************************************************************
%******************************************************************************
function [repM repVar] = reputationEstimateInd(t,trustee,utilFh,MAX_SAMPLES)

utilSamples = []; % utility samples
stopConditionReached = false; % true when we've reached sufficient precision

%******************************************************************************
%   Until stopping condition is reached
%******************************************************************************
while ~stopConditionReached
   
   %***************************************************************************
   %   Sample joint parameters from opinion models
   %***************************************************************************
   opSamples = cell(size(t.repModels)); % sampled parameters
   dirSamples = cell(numel(t.directModels),1);
   
   for trIt = 1:size(t.repModels,1)
      
      for sIt = 1:size(t.repModels,2)
         opSamples{trIt,sIt}=sample(t.repModels{trIt,sIt},t.repSampleIncrement);
      end
      
      if ~isequal(trIt,trustee)
         dirSamples{trIt} = sample(t.directModels{trIt},t.repSampleIncrement);
      end
      
   end
   
   dirSamples = dirSamples([1:(trustee-1),(trustee+1):size(t.repModels,1)]);
   
   %***************************************************************************
   %   Select reputation parameters for the trustee in question
   %***************************************************************************
   reputationParams = [opSamples{trustee,:}];
   
   %***************************************************************************
   %   Select parameter samples for learning
   %***************************************************************************
   noTrustees = size(opSamples,1);
   
   observedParams = ...
      [dirSamples opSamples([1:(trustee-1),(trustee+1):noTrustees],:)]';
   
   observedParams = [observedParams{:}];
   
   %***************************************************************************
   %   If there are no observed samples, just sample from the prior
   %***************************************************************************
   if isequal(0,numel(observedParams))
      
          paramSamples = sample(t.dirModelPrior,t.repSampleIncrement);
             behModels = feval(t.behModelClass,paramSamples);
      behaviourSamples = sample(behModels);
   
   %***************************************************************************
   %   Otherwise sample from the posterior parameter distribution
   %***************************************************************************
   else
      
      %************************************************************************
      %   reshape observed parameters for easy presentation to parameter
      %   distribution
      %************************************************************************
      totalNoParams = numel(observedParams)/t.repSampleIncrement/(noTrustees-1);
      newShape = [t.repSampleIncrement totalNoParams (noTrustees-1)];
      observedParams = reshape(observedParams,newShape);
      observedParams = permute(observedParams,[3 2 1]);

      %************************************************************************
      %   sample from a selection of hyperparameter distributions,
      %   each updated with the appropriate parameter sample.
      %************************************************************************
      paramSamples = zeros(t.repSampleIncrement,dims(t.paramModelPrior));

      for i=1:t.repSampleIncrement

         paramDist = observe(t.paramModelPrior,observedParams(:,:,i));
         paramSamples(i,:) = sample(paramDist);

      end

      %************************************************************************
      %   Sample conditional parameter distribution given hyperparameters
      %************************************************************************
      noBehParams = dims(t.directModels{1}); % no. direct behaviour parameters
      reputationIndices = [(noBehParams+1):size(observedParams,2)];
      jointDist = feval(t.paramModelClass,paramSamples);

      if isequal(0,numel(reputationIndices))
         condDist = jointDist;
      else
         condDist = conditional(jointDist,reputationParams,reputationIndices);
      end

      %************************************************************************
      %   Generate behaviour samples from sampled behaviour distributions
      %************************************************************************
       behParamSamples = sample(condDist)';
       behDistribution = feval(t.behModelClass,behParamSamples);
      behaviourSamples = sample(behDistribution);
   
   end % outer if statement
   
   %***************************************************************************
   %   Generate utility samples from behaviour Samples
   %***************************************************************************
     newUtilSamples = fheval(utilFh,behaviourSamples);
        utilSamples = [utilSamples newUtilSamples];

   %***************************************************************************
   %   Update stopping condition
   %***************************************************************************
   stopConditionReached = (numel(utilSamples)>MAX_SAMPLES) || ...
                          feval(t.repStopCondition,utilSamples);

end

  repM = mean(utilSamples);
repVar = var(utilSamples)/numel(utilSamples);

%******************************************************************************
%******************************************************************************
%   TBD: more subfunctions for MCMC methods
%******************************************************************************
%******************************************************************************









