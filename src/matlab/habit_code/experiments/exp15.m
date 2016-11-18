% Script for testing how number of seed observations impacts on group behaviour
% performance.
% More general sets of group
%
% Usage: simData  = exp15
%
function [errData timeData conditions]  = exp15(expName)

disp(datestr(now));
disp('Generating conditions...');

%******************************************************************************
%   Set up control variables
%******************************************************************************
noTrustees = unique(floor(exp([2:1:5.5])));
noTrusteeObs = unique(floor(exp([2:1:5.5])));
noSeedObs = unique(floor(exp([0:0.25:5.5])));
noRepObs = 0;
noTestRepObs = 0;

conditions.order = {'noTrustees', 'noTrusteeObs', 'noSeedObs'};
conditions.noTrustees = noTrustees;
conditions.noTrusteeObs = noTrusteeObs;
conditions.noSeedObs = noSeedObs;

[noTrustees noTrusteeObs noSeedObs noRepObs noTestRepObs] = ...
   ndgrid(noTrustees, noTrusteeObs, noSeedObs, noRepObs, noTestRepObs);

%******************************************************************************
%   Set up fixed parameters
%******************************************************************************
s.initialseeds = expseed(expName);
s.noRuns = 30;
s.noConditions = numel(noTrustees);
s.noTrusters = 5;
s.noPIEUObs = 100;
s.utilFh = funch;
s.noSeedObs = noSeedObs;

%******************************************************************************
%   Set up number of sources
%******************************************************************************
s.noSources = zeros(1,s.noConditions);

%******************************************************************************
%   Set up number of trustees
%******************************************************************************
s.noTrustees = noTrustees(:)';

%******************************************************************************
%   Set up number of direct observations
%******************************************************************************
disp('setting no. direct observations');
s.noDirectObs = cell(1,s.noConditions);
for i=1:s.noConditions
   s.noDirectObs{i} = ...
      repmat([0 noTrusteeObs(i)*ones(1,s.noTrustees(i)-1)],s.noTrusters,1);
end

%******************************************************************************
%   Set up number of reputation observations
%******************************************************************************
disp('setting no. reputation observations');
s.noRepObs = cell(1,s.noConditions);
for i=1:s.noConditions
   s.noRepObs{i} = ...
      repmat([noTestRepObs(i) noRepObs(i)*ones(1,s.noTrustees(i)-1)], ...
              s.noSources,1);
end

%******************************************************************************
%   Set up reputation Sources
%******************************************************************************
s.sourceGenerator = @generateSources;

%******************************************************************************
%   Set up trustees
%******************************************************************************
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
disp('done.');
disp('running simulation...');
[errData timeData] = JITSimulator(s,expName,conditions);
disp('done.');
disp(datestr(now));

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

alpha = 1 + sample(dirichlet(5)) .* p.noSeedObs;
trusteePopulation = dirichlet(alpha);

trustees = cell(1,a.noTrustees(condition));

for i=1:numel(trustees)

   params = sample(trusteePopulation);
   trustees{i} = multinomial(params);

end

function trusters = generateTrusters(a,p,condition,run)

p.priorDist = dirichlet(5);
p.dirDims = 5;
p.repDims = 5;

pWithPrior = p;
pWithPrior.paramModelPrior = nig(4);
pWithPrior.paramModelPrior.a = 1;
pWithPrior.paramModelPrior.b = eye(4);
pWithPrior.paramModelPrior.v = 1;

trusters = {gdTruster(p) dpTruster(p) bladeTruster(p) directTruster(p), ...
   gdTruster(pWithPrior)};





