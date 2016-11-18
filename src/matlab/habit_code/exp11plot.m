% EXP11PLOT script to plot results from exp11 data
function exp11plot(data,titleStr)

% calculate confidence intervals
data.c = tinv(0.975,data.n).*data.s./sqrt(data.n);

% reshape to separate out control variables
newShape = [4 4 20];
data.m = reshape(data.m,newShape);
data.c = reshape(data.c,newShape);

% define control variables for plotting
noTrusteeObs = unique(floor(exp([0:0.25:5.5])));
noTrustees = unique(floor(exp([2:1:5.5])));
trusters = {'Gaussian-Dirichlet','DP-Dirichlet','Blade','Direct only'};

xValues = repmat(noTrustees',1,4);

for i=1:numel(noTrustees)
   subplot(2,2,i);
   curErrMeans = squeeze(data.m(:,i,:))';
   curErrInterval = squeeze(data.c(:,i,:))';
   errorbar(xValues,curErrMeans,curErrInterval);
   legend(trusters,'Location','Best');
   xlabel('no. trustees');
   ylabel('mean absolute error');
   title(sprintf(titleStr,noTrustees(i)));
end







