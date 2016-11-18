% Returns the distribution mean
function m = mean(t,varargin)

%*************************************************************************
%   Calculate mean from definition
%*************************************************************************
m = sum(t.a(:).*t.v(:))
