% MULTINORMAL - Multivariate Gaussian Distribution Class
% This is capable of representing multiple distributions in a single object
classdef multinormal

   properties
      params = [0 1]; % default is single standard normal distribution
   end

   methods

      function c = multinormal(varargin)
      % Multivariate Gaussian Distribution Class
      %
      % Usage c = multinormal(mu,cov) - constructs from mean and covariance
      %       c = multinormal         - standard normal
      %       c = multinormal(params) - param vector as generated from @nig
      %       c = multinormal(mu,prc,false) - same as
      %              multinormal(mu,cov) accept prc is the precision matrix
      %              rather than the covariance.
      %
      %  mu(k,:) and cov(:,:,k) is are the mean and covariance matrix
      %  of the kth distribution.
      % 

         %*******************************************************************
         %  If no arguments, use defaults
         %*******************************************************************
         if isequal(0,nargin)
            return;
         end

         %*******************************************************************
         %  Throw an error if we have too many arguments
         %*******************************************************************
         if nargin>3
            error('Too many arguments');
         end
      
         %*******************************************************************
         %   Deal with single argument case
         %*******************************************************************
         if isequal(nargin,1)
            c.params = varargin{1};
            return;
         end
         
         %*******************************************************************
         %  Check if we have precision or covariance matrix
         %*******************************************************************
         if isequal(nargin,3)
            gotCov = varargin{3};
         else
            gotCov = true;
         end

         %*******************************************************************
         %  Get the means (convert from cell if necessary)
         %*******************************************************************
         mu = varargin{1};
         if iscell(mu)
            nmu = zeros(numel(mu),numel(mu{1}));
            for k=1:numel(mu)
               nmu(k,:) = reshape(mu{k},[1 size(nmu,2)]);
            end
            mu = nmu;
         end

         %*******************************************************************
         %  Get the precision matrices (convert from cell if necessary)
         %*******************************************************************
         arg2 = varargin{2};
         if iscell(arg2)
            narg = zeros([size(arg2{1}) numel(arg2)]);
            for k=1:numel(mu)
               narg(:,:,k) = arg2{k};
            end
            arg2 = narg;
         end

         if gotCov
            covariance = arg2;
            precision = zeros(size(covariance));
            for k=1:size(precision,3)
               precision(:,:,k) = inv(covariance(:,:,k));
            end
         else
            precision = arg2;
         end

         %*******************************************************************
         %  Perform some sanity checks
         %*******************************************************************
         if ~isequal(size(mu,2),size(precision,1),size(precision,2))
            error('mean and covariance size mismatch');
         end

         %*******************************************************************
         %  Allocate space for the params vectors and copy in the means
         %*******************************************************************
         noDists = size(mu,1); noDims = size(mu,2);
         noParams = noDims + noDims*(noDims+1)/2;
         c.params = zeros(noDists,noParams);
         c.params(:,1:noDims) = mu;

         %*******************************************************************
         %  Calculate the lower triangular decomposition of the precision
         %  and use the to vectorise the parameters.
         %  This parameterisation is more compact and helps speed things
         %  up later on.
         %*******************************************************************
         for k=1:noDists
            c.params(k,(noDims+1):end) = vech(chol(precision(:,:,k),'lower'));
         end
      end
   end
end



