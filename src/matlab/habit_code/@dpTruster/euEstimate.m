% DPTRUSTER/euEstimate
%
% Usage: [expval stderr] = euEstimate(t,trustee,utilFh);
%
function [expval stderr weights] = euEstimate(t,trustee,utilFh,guiEnable)

if nargin<4
   guiEnable = false;
end

%******************************************************************************
%   Generate mixture components by observing the direct observations for
%   each trustee.
%******************************************************************************
mixComponents = cell(1,t.noTrustees);

for i=1:t.noTrustees
   mixComponents{i} = observe(t.priorDist,t.directObs{i});
end

%******************************************************************************
%   Plot group prior and direct observations if gui is enabled
%******************************************************************************
if guiEnable
   
   % group prior
   plotComponents = mixComponents;
   plotComponents{trustee} = t.priorDist;
   subplot(2,2,1);
   plotBeliefs(zeros(1,numel(plotComponents)),plotComponents);
   title('Group-Based Prior');
   
   % direct only
   plotComponents = {observe(t.priorDist,t.directObs{trustee})};
   subplot(2,2,2);
   plotBeliefs(0,plotComponents);
   title('Direct Observations Only');
   
end
   
%******************************************************************************
%   Initialise the component weights to account for the marginal
%   likelihood of the direct observations according to each component.
%******************************************************************************
weights = zeros(1,t.noTrustees);
weights(trustee) = marginalLogLikelihood(t.priorDist,t.directObs{trustee});
weights(trustee) = weights(trustee) + log(t.alpha); % multiply by prior weight

for i=[1:(trustee-1), (trustee+1):t.noTrustees]

   weights(i) = marginalLogLikelihood(mixComponents{i},t.directObs{trustee});

end

%******************************************************************************
%   Observe direct observations of this trustee for components
%   containing information about other trustees.
%******************************************************************************
for i=[1:(trustee-1), (trustee+1):t.noTrustees]
   mixComponents{i} = observe(mixComponents{i},t.directObs{trustee});
end

%******************************************************************************
%   Plot direct observation posterior
%******************************************************************************
if guiEnable
   
   subplot(2,2,3);
   plotBeliefs(weights,mixComponents);
   title('Direct Information Posterior');
   
end

%******************************************************************************
%   Calculate the expected utility and standard error according to each
%   individual component.
%******************************************************************************
stderr  = zeros(1,t.noTrustees);
expval  = zeros(1,t.noTrustees);

for i=1:t.noTrustees
   [expval(i) stderr(i)] = expfval(mixComponents{i},utilFh);
end

%stderr = stderr(trustee); expval=expval(trustee);

%******************************************************************************
%   Now calculate the log weights based on each reported opinion
%   distribution, and sum them for each component
%******************************************************************************
curTrusteeRep = t.repModels(trustee,:);

componentRep = t.repModels;
[componentRep{trustee,:}] = deal(t.priorDist); % prior distribution weight

for i=1:t.noTrustees
   for j=1:t.noSources
      weights(i) = weights(i) + ...
         feval(t.logweight,curTrusteeRep{j},componentRep{i,j});
   end
end


%******************************************************************************
%   Plot all observations posterior
%******************************************************************************
if guiEnable
   
   subplot(2,2,4);
   plotBeliefs(weights,mixComponents);
   title('Final Posterior');
end

%******************************************************************************
%   normalise the weights
%******************************************************************************
weights = normalisedWeights(weights);

%******************************************************************************
%   use the weights to calculate the expected utility over all components.
%******************************************************************************
stderr = sum(stderr.*weights);
expval = sum(expval.*weights);

%******************************************************************************
%******************************************************************************
%   Function that calculates actual weights from unormalised log weights
%******************************************************************************
%******************************************************************************
function weights = normalisedWeights(unNormalisedLogWeights)

%******************************************************************************
%   zero max the log weights to try to maintain numerical stability.
%   This ensures that all weights are finite. Worst case scenario - the
%   maximum weight is so far above everything else that all other weights go
%   to zero.
%******************************************************************************
weights = unNormalisedLogWeights - max(unNormalisedLogWeights);

%******************************************************************************
%   normalise the weights
%******************************************************************************
weights = weights - log(sum(exp(weights)));

weights = exp(weights) ./ sum(exp(weights)); % too be sure


%******************************************************************************
%******************************************************************************
%   Function to a belief model 
%******************************************************************************
%******************************************************************************
function plotBeliefs(logWeights,components)

if ~isequal(dims(components{1}),3)
   return;
end

weights = normalisedWeights(logWeights);

if isequal(dims(components{1}),3)

   x = linspace(0,1,100);
   y = linspace(0,1,100);
   [X Y] = meshgrid(x,y);
   Z = 1-X-Y;
   values = [X(:) Y(:) Z(:)];
   
   P = zeros(size(X));
   
   for i=1:numel(components)
      
      P(:) = P(:) + weights(i)*pdf(components{i},values)';
      
   end
   
   contourf(X,Y,P);

end








