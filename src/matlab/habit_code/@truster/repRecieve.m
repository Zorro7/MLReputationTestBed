% TRUSTER/REPRECEIVE updates specified reputation source belief model
%
% Usage: t = repPeceive(t,trustee,source,obs);
%
% Updates the specified reputation source belief model with specified
% direct behaviour observations. Any previous observations are lost.
%
% parameters:
%                t  : the truster handle
%           trustee : the trustee's id
%            source : the reputation source's id
%               obs : the reported observations
%
function t = repReceive(t,trustee,source,obs)

%******************************************************************************
%   Update truster's belief model for trustee/source pair.
%******************************************************************************
t.repModels{trustee,source} = observe(t.repModelPrior,obs);




