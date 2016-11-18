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
p.priorDist = dirichlet(5);
p.dirDims = 5;
p.repDims = 5;
p.trusters = {dpTruster(p)};

p

%% evaluate
noTrustees   = p.noTrustees;
noTrusters   = numel(p.trusters);
noRepSources = p.noSources;



% observations = cell(noRepSources + 1,noTrustees);
% 
% for te=1:noTrustees
%     observations{1,te} = ceil(rand(1,p.noDirectObs(te))*5);
%     for obs=1:noRepSources
%         observations{obs+1,te} = ceil(rand(1,p.noRepObs(obs,te))*5);
%     end
% end
% observations

%%
observations(1,1:noTrustees) = {[3,2,5,2],[5,4,1],[3,3,4],[1,3,3],[2,4,2]}
observations(2:noRepSources+1,1:noTrustees) = ...
            {dirichlet([2,1,1,2,2]), dirichlet([4,3,2,2,2]); ...
            dirichlet([3,1,2,1,2]), dirichlet([4,1,1,1,2]); ...
            dirichlet([2,1,4,1,1]), dirichlet([3,3,3,2,1]); ...
            dirichlet([1,3,1,3,1]), dirichlet([1,1,1,1,1]); ...
            dirichlet([2,3,1,2,1]), dirichlet([1,1,1,1,1])}'




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
        % observe things
        reportedObs = observations{observer+1,trustee}
        % inform truster
        for truster = 1:noTrusters 
            %%%%%%%%%% Need this function to return something like 'dirichlet(a=[a,b,c,d,e,...],d=[1,2,3,4,5,...]'
            p.trusters{truster} = repReceive(p.trusters{truster},trustee,observer,reportedObs);
        end
    end
end

%% Compute trust

m = zeros(1,noTrusters);
e = zeros(1,noTrusters); 

trustee = 1;
for truster = 1:noTrusters
   [curM curE weights] = euEstimate(p.trusters{truster},trustee,p.utilFh, 1);
   m(truster) = curM;
   e(truster) = curE;
   weights
end

[m;e]

