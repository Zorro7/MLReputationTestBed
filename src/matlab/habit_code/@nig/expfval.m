% Evaluate the expected value of a function
% 
% Usage: [expval stderr] = expfval(t,utilf)
% 
function [expval stderr utilf] = expfval(t,utilf)

%******************************************************************************
%  If function is linear, calculate analytically using predictive moments
%******************************************************************************
if isa(utilf,'funch')

   if islinear(utilf)

      [mu Sigma] = predictiveMoments(t);
      [expval utilf] = fheval(utilf,mu);
      w = utilf.w;
      stderr = Sigma*w{2}^2;
      return;
   end

end

%******************************************************************************
%   Otherwise we need to sample
%******************************************************************************
NO_SAMPLES = 50;
distSampleParams = samples(t,NO_SAMPLES);
distSamples = multinormal(distSampleParams);
varSamples = sample(distSamples);

if isa(utilf,'funch')
   [utilSamples utilf] = fheval(utilf,varSamples);
else
   utilSamples = feval(utilf,varSamples);
end

expval = mean(utilSamples);
stderr = 2 * std(utilSamples) ./ sqrt(numel(samples));


