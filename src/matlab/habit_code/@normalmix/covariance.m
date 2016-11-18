% Returns the distribution covariance
function c = covariance(t)

%*************************************************************************
%   Return covariance matrix (weighted sum of existing coveriances)
%*************************************************************************
covars = covariance(t.components);

weight = repmat(reshape(t.w,[1 1 numel(t.w)]),...
      [size(covars,1),size(covars,2) 1]);

c = sum(covars.*weight,3);


