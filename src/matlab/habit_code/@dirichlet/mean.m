% Returns the distribution mean
function m = mean(t,varargin)

%*************************************************************************
%   Calculate mean from definition
%*************************************************************************
m = t.a/sum(t.a(:));
