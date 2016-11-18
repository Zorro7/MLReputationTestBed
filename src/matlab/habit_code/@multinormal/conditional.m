% MULTINORMAL/CONDITIONAL return conditional distribution
%
% Usage condDist = conditional(dist,values,valueIndices)
%
% values       - observed values
% valueIndices - indices of observed values
%
function condDist = conditional(dist,values,valueIndices)

%******************************************************************************
%   Make input row vectors
%******************************************************************************
valueIndices = valueIndices(:)';

if isvector(values)
   values = repmat(values,size(dist.params,1),1);
else
   values = values(:,:);
end

%******************************************************************************
%   Check indices are in range
%******************************************************************************
if any(valueIndices>dims(dist))
   error('multinormal/conditional: some indices are out of range');
end

%******************************************************************************
%   Check values and number of indices match
%******************************************************************************
if ~isequal(size(values,2),numel(valueIndices))
   error('number of values and indices must match');
end

%******************************************************************************
%   Check we have not tried to observe something twice
%******************************************************************************
if ~isequal(size(valueIndices),size(unique(valueIndices)))
   error('Duplicate observations for the same index not allowed');
end

%******************************************************************************
%   Retrieve joint mean and variance
%******************************************************************************
jointMu  = mean(dist);
jointVar = covariance(dist);

%******************************************************************************
%   Pertubate the mean and covariance so that the observed values are
%   at the end
%******************************************************************************
newOrder = [setdiff([1:dims(dist)],valueIndices) valueIndices];
jointMu  = jointMu(:,newOrder);
jointVar = jointVar(newOrder,newOrder,:);

%******************************************************************************
%   Split the joint mean and variance into their composite parts
%******************************************************************************
s2 = numel(valueIndices); % size of   observed vector
s1 = dims(dist) - s2;     % size of unobserved vector

m1 = jointMu(:,1:s1);
m2 = jointMu(:,1+s1:end);

V11 = jointVar(1:s1,1:s1,:);
V12 = jointVar(1:s1,1+s1:end,:);
V22 = jointVar(1+s1:end,1+s1:end,:);
V21 = jointVar(1+s1:end,1:s1,:);

%******************************************************************************
%   Calculate the condition parameters
%******************************************************************************
newParamSize = 2*s1 + (s1^2-s1)/2;
newParams = zeros(size(dist.params,1),newParamSize);

for i=1:size(jointMu,1)

   %***************************************************************************
   %   Calculate the conditional mean
   %***************************************************************************
   newMu = m1(i,:)' + V12(:,:,i) * inv(V22(:,:,i)) * ( values(i,:)-m2(i,:) )';
   newMu = newMu';

   %***************************************************************************
   %   Calculate the conditional variance
   %***************************************************************************
   newVar = V11(:,:,i) - V12(:,:,i) * inv(V22(:,:,i)) * V21(:,:,i);

   %***************************************************************************
   %   Calculate the cholesky decomposition of the precision matrix
   %***************************************************************************
   newPrecChol = chol(inv(newVar),'lower');

   %***************************************************************************
   %   Generate ith parameter vector
   %***************************************************************************
   newParams(i,1:s1) = newMu;
   newParams(i,1+s1:end) = vech(newPrecChol);

end

%******************************************************************************
%   Generate conditional distribution object from conditional parameter
%   vector.
%******************************************************************************
condDist = multinormal(newParams);






