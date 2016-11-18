% Script for testing how number of trustees impacts on group behaviour
% performance.
%
% Usage: [m e s] = dpGroupBehaviourDirChg150408
%
function [m e s] = dpGroupBehaviourDirChg150408


%******************************************************************************
%   Set up fixed parameters
%******************************************************************************
s.noRuns = 90;
s.noConditions = 10;
s.noTrusters = 1;
s.noPIEUObs = 100;
s.utilFh = funch;

%******************************************************************************
%   Set up number of sources
%******************************************************************************
s.noSources = zeros(1,s.noConditions);

%******************************************************************************
%   Set up number of trustees
%******************************************************************************
s.noTrustees = ones(1,s.noConditions)*50;

%******************************************************************************
%   Set up number of direct observations
%******************************************************************************
s.noDirectObs = cell(1,s.noConditions);
for i=1:s.noConditions
   s.noDirectObs{i} = [1 1+5*(i-1)*ones(1,s.noTrustees(i)-1)];
end

%******************************************************************************
%   Set up number of reputation observations
%******************************************************************************
s.noRepObs = cell(1,s.noConditions);
for i=1:s.noConditions
   s.noRepObs{i} = zeros(s.noSources(i),s.noTrustees(i));
end

%******************************************************************************
%   Set up reputation Sources
%******************************************************************************
s.sourceGenerator = @(a,p,condition,run) cell(1,0);

%******************************************************************************
%   Set up trustees
%******************************************************************************
initialPredictive = multinomial([60 1 1 1 1 0]/64);
          seedObs = sample(initialPredictive,s.noRuns,800);
 
s.trusteePopulations = cell(1,s.noRuns); % store population for later reference

for run=1:s.noRuns
   s.trusteePopulations{run} = observe(dirichlet(5),seedObs(run,:));
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
[m e] = mainloop(s);

%******************************************************************************
%   Process results
%******************************************************************************

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

trusters = {dpTruster(p)};





