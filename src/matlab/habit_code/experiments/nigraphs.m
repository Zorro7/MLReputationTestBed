

noLearningTrustees = 100;
learningTrustees = multinomial(sample(populationDist,noLearningTrustees));


learningModels = cell(1,noLearningTrustees);
learningObs = sample(learningTrustees,500);

for i=1:numel(learningModels)
   
   learningModels{i} = observe(initialPrior,learningObs(:,i));
   
end


%******************************************************************************
%   Calculate DP mixture
%******************************************************************************
nigDP = zeros(size(M));

for i=1:numel(allModels)
  
   nigDP(:) = nigDP(:) + pdf(learningModels{i},values)';
   
end

%******************************************************************************
%   Simulate NIG parameter model
%******************************************************************************


normalMixture = cell(1,20);

for s=1:numel(normalMixture)
   
   samples = zeros(numel(learningModels),3);
   
   for d=1:numel(dirModels)
      
      samples(d,:) = sample(learningModels{d});
      
   end
   
   hyperparamDist = observe(nig(3),samples);
   paramSample = sample(hyperparamDist);
   normalMixture{s} = multinormal(paramSample);
   
end
   
%******************************************************************************
%   Evaluate prior distribution
%******************************************************************************
nigP = zeros(size(M));

for i=1:numel(normalMixture)
   
   nigP(:) = nigP(:) + pdf(normalMixture{i},values);
   
end
% 
% figure;

actualPmax = actualP;
actualPmax(:) = actualPmax(:)./max(actualP(:));

nigPmax = nigP;
nigPmax(:) = nigPmax(:)./max(nigPmax(:));

Pmax = nigDP;
Pmax(:) = Pmax(:)./max(P(:));


subplot(2,2,1);
hold on;
contourf(M,V,actualPmax);
contour(M,V,nigPmax);
hold off;

subplot(2,2,2);
hold on;
contourf(M,V,actualPmax);
contour(M,V,Pmax);
hold off;

% figure;
% subplot(2,3,1);
% surf(M,V,actualPmax,'EdgeColor','none');
% subplot(2,3,2);
% surf(M,V,nigPmax,'EdgeColor','none');
% subplot(2,3,3);
% surf(M,V,Pmax,'EdgeColor','none');

% same again, less certainty

noLearningTrustees = 100;
learningTrustees = multinomial(sample(populationDist,noLearningTrustees));


learningModels = cell(1,noLearningTrustees);
learningObs = sample(learningTrustees,30);

for i=1:numel(learningModels)
   
   learningModels{i} = observe(initialPrior,learningObs(:,i));
   
end


%******************************************************************************
%   Calculate DP mixture
%******************************************************************************
nigDP = zeros(size(M));

for i=1:numel(allModels)
  
   nigDP(:) = nigDP(:) + pdf(learningModels{i},values)';
   
end

%******************************************************************************
%   Simulate NIG parameter model
%******************************************************************************


normalMixture = cell(1,20);

for s=1:numel(normalMixture)
   
   samples = zeros(numel(learningModels),3);
   
   for d=1:numel(dirModels)
      
      samples(d,:) = sample(learningModels{d});
      
   end
   
   hyperparamDist = observe(nig(3),samples);
   paramSample = sample(hyperparamDist);
   normalMixture{s} = multinormal(paramSample);
   
end
   
%******************************************************************************
%   Evaluate prior distribution
%******************************************************************************
nigP = zeros(size(M));

for i=1:numel(normalMixture)
   
   nigP(:) = nigP(:) + pdf(normalMixture{i},values);
   
end

% figure;

actualPmax = actualP;
actualPmax(:) = actualPmax(:)./max(actualP(:));

nigPmax = nigP;
nigPmax(:) = nigPmax(:)./max(nigPmax(:));

Pmax = nigDP;
Pmax(:) = Pmax(:)./max(P(:));


subplot(2,2,3);
hold on;
contourf(M,V,actualPmax);
contour(M,V,nigPmax);
hold off;

subplot(2,2,4);
hold on;
contourf(M,V,actualPmax);
contour(M,V,Pmax);
hold off;
% 
% figure;
% subplot(2,3,4);
% surf(M,V,actualPmax,'EdgeColor','none');
% subplot(2,3,5);
% surf(M,V,nigPmax,'EdgeColor','none');
% subplot(2,3,6);
% surf(M,V,Pmax,'EdgeColor','none');





