% returns expected value
% [m p] = ev(t)
% m - expected mean
% p - expected precision
function [m p] = mean(t)

m = t.m;

if isequal(2,nargout)
   p = t.a*inv(t.b);
end


