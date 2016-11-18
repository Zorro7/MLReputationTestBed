% Draws Samples from distribution
function s = sample(t,n)

%*************************************************************************
%  First assign samples to components according to their weight.
%  Note: we aggregate frequencies in the last to 
%*************************************************************************
if nargin<2
   n=1;
end
binEdges = [0 cumsum(t.w)];
binEdges(end) = 1.01; % >1 so that 1 falls in bin=numel(t.w);
[~, assignments] = histc(rand(1,n),binEdges);

%*************************************************************************
%  Select mean and variance for each sample point.
%*************************************************************************
cmeans = mean(t.components);
ccovar = covariance(t.components);

sampleMeans = cmeans(assignments,:);
sampleCovar = ccovar(:,:,assignments);

s = mvnrnd(sampleMeans,sampleCovar);


