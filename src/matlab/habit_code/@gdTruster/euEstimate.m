% TRUSTER/euEstimate
%
% Usage: [m e] = euEstimate(t,trustee,utilFh);
%
function [m e] = euEstimate(t,trustee,utilFh)

%******************************************************************************
%   Generate estimate based on direct experience.
%******************************************************************************
[dirM dirVar] = directEstimateInd(t,trustee,utilFh);

%******************************************************************************
%   Generate estimate based on reputation
%******************************************************************************
[repM repVar resultFlag] = reputationEstimateInd(t,trustee,utilFh);

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
function [dirM dirVar] = directEstimateInd(t,trustee,utilFh)

%******************************************************************************
%   Retrieve appropriate direct observation model
%******************************************************************************
model = t.directModels{trustee};
distMu = mean(model);
distCov = covariance(model);

%******************************************************************************
%   Calculate utility for each element of domain
%******************************************************************************
utilDomain = fheval(utilFh,t.dirModelPrior.d);

%******************************************************************************
%   Calculate expected utility using the predictive distribution
%******************************************************************************
dirM = sum( utilDomain .* distMu );

%******************************************************************************
%   Calculate expected utility variance according to definition
%******************************************************************************
squUtil = utilDomain' * utilDomain;
utilCov = squUtil .* distCov;
dirVar = sum(utilCov(:));

%******************************************************************************
%******************************************************************************
%   Subfunction for reputation based estimation with indepedent samples
%******************************************************************************
%******************************************************************************
function [repM repVar result] = ...
   reputationEstimateInd(t,trustee,utilFh)

%******************************************************************************
%   First, check that we have enough trustees to from a valid expectation
%   based on our assumed prior hyperparameters. If not, then we set the
%   result flag to false to indicate that we might as well rely only on our
%   direct experience.
%   (If we did proceed, we would only end up generating infinite covariances)
%******************************************************************************
noTrustees = size(t.repModels,1);
if 0 >= (t.paramModelPrior.a+noTrustees-size(t.paramModelPrior.m,1)-2)
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
opSamples = cell(size(t.repModels)); % sampled parameters
dirSamples = cell(numel(t.directModels),1);

for trIt = 1:size(t.repModels,1)
   
   for sIt = 1:size(t.repModels,2)
      curOpSamples = sample(t.repModels{trIt,sIt},t.noParamSamples);
      curOpSamples = curOpSamples(:,2:end); % strip off 1st component
      opSamples{trIt,sIt}=curOpSamples;
   end
   
   if ~isequal(trIt,trustee)
      curDirSamples = sample(t.directModels{trIt},t.noParamSamples);
      curDirSamples = curDirSamples(:,2:end); % strip off 1st component
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
   [dirSamples opSamples([1:(trustee-1),(trustee+1):noTrustees],:)]';

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
totalNoParams = numel(observedParams)/t.noParamSamples/(noTrustees-1);
newShape = [t.noParamSamples totalNoParams (noTrustees-1)];
observedParams = reshape(observedParams,newShape);
observedParams = permute(observedParams,[3 2 1]);

%***************************************************************************
%   Sample from the predictive parameter distribution conditioned on
%   the trustee's reputation.
%***************************************************************************
noBehParams = dims(t.directModels{1}); % no. direct behaviour parameters
meanSamples = zeros(t.noParamSamples,noBehParams);
SigmaSamples= zeros(noBehParams,noBehParams,t.noParamSamples);
diagCorr = zeros(totalNoParams,totalNoParams,t.noParamSamples);
sampleCorr = diagCorr;
diagMean =zeros(t.noParamSamples,totalNoParams);


for i=1:t.noParamSamples

   %************************************************************************
   %   Generate moments for the current observation samples
   %************************************************************************
   paramDist = observe(t.paramModelPrior,observedParams(:,:,i));

   [meanSamples(i,2:end) SigmaSamples(2:end,2:end,i) diagCorr(:,:,i)] = ...
      predictiveMoments(paramDist,reputationParams, ...
         [noBehParams:size(observedParams,2)]);
      
   diagMean(i,:) = predictiveMoments(paramDist,[],[]);
      
   sampleCorr(:,:,i) = corrcoef(observedParams(:,:,i));

   %************************************************************************
   %   Fill in missing parameters 
   %************************************************************************
   meanSamples(i,1) = 1-sum(meanSamples(i,2:end));
   SigmaSamples(1,1,i) = sum(diag(SigmaSamples(:,:,i)));
   SigmaSamples(1,2:end,i) = -sum(SigmaSamples(2:end,2:end,i));
   SigmaSamples(2:end,1,i) = SigmaSamples(1,2:end,i);

end

meanDiagCorr = zeros(totalNoParams);
meanDiagCorr = mean(diagCorr,3);
meanSampleCorr = mean(sampleCorr,3);
meanDiagMean = mean(diagMean);

%***************************************************************************
%   Calculate sample mean for expected utility
%***************************************************************************
utilDomain = fheval(utilFh,t.dirModelPrior.d);
utilSamples = repmat(utilDomain,size(meanSamples,1),1) .* meanSamples;
repM = mean(sum(utilSamples,2));

%***************************************************************************
%   Approximate the estimation variance
%***************************************************************************
sampleCov = mean(SigmaSamples,3);
squUtil = utilDomain' * utilDomain;
utilCov = squUtil .* sampleCov;
repVar = sum(utilCov(:));

%******************************************************************************
%   Return results (by setting result flag to true)
%******************************************************************************
result = true;



