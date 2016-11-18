% Script for testing how number of reputation sources effects performance
% All sources are perfect.
%
% Usage: [m t e s] = goodReputation220408 
%
% m : mean data
% t : elapsed time data
% e : standard error data
% s : input structure
%
function [m t e s] = goodReputation220408


%******************************************************************************
%   Set up fixed parameters
%******************************************************************************
s.noRuns = 30;
s.noConditions = 11;
s.noTrusters = 4;
s.noPIEUObs = 100;
s.utilFh = funch;

%******************************************************************************
%   Set up number of sources
%******************************************************************************
s.noSources = [0:3:30]; 

%******************************************************************************
%   Set up number of trustees
%******************************************************************************
s.noTrustees = 20*ones(1,s.noConditions);

%******************************************************************************
%   Set up number of direct observations
%******************************************************************************
s.noDirectObs = cell(1,s.noConditions);
for i=1:s.noConditions
   s.noDirectObs{i} = repmat([1 20*ones(1,s.noTrustees(i)-1)],s.noTrusters,1);
end

%******************************************************************************
%   Set up number of reputation observations
%******************************************************************************
s.noRepObs = cell(1,s.noConditions);
for i=1:s.noConditions
   s.noRepObs{i} = 20*ones(s.noSources(i),s.noTrustees(i));
end

%******************************************************************************
%   Set up reputation Sources
%******************************************************************************
s.sourceGenerator = @generateSources; 

%******************************************************************************
%   Set up trustees
%******************************************************************************
initialPredictive = multinomial([60 1 1]/62);
          seedObs = sample(initialPredictive,s.noRuns,2);
 
s.trusteePopulations = cell(1,s.noRuns); % store population for later reference

for run=1:s.noRuns
   s.trusteePopulations{run} = observe(dirichlet(3),seedObs(run,:));
end

s.trusteeGenerator = @generateTrustees;

%******************************************************************************
%   Plot trustee populations
%******************************************************************************
% figure;
% 
% [X Y] = meshgrid(linspace(0,1,100),linspace(0,1,100));
% Z = 1-X-Y;
% values = [X(:) Y(:) Z(:)];
% 
% P = zeros(size(X));
% P(:) = pdf(s.trusteePopulations{1},values);
% contourf(X,Y,P);
% 
% figure;


%******************************************************************************
%   Set up trusters
%******************************************************************************
s.trusterGenerator = @generateTrusters;
   
%******************************************************************************
%   Run main loop
%******************************************************************************
[m t e] = mainloop(s);

%******************************************************************************
%   Process results
%******************************************************************************

%******************************************************************************
%******************************************************************************
%   Function for generating reputation sources
%******************************************************************************
%******************************************************************************
function sources = generateSources(a,p,condition,run)

   sources = cell(1,a.noSources(condition));
   [sources{:}] = deal(funch);

%******************************************************************************
%******************************************************************************
%   Function for generating trustees
%******************************************************************************
%******************************************************************************
function trustees = generateTrustees(a,p,condition,run)

trustees = cell(1,a.noTrustees(condition));

for i=1:numel(trustees)
   
   params = sample(a.trusteePopulations{run});
   trustees{i} = multinomial(params);
   
end

function trusters = generateTrusters(a,p,condition,run)

p.priorDist = dirichlet(5);
p.dirDims = 5;
p.repDims = 5;

trusters = {gdTruster(p) dpTruster(p) bladeTruster(p) directTruster(p)};





