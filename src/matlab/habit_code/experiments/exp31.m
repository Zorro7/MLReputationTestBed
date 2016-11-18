% Effect of number of training trustees on correlated source.
%
% Usage: simData  = exp31
%
function [errData timeData conditions]  = exp31(expName)

disp(datestr(now));
disp('Generating conditions...');

%******************************************************************************
%   Set up control variables
%   Here, we use twice the number of trustees so that the correlated
%   source can report only on sources the agent knows nothing about
%******************************************************************************
noTrustees = unique(floor(exp([0:0.25:5.5])))*2;
noTrusteeObs = unique(floor(exp([2:1:5.5])));
noRepObs = unique(floor(exp([2:1:5.5])));
noTestRepObs = unique(floor(exp([2:1:5.5])));

conditions.order = {'noTrustees', 'noTrusteeObs', 'noRepObs', 'noTestRepObs'};
conditions.noTrustees = noTrustees;
conditions.noTrusteeObs = noTrusteeObs;
conditions.noRepObs = noRepObs;
conditions.noTestRepObs = noTestRepObs;

[noTrustees noTrusteeObs noRepObs noTestRepObs] = ...
   ndgrid(noTrustees, noTrusteeObs, noRepObs, noTestRepObs);

%******************************************************************************
%   Set up fixed parameters
%******************************************************************************
s.initialseeds = expseed(expName);
s.noRuns = 1;
s.noConditions = numel(noTrustees);
s.noTrusters = 4;
s.noPIEUObs = 100;
s.utilFh = funch;

%******************************************************************************
%   Set up number of sources
%******************************************************************************
s.noSources = 2*ones(1,s.noConditions);

%******************************************************************************
%   Set up number of trustees
%******************************************************************************
s.noTrustees = noTrustees(:)';

%******************************************************************************
%   Set up number of direct observations
%   The truster gets no direct knowledge about the first half of
%   trustees.
%******************************************************************************
disp('setting no. direct observations');
s.noDirectObs = cell(1,s.noConditions);
for i=1:s.noConditions
   s.noDirectObs{i} = ...
      repmat([zeros(1,ceil(s.noTrustees(i)/2)) ...
            noTrusteeObs(i)*ones(1,floor(s.noTrustees(i)/2))],s.noTrusters,1);
end

%******************************************************************************
%   Set up number of reputation observations
%   First source is the training source
%******************************************************************************
disp('setting no. reputation observations');
s.noRepObs = cell(1,s.noConditions);
for i=1:s.noConditions
   s.noRepObs{i} = ...
      [ ...
       zeros(1,ceil(s.noTrustees(i)/2)) ...  % training source
       noRepObs(i)*ones(1,floor(s.noTrustees(i)/2)) ...
      ; ...
       noRepObs(i)*ones(1,ceil(s.noTrustees(i)/2)) ...   % correlated source
       zeros(1,floor(s.noTrustees(i)/2)) ...
      ];
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
  sources{i} = disNoiseFunch(mainf,multinomial(sample(dirichlet(5))),0);

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

trusters = {gdTruster(p) dpTruster(p) bladeTruster(p) directTruster(p)};





