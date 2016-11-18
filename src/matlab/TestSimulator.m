clear all


%% fixed stuff
p.utilFh = funch;
p.noPEIUObs = 100;
p.noTrusters = 1;

p.noTrustees = 5;
p.noTrusteeObs = 3;

p.noRepObs = 4;
p.noTestRepObs = 3;
p.noSources = 2;

p.noDirectObs = repmat([0 p.noTrusteeObs * ones(1,p.noTrustees-1)],p.noTrusters,1);

p.noRepObs = repmat([p.noTestRepObs p.noRepObs*ones(1,p.noTrustees-1)], p.noSources,1);

p


%% truster generator
trp.priorDist = dirichlet(5);
trp.dirDims = 5;
trp.repDims = 5;
p.trusters = {dpTruster(trp)};

p

%% trustee generator
trustees = cell(1,p.noTrustees);
trusteePopulation = dirichlet(5);
for i=1:numel(trustees)   
   params = sample(trusteePopulation);
   trustees{i} = multinomial(params);
end
p.trustees = trustees;

p


%% generate sources
sources = cell(1,p.noSources);
p.repSources{:} = deal(funch);

p


%% evaluate
noTrustees   = numel(p.trustees);
noTrusters   = numel(p.trusters);
noRepSources = numel(p.repSources);



%% Generate direct observations for each tr/te pair
for trustee = 1:noTrustees
    for truster = 1:noTrusters
        % generate observations
        obs = sample(p.trustees{trustee},p.noDirectObs(truster,trustee));
        % inform truster
        p.trusters{truster} = directObserve(p.trusters{truster},trustee,obs);
    end
end




%% Generate trustee behaviour observations for each observer/trustee pair
for trustee = 1:noTrustees
    for observer = 1:noRepSources
        % generate observations
        dirObs = sample(p.trustees{trustee},p.noRepObs(observer,trustee));
        % transform observations for opinions
        if isa(p.repSources{observer},'function_handle')
            reportedObs = feval(p.repSources{observer},dirObs);
        else
            reportedObs = fheval(p.repSources{observer},dirObs);
        end
        % inform truster
        for truster = 1:noTrusters
            p.trusters{truster} = repReceive(p.trusters{truster},trustee,observer,reportedObs);
        end
    end
end

%% Compute trust

m = zeros(1,noTrusters);
e = zeros(1,noTrusters); 

for truster = 1:noTrusters
   [curM curE] = euEstimate(p.trusters{truster},1,p.utilFh);
   m(truster) = curM;
   e(truster) = curE;
end

[m;e]



