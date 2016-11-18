% Script for generating graphs to illustrate joint parameter distributions.
%

%******************************************************************************
%   Set up initial prior
%******************************************************************************
initialPrior = dirichlet(3);

%******************************************************************************
%   Actual population distribution
%******************************************************************************
seedDist = multinomial(sample(initialPrior));
seedSamples = sample(seedDist,10);
populationDist = observe(initialPrior,seedSamples);

%******************************************************************************
%   Sample trustees from population
%******************************************************************************
noTrustees = 20;
trustees = multinomial(sample(populationDist,noTrustees));

%******************************************************************************
%   Create belief models for each trustee
%******************************************************************************
dirModels = cell(1,noTrustees);
dirObs = sample(trustees,100);

for i=1:numel(dirModels)
   
   dirModels{i} = observe(initialPrior,dirObs(:,i));
   
end

%******************************************************************************
%   Sample data
%******************************************************************************
allModels = cell(1,numel(dirModels)+1);
allModels{1} = initialPrior;
allModels(2:end) = dirModels;
samples = zeros(3,500);
for i=1:size(samples,2)
   
   c = ceil(rand*numel(allModels));
   samples(:,i) = sample(allModels{c});
   
end

%******************************************************************************
%   Set up x and y axis
%******************************************************************************
x = linspace(0,1,60);
[X Y] = meshgrid(x,x);
Z = 1-X-Y;
valid = Z>=0;

X = X(valid); Y=Y(valid); Z=Z(valid);

values = [X(:) Y(:) Z(:)];
values(values<0) = 0;

%******************************************************************************
%   Calculate DP mixture
%******************************************************************************
P = zeros(size(X));

for i=1:numel(allModels)
  
   P(:) = P(:) + pdf(allModels{i},values)';
   
end

clf;
subplot(2,2,1);
%contourf(M,V,P,'EdgeColor','none');
ternpcolor(X,Y,P);
%set(gca,'CameraPosition',[0.5 0.5 900]);
title('Group-Based Prior');

%******************************************************************************
%   Sample new trustee
%******************************************************************************
testTrustee = multinomial(sample(populationDist));

posteriorModels = allModels;
testSamples = sample(testTrustee,10);
logWeights = zeros(1,numel(posteriorModels));

for i=1:numel(posteriorModels)
   
   posteriorModels{i} = observe(posteriorModels{i},testSamples);
   priorPredictive = multinomial(mean(allModels{i}));
   logWeights(i) = sum(log(pdf(priorPredictive,testSamples))); 
end

%******************************************************************************
%   Normalise weights
%******************************************************************************
logWeights = logWeights - mean(logWeights);
logWeights = logWeights - log(sum(exp(logWeights)));

weights = exp(logWeights);
weights = weights ./ sum(weights);

%******************************************************************************
%   Evaluate posterior
%******************************************************************************
posteriorP = zeros(size(X));

for i=1:numel(posteriorModels)
   
   posteriorP(:) = posteriorP(:) + weights(i)*pdf(posteriorModels{i},values)';
   
end

clf;
subplot(2,3,1);
%contourf(M,V,P,'EdgeColor','none');
P(:)=P(:)./max(P(:));
terncontour(X,Y,P);
%set(gca,'CameraPosition',[0.5 0.5 900]);
title('Group-Based Prior');
hold on;
actual = testTrustee.p;
ternplot(actual(1),actual(2),actual(3),'r*');

%******************************************************************************
%   Plot posterior distribution
%******************************************************************************
subplot(2,3,3);
%contourf(M,V,posteriorP,'EdgeColor','none');
posteriorP(:) = posteriorP(:)./max(posteriorP(:));
ternpcolor(X,Y,posteriorP);
%set(gca,'CameraPosition',[0.5 0.5 900]);
title('Group-based Posterior');
hold on;
actual = testTrustee.p;
ternplot(actual(1),actual(2),'k*');

%******************************************************************************
%   Plot posterior based on prior only
%******************************************************************************
basePosterior = zeros(size(X));
basePosterior(:) = pdf(posteriorModels{1},values)';
basePosterior(:) = basePosterior(:)./max(basePosterior);
subplot(2,3,2);
%contourf(M,V,basePosterior,'EdgeColor','none');
ternpcolor(X,Y,basePosterior);
%set(gca,'CameraPosition',[0.5 0.5 900]);
title('Direct Posterior');
hold on;
actual = testTrustee.p;
ternplot(actual(1),actual(2),'y*');

%******************************************************************************
%   Contour Comparison
%******************************************************************************
% subplot(2,2,4);
% hold on;
% contour(M,V,basePosterior,':');
% contour(M,V,posteriorP,'-');
% title('Posterior Contour Comparision');
% hold off;

%******************************************************************************
%   Actual Population and trustee behaviour
%******************************************************************************
subplot(2,3,4)
actualP = zeros(size(X));
actualP(:) = pdf(populationDist,values)';
actualP(:)./max(actualP(:));
%contourf(M,V,actualP);
terncontour(X,Y,actualP);
title('Population Distribution');
hold on;
actual = testTrustee.p;
ternplot(actual(1),actual(2),actual(3),'k*');


%******************************************************************************
%   Simulate NIG parameter model
%******************************************************************************


normalMixture = cell(1,30);

for s=1:numel(normalMixture)
   
   samples = zeros(numel(dirModels),3);
   
   for d=1:numel(dirModels)
      
      samples(d,:) = sample(dirModels{d});
      
   end
   
   hyperparamDist = observe(nig(3),samples);
   paramSample = sample(hyperparamDist);
   normalMixture{s} = multinormal(paramSample);
   
end
   
%******************************************************************************
%   Evaluate prior distribution
%******************************************************************************
nigP = zeros(size(X));

for i=1:numel(normalMixture)
   
   nigP(:) = nigP(:) + pdf(normalMixture{i},values);
   
end

subplot(2,3,5);

%contourf(M,V,nigP);
ternpcolor(X,Y,nigP);
title('Normal Mixture Prior');
      















   








