% AGGRESULTS: Function for aggregating result structures
%
% Usage results = aggresults(results1,results2,...)
%
function results = aggresults(varargin)

%******************************************************************************
%   Initialise results structure to first parameter
%******************************************************************************
results = varargin{1};

%******************************************************************************
%   Aggregate stats
%******************************************************************************
for i=2:nargin

   %***************************************************************************
   %   Define short aliases for the statistics we need to combine.
   %   Here, s1 and s2 are the variances, which are easier to combine that
   %   standard deviations.
   %***************************************************************************
   m1 = results.m;     s1 = results.s.^2;     n1 = results.n;
   m2 = varargin{i}.m; s2 = varargin{i}.s.^2; n2 = varargin{i}.n;
   
   %***************************************************************************
   %   Set all missing values to zero so that they don't contribute to the
   %   calculation.
   %***************************************************************************
   m1(isnan(m1)) = 0; s1(isnan(s1)) = 0; n1(isnan(n1)) = 0;
   m2(isnan(m2)) = 0; s2(isnan(s2)) = 0; n2(isnan(n2)) = 0;
   
   %***************************************************************************
   %   Calculate new sample size (the sum of the old sample sizes)
   %***************************************************************************
   nn = n1+n2;

   %***************************************************************************
   %   Find the indices of all non-empty samples. We use these to ignore
   %   empty samples where necessary to avoid inf and nan.
   %***************************************************************************
   nz1 = n1>0; nz2 = n2>0; nzn = nn>0;

   %***************************************************************************
   %   Calculate the weights for averaging
   %***************************************************************************
   w1 = zeros([size(nzn) 1]);
   w2 = zeros([size(nzn) 1]);
   w1(nzn) = n1(nzn)./nn(nzn);
   w2(nzn) = n2(nzn)./nn(nzn);

   %***************************************************************************
   %   Calculate the overall mean
   %***************************************************************************
   nm = w1.*m1 + w2.*m2;
   
   %***************************************************************************
   %   Form sum of square statistics (normalised by n) as a means to 
   %   calculating the joint variance.
   %***************************************************************************
   v1 = zeros([size(nn) 1]); v2 = zeros([size(nn) 1]);
   v1(nz1) = s1(nz1).*(1-1./n1(nz1)) + (m1(nz1) - nm(nz1)).^2;
   v2(nz2) = s2(nz2).*(1-1./n2(nz2)) + (m2(nz2) - nm(nz2)).^2;

   %***************************************************************************
   %   Use the sum of squared statistics to calculate the overall variance.
   %   Here, we adjust the weights so that we get the unbiased estimate.
   %***************************************************************************
   GTOne  = nn>1; % all indices where the combined sample size > 1

   w1 = zeros([size(w1) 1]);
   w2 = zeros([size(w2) 1]);
   w1(GTOne) = n1(GTOne)./( nn(GTOne) - 1);
   w2(GTOne) = n2(GTOne)./( nn(GTOne) - 1);

   ns = w1.*v1 + w2.*v2;
   
   %***************************************************************************
   %   Store the aggregated statistics
   %***************************************************************************
   results.n = nn;
   results.m = nm;
   results.s = sqrt(ns);
   
   %***************************************************************************
   %   If any combined samples are zero, the other statistics should be
   %   listed as missing (NaN) rather than zero.
   %***************************************************************************
   results.m(0>=results.n) = nan;
   results.s(0>=results.n) = nan;
   
   
end % main loop



