% Evaluate adaptive function handle.
%
% Usage: [y fhu] = fheval(fh,x);
%
% x   - input
% y   - output
% fh  - function handle
% fhu - function handle with updated state information
%
function [y fh] = fheval(fh,x)

%******************************************************************************
%   If there are no previously returned values, calculated the requested
%   values and store them for updating the posterior distribution.
%******************************************************************************
if isequal(0,numel(fh.prevX))

   fh.prevX = x;
   [mu fh.m] = fheval(fh.m,x);
   covar = feval(fh.covf,fh.loghyper,x);
   fh.prevY = mvnrnd(mu,covar)';
   y = fh.prevY;
   return;
end

%******************************************************************************
%   Now check for any previously requested values and retrieve them.
%******************************************************************************
y = zeros(size(x,1),1);
[newInd oldInd] = ismember(x,fh.prevX,'rows');
unSeenInd = ~newInd;
y(newInd) = fh.prevY(oldInd(oldInd>0));

%******************************************************************************
%   If there are no other values we are done.
%******************************************************************************
if all(newInd)
   return;
end

%******************************************************************************
%   Get the covariance matrix for the requested points and all points
%   previously requested from this function.
%******************************************************************************
allX = [x(unSeenInd,:); fh.prevX];
jointCov = feval(fh.covf,fh.loghyper,allX);
jointMu  = fheval(fh.m,allX);

%******************************************************************************
%   Calculate conditional parameters for newly requested points
%******************************************************************************
condSize = sum(unSeenInd);
cov11 = jointCov(1:condSize,1:condSize);
cov12 = jointCov(1:condSize,(condSize+1):end);
cov22 = jointCov((condSize+1):end,(condSize+1):end);
mu1   = jointMu(1:condSize);
mu2   = jointMu((condSize+1):end);

condCov = cov11 - cov12*inv(cov22)*cov12';
condMu  = mu1 + cov12*inv(cov22)*(fh.prevY-mu2);

%******************************************************************************
%   Quick hack to increase the chance of positive definite: reduce all
%   correlations that have been mistakenly computed with greater magnitude
%   than one, and ensure the matrix is symmetric.
%******************************************************************************
s = diag(sqrt(diag(condCov))); sinv = inv(s);
correlations  = sinv*condCov*sinv;
outOfRangeInd = (abs(correlations) > 1) & (eye(length(correlations))>1);
correlations(outOfRangeInd) = 1;
condCov = s*correlations*s;
condCov = tril(condCov) + tril(condCov)';

%******************************************************************************
%   Generate new points, adding the mean function and store new points in
%   point history.
%******************************************************************************
y(unSeenInd) = mvnrnd(condMu,condCov);
fh.prevY = [y(unSeenInd); fh.prevY];
fh.prevX = allX;

