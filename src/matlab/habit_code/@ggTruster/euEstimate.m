% TRUSTER/euEstimate
%
% Usage: [m e] = euEstimate(t,trustee,utilFh);
%
function [m e w] = euEstimate(t,trustee,~)

% the weights vector 'w' is only applicable to gdTrusters.
% Therefore, we just return nans.
w = nan(1,t.noTrustees);

%******************************************************************************
%   Generate estimate based on direct experience.
%******************************************************************************
[dirM dirVar] = directEstimateInd(t,trustee);

%******************************************************************************
%   Generate estimate based on reputation
%******************************************************************************
[repM repVar resultFlag] = reputationEstimateInd(t,trustee);

%******************************************************************************
%   Calculate estimate and approximate standard error.
%******************************************************************************
if resultFlag
   
   %***************************************************************************
   %   If the direct estimate is based on the prior (i.e. there is no direct
   %   experience) then just return the reputation based estimate.
   %***************************************************************************
   directModel = t.directModels{trustee};
   priorModel = t.dirModelPrior;
   if isequal(directModel.a,priorModel.a)
      m = repM;
      e = repVar;
      return;
   end
   
   %***************************************************************************
   %   Sanity check variances before combining estimates.
   %***************************************************************************
   if 0>=dirVar
      error(['direct variance is invalid: ' dirVar]);
   end
   
   if 0>=repVar
      error(['reputation variance is invalid: ' repVar]);
   end

   %***************************************************************************
   %   Calculate estimate
   %***************************************************************************
   totPrec = 1/dirVar + 1/repVar;
         m = (dirM/dirVar + repM/repVar)/totPrec;
         e = sqrt(1/totPrec);
         
else
   m = dirM;
   e = dirVar;
end

%******************************************************************************
%******************************************************************************
%   Subfunction for direct estimation with independent samples.
%   This can and is achieved analytically in this case, since the domain
%   is discrete.
%******************************************************************************
%******************************************************************************
function [dirM dirVar] = directEstimateInd(t,trustee)

%******************************************************************************
%   Retrieve appropriate direct observation model
%******************************************************************************
model = t.directModels{trustee};
dirM = mean(model);
dirVar = covariance(model);

%******************************************************************************
%******************************************************************************
%   Subfunction for reputation based estimation with indepedent samples
%******************************************************************************
%******************************************************************************
function [repM repVar result] = reputationEstimateInd(t,trustee)

%******************************************************************************
%   First, check that we have enough trustees to from a valid expectation
%   based on our assumed prior hyperparameters. If not, then we set the
%   result flag to false to indicate that we might as well rely only on our
%   direct experience.
%   (If we did proceed, we would only end up generating infinite covariances)
%******************************************************************************
if t.noTrustees < t.MIN_TRUSTEES;
    repM = 0;
    repVar = 0;
    result = false;
    return;
end

%******************************************************************************
%  Also check that each confidence model has sufficient information to allow
%  sampling. We should really allow things to proceed even if only a subset
%  or reputation source models allow sampling, but this way is easier for now.
%******************************************************************************
if ~all(cellfun(@isSamplingOK,t.repModels(:)))
    [repM repVar result] = groupEstimateInd(t,trustee);
    return;
end


%******************************************************************************
%   Sample joint parameters from opinion models.
%   For each model, we strip off the first dimension of each sample.
%   We do this because the sum of the elements of each sample must add to
%   1. It is both impractical (due to likelihood of singular covariance
%   matrices) and unnecessary for the gaussian model to learn this. We
%   can always regenerate the first component later by subtracting the
%   sum of the modelled parameters from 1.
%******************************************************************************
opSamples = cell(size(t.repModels)); % sampled parameters
dirSamples = cell(numel(t.directModels),1);

for trIt = 1:size(t.repModels,1)
   
   for sIt = 1:size(t.repModels,2)
      curOpSamples = sample(t.repModels{trIt,sIt},t.noParamSamples);
      opSamples{trIt,sIt}=curOpSamples;
   end
   
   if ~isequal(trIt,trustee)
      curDirSamples = sample(t.directModels{trIt},t.noParamSamples);
      dirSamples{trIt} = curDirSamples;
   end
   
end

dirSamples = dirSamples([1:(trustee-1),(trustee+1):size(t.repModels,1)]);

%******************************************************************************
%   Select reputation parameters for the trustee in question
%******************************************************************************
reputationParams = [opSamples{trustee,:}];

%******************************************************************************
%   Select parameter samples for learning
%******************************************************************************
observedParams = ...
   [dirSamples opSamples([1:(trustee-1),(trustee+1):t.noTrustees],:)]';

observedParams = [observedParams{:}];

%******************************************************************************
%   If there are no observed samples, return a null result
%******************************************************************************
if isequal(0,numel(observedParams))
    repM = 0; repVar = 0; result = false;
   return;
end

%***************************************************************************
%   reshape observed parameters for easy presentation to parameter
%   distribution
%***************************************************************************
totalNoParams = numel(observedParams)/t.noParamSamples/(t.noTrustees-1);
newShape = [t.noParamSamples totalNoParams (t.noTrustees-1)];
observedParams = reshape(observedParams,newShape);
observedParams = permute(observedParams,[3 2 1]);

