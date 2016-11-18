% Returns the distribution covariance
function c = covariance(t)

%*************************************************************************
%   Sum required to calculate covariance
%*************************************************************************
as = sum(t.a(:));

%*************************************************************************
%   Calculate covariance matrix from definition
%*************************************************************************
c = (-t.a)'*(t.a) + diag(as*t.a);

c = c / ( (as^2)*(as+1) );
