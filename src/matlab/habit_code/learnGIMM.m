% Wrapper function that fits gaussian mixture to infinite mixture model
% using varational methods.
% This function simply packages the result as one of our multinormal objects
% representing all of the component distributions, and also returns
% the component weights.
% 
% Usage: dist = learnGIMM(data) - where each datum is a column vector of data.
%
function dist = learnGIMM(data)

%*****************************************************************************
%  Learn infinite mixture model and approximate using finite model
%  Note: we transpose the data to be consistent with our own code.
%  That is, vdgm treats each column vector as a datum, while we tend to work
%  with row vectors.
%*****************************************************************************
addpath([pwd filesep 'vdpgm'],'-END'); %This should ensure vdpgm is accessible.
opts = mkopts_avdp;
opts.suppress_output=true; % suppress text output
result = vdpgm(data',opts);

%*****************************************************************************
%  Extract expected mixture components and their weights
%  Note: hp_prior appears to form one of the components of the posterior
%  and so I assume does not need to be included seperately.
%*****************************************************************************
weights = result.hp_posterior.xi ./ sum(result.hp_posterior.xi);
means = result.hp_posterior.m';
noDims = size(means,2);
eta = repmat(reshape(result.hp_posterior.eta,[1 1 result.K]),[noDims noDims]);
precisions = result.hp_posterior.inv_B .* eta;

%*****************************************************************************
%  Now use the parameters to construct a multinormal mixture model
%*****************************************************************************
dist = normalmix(weights,means,precisions,false);



