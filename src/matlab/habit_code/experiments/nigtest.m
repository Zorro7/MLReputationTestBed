% Script for testing nig

function nigtest

n = [2:100];

noSamples = 100;

emean_err = zeros(noSamples,numel(n),3);
ecov_err = zeros(noSamples,3,3,numel(n));
smean_err = zeros(noSamples,numel(n),3);
scov_err = zeros(noSamples,3,3,numel(n));

for s=1:noSamples
   
   trueDist = multinormal(sample(nig(3)));
   for i=1:numel(n)
      [emean_err(s,i,:) ecov_err(s,:,:,i) smean_err(s,i,:) scov_err(s,:,:,i)] = ...
         nigepisode(n(i),trueDist);
   end
end

emean_err = squeeze(mean(emean_err));
ecov_err = squeeze(mean(ecov_err));
smean_err = squeeze(mean(smean_err));
scov_err = squeeze(mean(scov_err));

for i=1:3
   
   subplot(4,3,9+i);
   plot(n,emean_err(:,i)',n,smean_err(:,i)'); legend('estimated','sampled');
   title('mean error');

end

for i=1:3
   for j=1:3
      subplot(4,3,j+(i-1)*3);
      title(sprintf('cov(%d,%d) error',i,j));
      eErr = reshape(ecov_err(i,j,:),[1 numel(n)]);
      sErr = reshape(scov_err(i,j,:),[1 numel(n)]);
      plot(n,eErr,n,sErr); legend('estimated','sampled');
   end
end


function [emean_err ecov_err smean_err scov_err] = nigepisode(n,trueDist)

obs = sample(trueDist,n)';

sample_mean = mean(obs,1);

sample_cov = cov(obs);

sample_corr = corrcoef(obs);

posterior = observe(nig(3),obs);

tmpCov = posterior.b/(posterior.a-4);
tmpPrec = inv(tmpCov);
expPrecChol = chol(tmpPrec)';

% attempt cholesky decomposition if decomposition failed
if isequal(0,numel(expPrecChol))
   expPrecChol = chol(tmpCov)';
end

meanParams = [posterior.m' vech(expPrecChol)'];

estDist = multinormal(meanParams);

generatedSamples = sample(multinormal(sample(posterior,200)))';

sample_cov = cov(generatedSamples);
sample_mean = mean(generatedSamples);

estimated_mean = mean(estDist);
estimated_cov = covariance(estDist);



estimated_corr = estimated_cov ./ ...
   (sqrt(diag(estimated_cov))*sqrt(diag(estimated_cov))');


true_mean = mean(trueDist);
emean_err = abs(estimated_mean-true_mean);
smean_err = abs(sample_mean-true_mean);

true_cov = covariance(trueDist);
ecov_err = abs(true_cov-estimated_cov);
scov_err = abs(true_cov-sample_cov);



