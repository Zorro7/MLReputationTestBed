% Returns the distribution mean
function m = mean(t,varargin)

%*************************************************************************
%   Return mean distribution
%*************************************************************************
m = t.params(:,1:dims(t));

