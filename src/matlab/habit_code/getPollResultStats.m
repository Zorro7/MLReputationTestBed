function getPollResultStats(varargin)
% getPollResultStats utility to get states for the absolution estimation error
% for a given file in the web1 dataset.
%
% Usage: getPollResultStats(outfile, infile1, infile2...)
%
% for each file k:
%
% stats(k).allParams - randseeds and control variable setup for file
% stats(k).datafile - the kth file name
% stats(k).en - estimate error sample size
% stats(k).em - estimate error sample mean
% stats(k).es - estimate error sample standard deviation
% stats(k).ee - estimate error sample standard error for 0.95 confidence
% stats(k).t? - (as above for execution times)
%

stats = struct('allParams',[],'datafile',[],...
   'en',[],'em',[],'es',[],'ee',[], ...
   'tn',[],'tm',[],'ts',[],'te',[]);

stats = repmat(stats,[1 nargin]);

for k=1:nargin
   
   load(varargin{k});
   
   siz = size(estimates);
   siz(1:2)=1; % don't replicate 1st two dimensions
   trueValue = repmat(trueValue,siz);
   
   absErrors = abs(trueValue-estimates);
   
   % clean the data by removing outliers
   maxErrors = repmat(prctile(absErrors,95),[size(absErrors,1),1,1,1,1]);
   absErrors(absErrors>maxErrors) = nan;
   
   maxTime = repmat(prctile(estTimers,95),[size(estTimers,1),1,1,1,1]);
   estTimers(estTimers>maxTime) = nan;
   
   stats(k).allParams = allParams;
   stats(k).datafile = varargin{k};
   
   stats(k).en = squeeze(sum(~isnan(absErrors)));
   stats(k).em = squeeze(nanmean(absErrors));
   stats(k).es = squeeze(nanstd(absErrors));
   stats(k).ee = tinv(0.975,stats(k).en-1) .* stats(k).es ./ sqrt(stats(k).en);
   
   stats(k).tn = squeeze(sum(~isnan(estTimers)));
   stats(k).tm = squeeze(nanmean(estTimers));
   stats(k).ts = squeeze(nanstd(estTimers));
   stats(k).te = tinv(0.975,stats(k).tn-1) .* stats(k).ts ./ sqrt(stats(k).tn);

end

save(stats,outfile);






