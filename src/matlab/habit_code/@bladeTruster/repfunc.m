% BLADETRUSTER/REPFUNC CPT for given source trustee pair
%
% Usage: cpt = repfunc(t,trustee,source)
%
%   where
%
%      source : reputation source id
%     trustee : trustee id
%         cpt : conditional probability table representing
%               joint distribution of trustee behaviour and
%               reputation opinions.
%
%     cpt is indexed as cpt(behaviour,opinion)
%
% TODO: this is an intuitative guess as to how this is calculated. Need
% to check the math.
%
function cpt = repfunc(t,trustee,source)

%******************************************************************************
%   Initial CPT with uniform prior (unnormalised)
%******************************************************************************
cpt = ones(dims(t.dirModelPrior),dims(t.repModelPrior));

%******************************************************************************
%   Update the cpt using the opinions for all other trustees as evidence
%******************************************************************************
for otherTrustee = [1:(trustee-1) (trustee+1):size(t.repModels,1)];

   %***************************************************************************
   %   Retrieve opinion observations for this trustee
   %***************************************************************************
   repModel = t.repModels{otherTrustee,source};
   opinionObs = repModel.a - t.repModelPrior.a;

   %***************************************************************************
   %   Use belief distribution for the trustee to spread the observations
   %   apropriately across the CPT
   %***************************************************************************
   dirModel = t.directModels{otherTrustee};
   %update = repmat(mean(dirModel)',1,dims(repModel)) .* ...
   %         repmat(opinionObs,dims(dirModel),1);
   
   update = mean(dirModel)' * opinionObs; % this is quicker
   
   cpt = cpt + update;

end

%******************************************************************************
%   Normalise CPT w.r.t trustee behaviour
%******************************************************************************
cpt = cpt ./ sum(cpt(:));

