%***************************************************************************
%   Sample outcomes using the predictive parameter distribution conditioned
%   on the trustee's reputation.
%***************************************************************************
noBehParams = dims(t.directModels{1}); % no. direct behaviour parameters
outcomeSamples = nan(1,t.noParamSamples);


for i=1:t.noParamSamples

   %************************************************************************
   %   Generate moments for the current observation samples
   %************************************************************************
   paramDist = observe(t.paramModelPrior,observedParams(:,:,i));

   trusteeParamSample = -ones(2,10);
   
   try
      while all(trusteeParamSample(2,:)<=0)
         trusteeParamSample = ...
            predictiveSample(paramDist,reputationParams(i,:),...
            (noBehParams+1):size(observedParams,2),10);
      end
      
      posPrec = find(trusteeParamSample(2,:) > 0,1);
      
      trusteeParamSample = trusteeParamSample(:,posPrec);
      
      
      sampledBehaviourDistributions = multinormal(trusteeParamSample');
      
      outcomeSamples(i) = sample(sampledBehaviourDistributions);
      
      
   catch
      dummy=0;
   end
   
   
end

%******************************************************************************
%   Return results (by setting result flag to true)
%******************************************************************************
repM = nanmean(outcomeSamples);
repVar = nanvar(outcomeSamples);
fprintf('proportion of nan samples: %f\n',mean(isnan(outcomeSamples(:))));
result = true;

%******************************************************************************
%******************************************************************************
%   Subfunction for estimate based on group behaviour only
%   Basically the same as a reputation estimate, but with reputation ignored
%******************************************************************************
%******************************************************************************
function [repM repVar result] = groupEstimateInd(t,trustee)

%******************************************************************************
%   First, check that we have enough trustees to from a valid expectation
%   based on our assumed prior hyperparameters. If not, then we set the
%   result flag to false to indicate that we might as well rely only on our
%   direct experience.
%   (If we did proceed, we would only end up generating infinite covariances)
%******************************************************************************
if t.noTrustees < t.MIN_TRUSTEES;
    repM = 0;
    repVar = 0;
    result = false;
    return;
end

if ~all(cellfun(@isSamplingOK,t.directModels([1:(trustee-1),...
      (trustee+1):numel(t.directModels)])))
    repM = 0;
    repVar = 0;
    result = false;
    return;
end

%******************************************************************************
%   Sample joint parameters from opinion models.
%   For each model, we strip off the first dimension of each sample.
%   We do this because the sum of the elements of each sample must add to
%   1. It is both impractical (due to likelihood of singular covariance
%   matrices) and unnecessary for the gaussian model to learn this. We
%   can always regenerate the first component later by subtracting the
%   sum of the modelled parameters from 1.
%******************************************************************************
dirSamples = cell(numel(t.directModels),1);

for trIt = 1:numel(t.directModels)
   
   if ~isequal(trIt,trustee)
      curDirSamples = sample(t.directModels{trIt},t.noParamSamples);
      dirSamples{trIt} = curDirSamples;
   end
   
end

dirSamples = dirSamples([1:(trustee-1),(trustee+1):numel(t.directModels)]);

%******************************************************************************
%   Select parameter samples for learning
%******************************************************************************
observedParams = [dirSamples{:}];

%******************************************************************************
%   If there are no observed samples, return a null result
%******************************************************************************
if isequal(0,numel(observedParams))
    repM = 0; repVar = 0; result = false;
   return;
end

%***************************************************************************
%   reshape observed parameters for easy presentation to parameter
%   distribution
%***************************************************************************
totalNoParams = numel(observedParams)/t.noParamSamples/(t.noTrustees-1);
newShape = [t.noParamSamples totalNoParams (t.noTrustees-1)];
observedParams = reshape(observedParams,newShape);
observedParams = permute(observedParams,[3 2 1]);

%***************************************************************************
%   Sample outcomes using the predictive parameter distribution conditioned
%   on the trustee's reputation.
%***************************************************************************
outcomeSamples = nan(1,t.noParamSamples);


for i=1:t.noParamSamples

   %************************************************************************
   %   Generate moments for the current observation samples
   %************************************************************************
   paramDist = observe(nig(2),observedParams(:,:,i)); % hack of a prior

   trusteeParamSample = -ones(2,10);
   
   try
      while all(trusteeParamSample(2,:)<=0)
         trusteeParamSample = ...
            predictiveSample(paramDist,[],[],10);
      end
      
      posPrec = find(trusteeParamSample(2,:) > 0,1);
      
      trusteeParamSample = trusteeParamSample(:,posPrec);
      
      
      sampledBehaviourDistributions = multinormal(trusteeParamSample');
      
      outcomeSamples(i) = sample(sampledBehaviourDistributions);
      
      
   catch
      dummy=0;
   end
   
   
end

%******************************************************************************
%   Return results (by setting result flag to true)
%******************************************************************************
repM = nanmean(outcomeSamples);
repVar = nanvar(outcomeSamples);
fprintf('proportion of nan samples: %f\n',mean(isnan(outcomeSamples(:))));
result = true;

