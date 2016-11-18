% Multivariate Normal-Gamma Model
% In fact, this is a Multivariate normal-wishart model, as it
% is conjugate to multivariate normal distributions
classdef nig
   
   
   properties (SetAccess = private)
      
      %*********************************************************************
      %  Hyperparameters for Normal-Gamma Model
      %  The parameters are for a multivariate gaussian, then these
      %  represent the corresponding normal-wishart hyperparameters.
      %*********************************************************************
      a = 10^-10;
      b = 10^-20;
      v = 10^-50;
      m = 0;
      
      %*********************************************************************
      %   Place holder for Cholesky decomposition of b^(-1), this is
      %   calculated just-in-time when required, but wiped during periods of
      %   consecutive updates to b
      %
      %   A value of -1 indicates that the decomposition needs to be
      %   recalculated.
      %*********************************************************************
      binvCholesky = -1;
          
   end
   
   methods
      
      function t = nig(a,b,v,m)
         % Constructor Multivariate Normal-Gamma Model
         % In fact, this is a Multivariate normal-wishart model, as it
         % is conjugate to multivariate normal distributions
         %
         % Usage: n = nig(a,b,v,m) where a,b,v,m are hyperparameters
         %
         % or: n = nig; -> equivalent to n=nig(1,1,1,0)
         %
         % or: n = nig(n); -> n is number of dimensions
         %
         
         %*********************************************************************
         %    Set defaults
         %*********************************************************************
         if isequal(nargin,1)
            t.a=0;
            t.b=t.b.*eye(a);
            t.v=0;
            t.m=repmat(t.m,a,1);
            
         elseif isequal(nargin,4)
            t.a=a;
            t.b=b;
            t.v=v;
            t.m=m;
            
         else
            error('Wrong number of arguments for nig constructor');
         end
         
      end
      
   end
   
end
            

