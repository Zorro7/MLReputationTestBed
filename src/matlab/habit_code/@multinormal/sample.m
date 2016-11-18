% Draws Samples from distribution
% 
% Usage: s = sample(t,varargin)
%
% varargin in specifies the number of samples required.
%
% If t specifies parameters for more than one multinormal distribution
% then the required number of samples is returned for each specified
% distribution.
%
%
function s = sample(t,varargin)

%*************************************************************************
%   Default number of samples
%*************************************************************************
if isequal(0,numel(varargin))
   varargin = {1};
end

%*************************************************************************
%   Generate standard normal samples first.
%*************************************************************************
s = randn([dims(t) size(t.params,1) varargin{:}]);

%*************************************************************************
%   Transform each sample with the correct covariance
%*************************************************************************
stddev = std(t);

if isequal(ndims(stddev),2)
   stddev = reshape(stddev,[size(stddev) 1]);
end

for i=1:size(stddev,3)

   s(:,i,:) = (squeeze(s(:,i,:))'*stddev(:,:,i))';

end

%*************************************************************************
%   Add the correct mean
%*************************************************************************
m = repmat(mean(t)',[1,1,prod(varargin{:})]);

s(:,:,:) = s(:,:,:) + m;

s = squeeze(s);



