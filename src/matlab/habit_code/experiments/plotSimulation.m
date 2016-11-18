% Script for plotting results
%
% Usage: plotSimulation(m)
%
function plotSimulation(results)

trusterResults = results(:,:,2:end);
actualResults = repmat(results(:,:,1),[1 1 size(trusterResults,3)]);

results = abs(trusterResults - actualResults);

 mu = squeeze(nanmean(results));
stdev = squeeze(nanstd(results));
num = squeeze(sum(~isnan(results)));

interval = tinv(0.975,num) .* stdev;

errorbar(mu,interval);
title('Simulation results');
xlabel('condition');
ylabel('utility estimate');

if ~isvector(mu)

   legendText = cell(1,size(interval,2));

   for i=1:numel(legendText)

      legendText{i} = ['truster ' num2str(i-1)];

   end

   legend(legendText{:});

end



