clear all

n = 50;
rounds = 5000;

propMin = 0;
propMax = 1;
capMin = 0;
capMax = 1;
prefMin = 0.5;
prefMax = 1.5;
sigma = 0.1;

fixedPreference = 1;
baseUtility = 1;

results = zeros(1,rounds);
for round = 1:rounds
    capMean = capMin+(capMax-capMin)*rand(1,1);
    propMean = propMin+(propMax-propMin)*rand(1,1);

    simCapabilities = sigma*randn(1,n)+capMean;
    properties = sigma*randn(1,n)+propMean;

    preferences = sigma*randn(1,n)+fixedPreference;
    
    capabilities = (properties+simCapabilities)/2;

%      utilities = baseUtility - (preferences-capabilities);
%     utilities = capabilities .* (preferences < capabilities);
%     utilities = sqrt((capabilities-preferences).^2);
     utilities = abs(capabilities-preferences);
%     utilities = capabilities/preferences;
    
    results(round) = mean(utilities);
end
meanFixed = mean(results);
subplot(2,1,1),histogram(results)

results = zeros(1,rounds);
for round = 1:rounds
    capMean = capMin+(capMax-capMin)*rand(1,1);
    propMean = propMin+(propMax-propMin)*rand(1,1);
    prefMean = prefMin+(prefMax-prefMin)*rand(1,1);
%     prefMean = randn(1,1)*0.1;

    simCapabilities = sigma*randn(1,n)+capMean;
    properties = sigma*randn(1,n)+propMean;

    preferences = sigma*randn(1,n)+prefMean;
        
    capabilities = (properties+simCapabilities)/2;

%      utilities = baseUtility - (preferences-capabilities);
%     utilities = capabilities .* (preferences < capabilities);
%     utilities = baseUtility .* -log(abs(capabilities-preferences));
%     utilities = sqrt((capabilities-preferences).^2);

    utilities = abs(capabilities-preferences);
    
    results(round) = mean(utilities);
end

meanSubjective = mean(results);
subplot(2,1,2),histogram(results)
[meanFixed,meanSubjective]




%%


a = rand(1,10000);
b = randn(1,10000)+a;
histogram(abs(b))

