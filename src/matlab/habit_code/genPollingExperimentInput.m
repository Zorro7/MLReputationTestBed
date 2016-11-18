function genPollingExperimentInput(outFile)

%*****************************************************************************
%  Set up input structure template.
%*****************************************************************************
repSources = 2:4;
windowSize = [10 25 50 100];
noTrustees = [10 25 50];

numSources = numel(repSources);
numWindows = numel(windowSize);
numTrustees= numel(noTrustees);

tmpl.randSeed = nan;
tmpl.noSamples = 2;
tmpl.noTrustees = nan;
tmpl.repSource = nan; % not used, but will keep it here nonetheless.
tmpl.repId = nan;
tmpl.windowId = nan;
tmpl.windowSize = nan;
tmpl.mainVariable = 'not set';

%*****************************************************************************
%  Set up noTrustee experiments
%*****************************************************************************
ptmp = tmpl;
ptmp.noTrustees = 0:2:50;
ptmp.mainVariable = 'noTrustees';

pTrustees = repmat(ptmp,[numWindows,numSources]);

[tmpSources tmpWindows] = meshgrid(repSources,windowSize);

tmpSources = num2cell(tmpSources);
tmpWindows = num2cell(tmpWindows);

[pTrustees(:).repId] = deal(tmpSources{:});
[pTrustees(:).repSource] = deal(tmpSources{:}); % redundant variable
[pTrustees(:).windowSize] = deal(tmpWindows{:});

%*****************************************************************************
%  Set up windowSize experiments
%*****************************************************************************
ptmp = tmpl;
ptmp.windowSize = 0:2:50;
ptmp.mainVariable = 'windowSize';

pWindows = repmat(ptmp,[numTrustees,numSources]);

[tmpSources tmpTrustees] = meshgrid(repSources,noTrustees);

tmpSources = num2cell(tmpSources);
tmpTrustees = num2cell(tmpTrustees);

[pWindows(:).repId] = deal(tmpSources{:});
[pWindows(:).repSource] = deal(tmpSources{:}); % redundant variable
[pWindows(:).noTrustees] = deal(tmpTrustees{:});

%*****************************************************************************
%  Replicate input for required number of jobs
%*****************************************************************************
NO_CORES = 8;
NO_JOBS = 10;

pTrustees = repmat(pTrustees,[1 1 NO_CORES NO_JOBS]);

pWindows = repmat(pWindows,[1 1 NO_CORES NO_JOBS]);

%*****************************************************************************
%  Now set random seeds
%*****************************************************************************
for k=1:numel(pTrustees)
   pTrustees(k).randSeed = round(100 * clock * rand(6,1));
end

for k=1:numel(pWindows)
   pWindows(k).randSeed = round(100 * clock * rand(6,1));
end

%*****************************************************************************
%  Save to Master file
%*****************************************************************************
save([outFile '_master'],'pTrustees','pWindows');

%*****************************************************************************
%  Save to separate job files
%*****************************************************************************
for k=1:numel(pTrustees)
   
   [winIt repIt coreIt jobIt] = ind2sub(size(pTrustees),k);
   
   filename = sprintf('%s_trustee_W%03d_R%d_C%d_J%02d',outFile,...
      windowSize(winIt),repSources(repIt),coreIt,jobIt);
   
   p = pTrustees(k);
   
   save(filename,'p');
   
end

for k=1:numel(pWindows)
   
   [trIt repIt coreIt jobIt] = ind2sub(size(pWindows),k);
   
   filename = sprintf('%s_window_T%03d_R%d_C%d_J%02d',outFile,...
      noTrustees(trIt),repSources(repIt),coreIt,jobIt);
   
   p = pWindows(k);
   
   save(filename,'p');
   
end


