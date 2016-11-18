% Multivariate Gaussian Distribution Class
%
% Usage c = clippedNormal(mu,cov) - constructs from mean and covariance
%       c = clippedNormal         - standard normal
%       c = clippedNormal(params) - param vector as generated from @nig
% 
function c = clippedNormal(varargin)

%*************************************************************************
%   Pop out of range function of the variable argument list.
%*************************************************************************
if nargin>0
   if isa(varargin{1},'char') || isa(varargin{1},'function_handle')
      c.outOfRange = varargin{1};
      varargin = varargin(2:end);
   else
      c.outOfRange = @defaultRange;
   end
else
   c.outOfRange = @defaultRange;
end

%*************************************************************************
%   Otherwise use default (everything must sum to one);
%*************************************************************************

%*************************************************************************
%   Check parameters
%*************************************************************************
if isequal(nargin,0)

  c.params = [0 1];

elseif isequal(nargin,1)

   c.params = varargin{1};

elseif isequal(nargin,2)

   if ~isequal(numel(mu),size(covariance,1),size(covariance,2))
      error('mean and covariance size mismatch');
   end

   mu = varargin{1}; covariance = varargin{2};
   c.params = [mu vech(chol(inv(covariance),'lower'))];

end

%*************************************************************************
%   construct object
%*************************************************************************
c = class(c,'clippedNormal',distribution);

%*************************************************************************
%*************************************************************************
%   Default Range function
%*************************************************************************
%*************************************************************************
function ind = defaultRange(s)

%*************************************************************************
%   Out of range if sum of elements is greater than 1
%*************************************************************************
sampleSum = sum(s)<1;






