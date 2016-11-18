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



observations = cell(noRepSources + 1,noTrustees);
observations{1,1} = [];
observations{1,2} = [3,3,5];
observations{1,3} = [3,1,5];
observations{1,4} = [1,3,4];
observations{1,5} = [4,3,4];




%% Generate direct observations for each tr/te pair
for trustee = 1:noTrustees
    for truster = 1:noTrusters
        % generate observations
%         obs = sample(p.trustees{trustee},p.noDirectObs(truster,trustee))
        obs = observations{truster,trustee}
        % inform truster
        
        %%%%%%%%%%%%% Need this function to return something like '[obs1,obs2,obs3,...]' for the truster
        p.trusters{truster} = directObserve(p.trusters{truster},trustee,obs);
    end
end




%% Generate trustee behaviour observations for each observer/trustee pair
for trustee = 1:noTrustees
    for observer = 1:noRepSources
        % generate observations
%         dirObs = sample(p.trustees{trustee},p.noRepObs(observer,trustee))
%         % transform observations for opinions
%         if isa(p.repSources{observer},'function_handle')
%             reportedObs = feval(p.repSources{observer},dirObs);
%         else
%             reportedObs = fheval(p.repSources{observer},dirObs);
%         end
        % inform truster
        reportedObs = observations{observer+1,trustee}
        for truster = 1:noTrusters 
            %%%%%%%%%% Need this function to return something like 'dirichlet(a=[a,b,c,d,e,...],d=[1,2,3,4,5,...]'
            p.trusters{truster} = repReceive(p.trusters{truster},trustee,observer,reportedObs);
            rm = p.trusters{truster}.repModels
            rm{end}
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

