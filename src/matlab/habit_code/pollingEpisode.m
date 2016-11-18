% POLLINGEPISODE episode for polling experiment
% Usage [m e t] = pollingEpisode(training)
% where m : is the vector of estimates for each trustee
%       t : is the time taken to execute
%       e : is the standard error reported by the truster
%
% Training is the training data sets respectively.
% Each one is a structure containing fields simon, rama, alba, alex.
% Rama is always our dedicated truster.
%
function [m e t w] = pollingEpisode(training)

%******************************************************************************
%  Initialise Prior Distribution for the parameter (behaviour) model.
%******************************************************************************
prior = nig; % Improper Normal-Inverse-Gamma prior

%******************************************************************************
%  Initialise the Truster object with necessary parameters:
%  * The number of trustees is always the no. columns in the data matricies
%    (which we assume all have the same number of columns). We subtract 3
%    because the first three columns are occupied by the timestamp.
%  * The number of reputation sources is always 3: simon, alex & alba.
%  * The behavior model class is Gaussian, and uses the appropriate NIG prior.
%******************************************************************************
s.behModelClass = 'multinormal';             % behaviour model class
   s.noTrustees = size(training.rama,2)-3;   % no. trustees
    s.noSources = 3;                         % no. reputation sources
    s.priorDist = prior;                     % prior for behaviour model

     theTruster = dpTruster(s);  % set up truster based on these parameters

%******************************************************************************
%  For use with static behaviour model, strip out the timestamps.
%******************************************************************************
staticTraining.simon = training.simon(:,4:end);
 staticTraining.rama = training.rama(:,4:end);
 staticTraining.alba = training.alba(:,4:end);
 staticTraining.alex = training.alex(:,4:end);

%******************************************************************************
%  Train the truster using Rama's data.
%******************************************************************************
for trustee=1:s.noTrustees

   obs = staticTraining.rama(:,trustee);
   obs = obs(isfinite(obs)); % remove missing data
   theTruster = directObserve(theTruster,trustee,obs);

end

%******************************************************************************
%  Send the truster's the reputation data.
%******************************************************************************
sources = {'simon','alex','alba'};
for rep=1:numel(sources)
   for trustee=1:s.noTrustees
      
      obs = staticTraining.(sources{rep})(:,trustee);
      obs = obs(isfinite(obs)); % remove missing data
      theTruster = repReceive(theTruster,trustee,rep,obs);

   end
end

%******************************************************************************
%  Get estimates for each trustee
%  NOTE: if we do a windowing version, we probably can only do one test
%  datum at the time, with the same window of test data prior. This is
%  only fair.
%******************************************************************************
m = nan(1,s.noTrustees);
e = nan(1,s.noTrustees);
t = nan(1,s.noTrustees);
w = nan(s.noTrustees);
util = funch; % use U(x)=x for utility function
for trustee=1:s.noTrustees
   tic;
     [m(trustee) e(trustee) w(trustee,:)] = euEstimate(theTruster,trustee,util); 
   t(trustee) = toc;
end

