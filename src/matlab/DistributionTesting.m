clear all

n = 100;
rounds = 5000;

propMin = 0;
propMax = 1;
capMin = 0;
capMax = 1;
prefMin = 0;
prefMax = 1;
sigma = 0.1;

fixedPreference = 1;
baseUtility = 1;

results = zeros(1,rounds);
for round = 1:rounds
    capMean = capMin+(capMax-capMin)*rand(1,1);
    propMean = propMin+(propMax-propMin)*rand(1,1);

    simCapabilities = sigma*randn(1,n)+capMean;
    properties = sigma*randn(1,n)+propMean;

    preferences = ones(1,n)*fixedPreference;
    
    capabilities = (properties+simCapabilities)/2;

    utilities = baseUtility - abs(capabilities-preferences);
%     utilities = abs(capabilities-preferences) < 0.5;
    
    results(round) = mean(utilities);
end
meanFixed = mean(results);

results = zeros(1,rounds);
for round = 1:rounds
    capMean = capMin+(capMax-capMin)*rand(1,1);
    propMean = propMin+(propMax-propMin)*rand(1,1);
    prefMean = prefMin+(prefMax-prefMin)*rand(1,1);

    simCapabilities = sigma*randn(1,n)+capMean;
    properties = sigma*randn(1,n)+propMean;

    preferences = sigma*randn(1,n)+prefMean;
        
    capabilities = (properties+simCapabilities)/2;

    utilities = baseUtility - abs(capabilities-preferences);
%     utilities = abs(preferences-capabilities) < 0.25;
    
    results(round) = mean(utilities);
end

meanSubjective = mean(results);

[meanFixed,meanSubjective]




