% Plots simulation results
%
% Usage: plotResults(filename) plots errdata in default order
%        plotResults(filename,dataset) dataset is one of 'error','time','all'
%        plotResults(filename,dataset,order) specifies custom order for
%           condition variables. The first condition always provides the
%           horizontal axis, with different plots for all the rest.
%
% In all cases, plotResults returns a cell array of figure handles.
%
function plotResults(filename,dataset,order)

%******************************************************************************
%   load the data file
%******************************************************************************
disp('loading data...');
load([filename '.mat']);
disp('done.');

%******************************************************************************
%   Set default truster names dataset and variable order.
%   The default is to choose the first condition variable that has the
%   highest number of tested values.
%******************************************************************************
%trusters = {'gdTruster' 'dpTruster' 'bladeTruster' 'directTruster'};

if 2>nargin
   dataset = 'error';
end

if 3>nargin
   order = []; % use default order
end

%******************************************************************************
%   Reorder the variables according to the custom order
%******************************************************************************
[conditions errData timeData] = reorderData(order,conditions,errData,timeData);

%******************************************************************************
%   Plot the desired data
%******************************************************************************
if isequal(dataset,'error')
   plotDataSet(errData,conditions,trusters,'absolute error',[filename 'Err']);
elseif isequal(dataset,'time')
   plotDataSet(timeData,conditions,trusters,'runtime',[filename 'Time']);
else
   plotDataSet(errData,conditions,trusters,'absolute error',[filename 'Err']);
   plotDataSet(timeData,conditions,trusters,'runtime',[filename 'Time']);
end

%******************************************************************************
%******************************************************************************
%   Subfunction for plotting a particular dataset
%******************************************************************************
%******************************************************************************
function plotDataSet(data,conditions,trusters,xTitle,titlePrefix)

disp(['generating figures for: ' titlePrefix]);

%******************************************************************************
%   If there is more than one condition, then form the cartesian product,
%   otherwise we only have one figure
%******************************************************************************
noConditions = numel(conditions.order);
if 1<noConditions
   
   %***************************************************************************
   %   Form cell array of possible condition value indices
   %***************************************************************************
   conditionValues = cell(1,noConditions-1);
   for i=1:numel(conditionValues)
      conditionValues{i} = [1:numel(conditions.(conditions.order{i+1}))];
   end
   
   %***************************************************************************
   %   From this, form the cartesian product
   %***************************************************************************
   [conditionValues{:}] = ndgrid(conditionValues{:});
   
   %***************************************************************************
   %   Flatten value arrays and concatenate
   %***************************************************************************
   for i=1:numel(conditionValues)
      val = conditionValues{i};
      conditionValues{i} = val(:);
   end
   
   conditionValues = [conditionValues{:}];

else
   conditionValues = [1];
end

%******************************************************************************
%   Plan the number of pages (maximum of 3x4) per page
%******************************************************************************
MAX_PER_PAGE = 8;
noConditions =  size(conditionValues,1);
noPages = ceil(noConditions / MAX_PER_PAGE);

%******************************************************************************
%   Generate figure for each page
%******************************************************************************
condition = 1; % the current condition number (used as an iterator)

pageFig = figure('Visible','off'); % figure is invisible.

for page=1:noPages
   
   disp(sprintf('generating page: %d of %d',page,noPages));
   clf;
   
   %***************************************************************************
   %   Generate each axes on this page
   %***************************************************************************
   for axesIt=1:MAX_PER_PAGE
      
      disp(['axes: ' num2str(axesIt)]);
      
      %************************************************************************
      %   stop if we've ran out of conditions
      %************************************************************************
      if condition > noConditions
         disp('saving page');
   
         set(pageFig,'PaperType','A4','PaperUnits','centimeters', ...
            'PaperPosition',[0,0,20,29]);
   
         saveas(pageFig,sprintf('%sp%d.pdf',titlePrefix,page));
         close(pageFig);
         return;
      end
      
      %************************************************************************
      %   plot the currect axes
      %************************************************************************
      subplot(4,2,axesIt);
      plotSingle(data,xTitle,trusters,conditions, ...
         conditionValues(condition,:),titlePrefix)
      
      condition = condition + 1;
      
   end
   
   %***************************************************************************
   %   save current page
   %***************************************************************************
   disp('saving page');
   
   set(pageFig,'PaperType','A4','PaperUnits','centimeters', ...
      'PaperPosition',[0,0,20,29]);
   
   saveas(pageFig,sprintf('%sp%d.pdf',titlePrefix,page));
   
end

%***************************************************************************
%  close the figure when we're done
%***************************************************************************
close(pageFig);
























