% TRUSTER/directObserve updates specified trustee belief model
%
% Usage: t = directObserve(t,trustee,obs);
%
% Updates the specified trustee's belief model with specified
% direct behaviour observations. Any previous observations are lost.
%
function t = directObserve(t,trustee,obs)

%******************************************************************************
%   Update trustee's belief model
%******************************************************************************
t.directModels{trustee} = observe(t.dirModelPrior,obs);



