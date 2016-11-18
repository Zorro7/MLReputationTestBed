% GIMMTRUSTER Gaussian confidence models with reputation model instantiated
% by Gaussian Infinite Mixture Model
classdef gimmTruster
   
   properties (SetAccess = private)
      
      noSources = 1;
      noTrustees = 1;
      
      repModelPrior = nig(10^-10,10^-20,10^-50,0); % proper uninformative prior
      dirModelPrior = nig(10^-10,10^-20,10^-50,0);

      % NOTE prior for parameter (reputation) model not required here
      % because the vdpgm library seems to choose something sensible
      % based on the data. Not a bad idea provided you have data to
      % base it on!
      
      directModels;
      repModels;
      
   end
   
   properties (Constant)
      noParamSamples = 500;
      noSamplesPerModel = 1;
      MIN_TRUSTEES = 5; % minimum number of trustees for propr param dist.
   end
   
   methods
      
      function t = gimmTruster(noTrustees)
         % GIMMTRUSTER/GIMMTRUSTER Gaussian Infinite Mixture Model Truster
         %
         %  Usage: t = gimmTruster(noTrustees); or t = gimmTruster;
         %
         %  Currently assume number of reputation sources is 1.
         %  No of trustees also defaults to 1 if not provided.
         %
         
         %*********************************************************************
         %   Initial input structure if none provided
         %*********************************************************************
         if ~isequal(nargin,0)
            t.noTrustees = noTrustees;
         end
         
         %*********************************************************************
         %   Initialise direct and reputation models
         %*********************************************************************
         t.directModels = cell(1,t.noTrustees);
         t.repModels = cell(t.noTrustees,t.noSources);
         
         [t.directModels{:}] = deal(t.dirModelPrior);
         [t.repModels{:}] = deal(t.repModelPrior);
         
      end
      
   end
   
end

