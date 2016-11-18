% STANDALONESIMULATOR: function for standalone simulatioin application
%
% Usage: standaloneSimulator inputfile outputfilePrefix
%
function standaloneSimulator(inputfile,outputfilePrefix)

%******************************************************************************
%   Check input arguments
%******************************************************************************
if ~isequal(nargin,2)
   error('Usage: standaloneSimulator inputfile outputfilePrefix');
end

if (~ischar(inputfile)) || (~ischar(outputfilePrefix))
   error('Usage: standaloneSimulator inputfile outputfilePrefix');
end

%******************************************************************************
%   Load input data
%******************************************************************************
disp(datestr(now));
disp('loading input file...');
load(inputfile);
disp('done.');

disp('finding the number of trusters...');
noAgents = numel(simData.conditionCommons{1}.trusters);
disp('done.');

%******************************************************************************
%   Run Simulation
%******************************************************************************
disp('Running Simulation...');
simulate(simData,noAgents,outputfilePrefix);
disp('done.');
disp(datestr(now));


