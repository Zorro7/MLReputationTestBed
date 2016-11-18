% Script for generating graphs to illustrate joint parameter distributions.
%

%******************************************************************************
%   Set up initial prior
%******************************************************************************
initialPrior = nig;

%******************************************************************************
%   Actual population distribution
%******************************************************************************
seedDist = multinormal(sample(initialPrior));
seedSamples = sample(seedDist,6);
populationDist = observe(initialPrior,seedSamples);

%******************************************************************************
%   Sample trustees from population
%******************************************************************************
noTrustees = 10;
trustees = multinormal(sample(populationDist,noTrustees));

%******************************************************************************
%   Create belief models for each trustee
%******************************************************************************
dirModels = cell(1,noTrustees);
dirObs = sample(trustees,30);

for i=1:numel(dirModels)
   
   dirModels{i} = observe(initialPrior,dirObs(i,:));
   
end

%******************************************************************************
%   Sample data
%******************************************************************************
allModels = cell(1,numel(dirModels)+1);
allModels{1} = initialPrior;
allModels(2:end) = dirModels;
samples = zeros(2,500);
for i=1:size(samples,2)
   
   c = ceil(rand*numel(allModels));
   samples(:,i) = sample(allModels{c});
   
end

clf;
subplot(2,1,1);
scatter(samples(1,:),samples(2,:));


%******************************************************************************
%   Set up x and y axis
%******************************************************************************
minVar = min(samples(2,:)); maxVar = max(samples(2,:));
 minMu = mean(samples(1,:))-std(samples(1,:));
 maxMu = mean(samples(1,:))+std(samples(1,:));
var = linspace(minVar,maxVar,100);
mu  = linspace(minMu,maxMu,100);

[M V] = meshgrid(mu,var);

values = [M(:) V(:)];

%******************************************************************************
%   Calculate DP mixture
%******************************************************************************
P = normpdf(M,initialPrior.m,sqrt(V/initialPrior.v)) * ...
     gampdf(V,initialPrior.a,initialPrior.b);

for i=1:numel(dirModels)
   
   disp(['calculating pdf for model : ' num2str(i)]);
   P = P + normpdf(M,dirModels{i}.m,sqrt(V/dirModels{i}.v)) * ...
            gampdf(V,dirModels{i}.a,dirModels{i}.b);
   
end

subplot(2,1,2);
mesh(M,V,P);
xlabel('mean'); ylabel('variance'); zlabel('density');



