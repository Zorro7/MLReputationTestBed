% TRUSTER/euEstimate
%
% Usage: [m e] = euEstimate(t,trustee,utilFh);
%
function [m e] = euEstimate(t,trustee,utilFh)

%******************************************************************************
%   Retrieve direct observation model for trustee and use it to calculate
%   the prior mariginal behaviour distribution
%******************************************************************************
priorModel = t.directModels{trustee};
postModel = priorModel;

%******************************************************************************
%   Update model iteratively with respect to all available opinions
%******************************************************************************
for source = 1:size(t.repModels,2)

   %***************************************************************************
   %   Retrieve CPT for current source, along with the source model
   %***************************************************************************
   cpt = repfunc(t,trustee,source);
   sourceModel = t.repModels{trustee,source};
   
   %***************************************************************************
   %   Calculate the reputation distributions conditioned on the hidden and
   %   prior observed data.
   %***************************************************************************
   Rcond = cpt ./ repmat(sum(cpt,2),1,size(cpt,2));         % prior conditional
   Rcond = Rcond .* repmat(mean(priorModel)',1,size(cpt,2));% posterior
   Rcond = Rcond ./ repmat(sum(Rcond,1),size(cpt,2),1);     % normalise
   
   %***************************************************************************
   %  Update the behaviour distribution in light of reputation
   %***************************************************************************
   opinions = sourceModel.a - 1;
   
   opinionLikelihood = Rcond .* repmat(opinions,size(cpt,1),1);
   
   totalLikelihood = sum(opinionLikelihood');
   
   postModel.a = postModel.a + totalLikelihood;
  
end

%******************************************************************************
%   Calculate utility for each element of domain
%******************************************************************************
utilDomain = fheval(utilFh,t.dirModelPrior.d);

%******************************************************************************
%   Calculate expected utility using the predictive distribution
%******************************************************************************
m = sum( utilDomain .* mean(postModel) );

%******************************************************************************
%   Calculate (expected utility)^2 according to definition.
%   This is only an estimate, because we only have one dirichlet instead of
%   the true mixture.
%******************************************************************************
squUtil = utilDomain' * utilDomain;
utilCov = squUtil .* covariance(postModel);
expSquUtil = sum(utilCov(:));

%******************************************************************************
%   Approximate standard error
%******************************************************************************
e = sqrt(expSquUtil - m^2);





