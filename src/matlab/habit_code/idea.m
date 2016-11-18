

% how to generate utility functions
x=0:0.01:1;
subplot(1,2,1);
a=0.5*randn(1,5); plot(mvnrnd(a(1)*x+a(2),covSEiso(log(0.1*rand(1,2)),x'),1)');


% how to generate behaviour distributions
d = dirichlet;
n = nig(1);
mix = normalmix(sample(d,1),sample(n,5));

p = pdf(mix);
subplot(1,2,2); plot(p./max(p));


% reputation sources could use a combination of both
% 1) random function to map behaviour to output mean
% 2) noise from gaussian mixture model
%
% in each case distribution parameters specifies
% the distribution of 'types' of reputation sources
