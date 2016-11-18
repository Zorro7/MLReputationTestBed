% Returns the distribution mean
function m = mean(t,varargin)

%*************************************************************************
%   Return distribution mean
%*************************************************************************
m = t.w * mean(t.components);

