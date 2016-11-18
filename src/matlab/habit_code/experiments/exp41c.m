% Effect of number of training trustees on 100% noise source
%
% Usage: simData  = exp41c
%
function [errData timeData conditions]  = exp41c(expName)

disp(datestr(now));
disp('Generating conditions...');

%******************************************************************************
%   Set up control variables
%******************************************************************************
noTrustees = unique(floor(exp([0:0.25:5.5])));
noTrusteeObs = [7];
noRepObs = [7 148]
noTestRepObs = [7 148]
noParamSamples = [50 200 1000];

conditions.order = {'noTrustees', 'noParamSamples', 'noTrusteeObs', ...
   'noRepObs', 'noTestRepObs'};

conditions.noTrustees = noTrustees;
conditions.noParamSamples = noParamSamples;
conditions.noTrusteeObs = noTrusteeObs;
conditions.noRepObs = noRepObs;
conditions.noTestRepObs = noTestRepObs;

[noTrustees noParamSamples noTrusteeObs noRepObs noTestRepObs] = ...
   ndgrid(noTrustees, noParamSamples, noTrusteeObs, noRepObs, noTestRepObs);

%******************************************************************************
%   Set up fixed parameters
%******************************************************************************
s.initialseeds = expseed(expName);
s.noRuns = 3;
s.noConditions = numel(noTrustees);
s.noTrusters = 5;
s.noPIEUObs = 100;
s.utilFh = funch;
s.noParamSamples = noParamSamples;

%******************************************************************************
%   Set up number of sources
%******************************************************************************
s.noSources = ones(1,s.noConditions);

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

for i=1:numel(sources)

      lookup = randperm(5);
       mainf = @(x) reshape(lookup(ceil(x)),[size(x) 1]);
  sources{i} = disNoiseFunch(mainf,multinomial(sample(dirichlet(5))),1);

end

%******************************************************************************
%******************************************************************************
%   Function for generating trustees
%******************************************************************************
%******************************************************************************
function trustees = generateTrustees(a,p,condition,run)

trustees = cell(1,a.noTrustees(condition));

trusteePopulation = dirichlet(5);

for i=1:numel(trustees)
   
   params = sample(trusteePopulation);
   trustees{i} = multinomial(params);
   
end

function trusters = generateTrusters(a,p,condition,run)

p.priorDist = dirichlet(5);
p.dirDims = 5;
p.repDims = 5;

pWithPrior = p;
pWithPrior.paramModelPrior = nig(8);
pWithPrior.paramModelPrior.a = 1;
pWithPrior.paramModelPrior.b = eye(8);
pWithPrior.paramModelPrior.v = 1;

trusters = {gdTruster(p) dpTruster(p) bladeTruster(p) directTruster(p), ...
   gdTruster(pWithPrior)};





