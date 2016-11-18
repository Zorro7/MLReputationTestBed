% Plots Single result set to current axes
%
% Usage: plotSingle(data,datalabel,trusters,conditions,conditionVals)
%        plotSingle(data,datalabel,trusters,conditions,conditionVals)
%
%
function plotSingle(data,datalabel,trusters,conditions,conditionVals,titlestr)

%******************************************************************************
%   Default title
%******************************************************************************
if nargin < 6
   titlestr = '';
end

%******************************************************************************
%   Put condition values in cell array
%******************************************************************************
conditionCells = cell(size(conditionVals));
for i=1:numel(conditionVals)
   conditionCells{i} = conditionVals(i);
end

%******************************************************************************
%   Reshape data if necessary
%******************************************************************************
shape = ones(1,1+numel(conditions.order));
shape(1) = size(data.m,1);
for i=1:numel(conditions.order)
   shape(i+1) = numel(getfield(conditions,conditions.order{i}));
end

data.m = reshape(data.m,shape);
data.s = reshape(data.s,shape);
data.n = reshape(data.n,shape);


%******************************************************************************
%   Extract target dataset
%******************************************************************************
s = [size(data.m) 1]; s = s(3:end); 
ind = sub2ind(s,conditionCells{:});
data.m = data.m(:,:,ind)';
data.s = data.s(:,:,ind)';
data.n = data.n(:,:,ind)';

%******************************************************************************
%   Set confidence interval
%   and x-axis
%******************************************************************************
data.c = tinv(0.975,data.n) .* data.s ./ sqrt(data.n);
data.x = conditions.(conditions.order{1});
data.x = repmat(data.x',1,size(data.m,2));

%******************************************************************************
%   plot data
%******************************************************************************
linespec = {'b:x','g-s','r--^','c-.o','m:d'};
for i=1:size(data.x,2)
   errorbar(data.x(:,i)',data.m(:,i)',data.c(:,i)',linespec{i});
   hold on;
end
titlefmt = [titlestr(4:end)];
for i=2:numel(conditions.order)
   variable = conditions.order{i};
   if 8<=numel(variable)
      variable = [variable(1:min(5,numel(variable)-3)) '.' variable( (numel(variable)-2):end)];
   end
   titlefmt = [titlefmt ',' variable ...
      sprintf(' %d ', conditions.(conditions.order{i})(conditionVals(i-1)) )];
end

title(titlefmt,'FontSize',8);
ylabel(datalabel,'FontSize',8);
xlim([min(data.x(:)) max(data.x(:))]);
xlabel(conditions.order{1},'FontSize',8);
legend(trusters{:});
hold off;

textobj = findobj('type', 'text');
set(textobj, 'fontunits', 'points');
set(textobj, 'fontsize', 6);

























