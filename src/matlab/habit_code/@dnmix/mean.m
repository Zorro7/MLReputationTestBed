% Returns the distribution mean
% Usage: m = mean(t,v)
% if v is present, then it is used as the values of the discrete
% part (TBD)
%
function m = mean(t,varargin)

%*************************************************************************
%   If any values have been passed in, use them as the domain of the
%   discrete part. At the moment we just use 1:n
%*************************************************************************
discreteValues  = [1:numel(t.n)]';
discreteValueSize = size(discreteValues,2);


%*************************************************************************
%   Calculate Dirchlet mean, which gives us the marginal probabilities
%*************************************************************************
dirichMean = mean(t.d);

%*************************************************************************
%   The mean of the descrete variable the weighted sum of its values
%   Actually, this is only meaningful if the values we've assigned
%   really mean something.
%*************************************************************************
m = zeros(1,discreteValueSize+dims(t.n{1}));

m(1:discreteValueSize) = sum(discreteValues(:) .* t.d.a(:));

%*************************************************************************
%   Continous part is weighted sum of the other means (TBD)
%*************************************************************************
