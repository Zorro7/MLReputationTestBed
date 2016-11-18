% Returns the distribution precision
function p = precision(t)

%*************************************************************************
%   Return covariance matrix (weighted sum of existing coveriances)
%*************************************************************************
precs = precision(t.components);

weight = repmat(reshape(t.w,[1 1 numel(t.w)]),...
      [size(precs,1),size(precs,2) 1]);

p = sum(precs.*weight,3);


