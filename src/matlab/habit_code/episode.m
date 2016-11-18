% EPISODE: episode for new travos-c simulations.
%
% Usage [m e t] = episode(params)
%
% params is a structure with the following fields
%
%       utilFh : funch handle representing utility function.
%     trusters : cell array of trusters.
%     trustees : cell array of trustee distributions for training.
%   repSources : cell array of reputation source functions.
%  noDirectObs : matrix containing number of direct observations each
%                truster gets of each trustee.
%     noRepObs : matrix containing number of observations each reputation
%                source gets of each trustee.
%    noPIEUObs : no of samples with perfect information used to estimate
%                true expected utility.
%
%  In both noDirectObs and noRepObs, each column refers to a specific
%  trustee and each row a specific observer.
%
%  The function returns three vectors:
%
%            m : sample mean for each EU estimate
%            e : standard error for each EU estimate
%            t : elapsed time in seconds
%  
%  All estimates are made with reference to the first trustee distribution
%  in the trustees array only. The first element in each output vector
%  is produced using complete information of the trustee distribution and the
%  rest are given by the trusters in the order that they appear in the
%  trusters array.
%
%  The first element of e is std/sqrt(n(1)), where std is standard deviation
%  of the samples used to estimate the true EU given the true trustee
%  behaviour distribution. The other elements of e are reported by the
%  trusters, and are intended to be a similar estimate of the standard error.
%  They may or may not be calculated in the same way, depending on how the
%  estimates are formed (e.g. using MCMC).
%
function [m e t] = episode(p)

%******************************************************************************
%   Determine number of trusters, trustees and so on
%******************************************************************************
noTrustees   = numel(p.trustees);
noTrusters   = numel(p.trusters);
noRepSources = numel(p.repSources);

%******************************************************************************
%   Generate direct observations for each truster/trustee pair
%******************************************************************************
for trustee = 1:noTrustees
   for truster = 1:noTrusters

      %************************************************************************
      %   Generate observations
      %************************************************************************
      obs = sample(p.trustees{trustee},p.noDirectObs(truster,trustee));

      %************************************************************************
      %   Inform Truster
      %************************************************************************
      p.trusters{truster} = directObserve(p.trusters{truster},trustee,obs);

   end % truster loop
end % trustee loop

%******************************************************************************
%   Generate trustee behaviour observations for each observer/trustee pair
%******************************************************************************
for trustee = 1:noTrustees
   for observer = 1:noRepSources

      %************************************************************************
      %   Generate Observations
      %************************************************************************
      dirObs = sample(p.trustees{trustee},p.noRepObs(observer,trustee));

      %************************************************************************
      %   Transform observations through observers opinion function
      %************************************************************************
      if isa(p.repSources{observer},'function_handle')
         reportedObs = feval(p.repSources{observer},dirObs);
      else
         reportedObs = fheval(p.repSources{observer},dirObs);
      end

      %************************************************************************
      %   Inform truster of observations
      %************************************************************************
      for truster = 1:noTrusters

         p.trusters{truster} = ...
            repReceive(p.trusters{truster},trustee,observer,reportedObs);

      end % truster loop

   end % observer loop
end % trustee loop

%******************************************************************************
%   Initialise output variables
%******************************************************************************
m = zeros(1,noTrusters+1); % plus 1 for estimate with perfect information
e = zeros(1,noTrusters+1); 
t = zeros(1,noTrusters+1); 

%******************************************************************************
%   Approximate true expected utility for each trustee. This is achieved
%   by monte carlo sampling given complete information. In the discrete case,
%   we can just do this analytically.
%******************************************************************************
tic;
if isa(p.trustees{1},'multinomial')
   dist = p.trustees{1}.p;
   domain = p.trustees{1}.v;
   m(1) = sum(fheval(p.utilFh,domain) .* dist);
   e(1) = 0;
else
   trueObs = fheval(p.utilFh,sample(p.trustees{1},p.noPIEUObs));
   %n(1) = p.noPIEUObs;
   m(1) = mean(trueObs);
   e(1) = std(trueObs) / sqrt(p.noPIEUObs);
end
t(1) = toc;

%******************************************************************************
%   Retrieve expected utility estimate from trusters
%******************************************************************************
for truster = 1:noTrusters

   tic;
   [curM curE] = euEstimate(p.trusters{truster},1,p.utilFh);
   t(truster+1) = toc;
   m(truster+1) = curM;
   e(truster+1) = curE;

end





