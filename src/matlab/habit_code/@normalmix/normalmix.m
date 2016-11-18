% NORMALMIX - Multivariate Gaussian Mixture model
classdef normalmix

   properties
      w; % the component weights
      components; % the components 
   end

   methods
      function c = normalmix(varargin)
      % Multivariate Gaussian Mixture model
      %
      % Usage c = normalmix(w,mu,cov) - constructs from mean,cov and weights
      %       c = normalmix         - standard normal
      %       c = normalmix(w,params) - param vector as generated from @nig
      %
      % For c = normalmix(mu,cov,w), mu is 2-D and cov is 3-D, and in each case
      % the last dimension corresponds to the mixture component. For example,
      % a 3-D mixture model with 5 components will have:
      % 
      % size(mu) = [3,5], size(cov) = [3,3,5], size(w) = 3
      %
      % Alternatively, mu and cov can be cell arrays
      % 
      
         %*******************************************************************
         %   If no parameters, use defaults
         %*******************************************************************
         if isequal(nargin,0)
            c.w = 1;
            c.components = multinormal;
            return;
         end

         %*******************************************************************
         %  In any other case, we just pass the arguments on to the
         %  components constructor.
         %*******************************************************************
         c.w = varargin{1};
         c.components = multinormal(varargin{2:end});

      end
   end
end




