% Script for illustrating why we should learn parameter correlations instead
% of opinion functions directly
%
function functionVParameterLearning

samplesPerDist = 10;
noDistribution = 50;

repFunc = @(x) x+sin(x*3);

%******************************************************************************
%   Generate behaviour with associated opinions (tight variance)
%******************************************************************************
% stddev = 0.01;
% [nx nr] = generateData(stddev,samplesPerDist,noDistribution);
% 
% % plot data
% subplot(3,2,1);
% plot(nx,repFunc(nx),'r^',nr,repFunc(nx),'b.');
% title(['Data Regression, behaviour std dev = ' num2str(stddev)]);
% legend('known behaviour','latent behaviour','Location','NorthWest');
% xlabel('behaviour'); ylabel('opinion');
% subplot(3,2,2);
% plotMean(nr,repFunc(nx),samplesPerDist,noDistribution,stddev);

%******************************************************************************
%   Plot the means
%******************************************************************************

%******************************************************************************
%   Generate behaviour with associated opinions (medium variance)
%******************************************************************************
stddev = 0.1;
[nx nr] = generateData(stddev,samplesPerDist,noDistribution);

% plot data
subplot(2,2,1);
plot(nx,repFunc(nx),'r^',nr,repFunc(nx),'b.');
title(['Data Regression, behaviour std dev = ' num2str(stddev)]);
legend('known behaviour','latent behaviour','Location','NorthWest');
xlabel('behaviour'); ylabel('opinion');
subplot(2,2,2);
plotMean(nr,repFunc(nx),samplesPerDist,noDistribution,stddev);

%******************************************************************************
%   Generate behaviour with associated opinions (wide variance)
%******************************************************************************
stddev = 1;
[nx nr] = generateData(stddev,samplesPerDist,noDistribution);

% plot data
subplot(2,2,3);
plot(nx,repFunc(nx),'r^',nr,repFunc(nx),'b.');
title(['Data Regression, behaviour std dev = ' num2str(stddev)]);
legend('known behaviour','latent behaviour','Location','NorthWest');
xlabel('behaviour'); ylabel('opinion');
subplot(2,2,4);
plotMean(nr,repFunc(nx),samplesPerDist,noDistribution,stddev);

%******************************************************************************
%******************************************************************************
%   Function for generating the data
%******************************************************************************
%******************************************************************************
function [nx nr] = generateData(stddev,samplesPerDist,noDistribution)

sampledMu = sort(12*rand(1,noDistribution)-6);
mu = repmat(sampledMu,1,samplesPerDist);
nx = stddev*randn(size(mu))+mu;
nr = stddev*randn(size(mu))+mu;

%******************************************************************************
%******************************************************************************
%   Function for plotting means
%******************************************************************************
%******************************************************************************
function plotMean(nx,ny,samplesPerDist,noDistribution,stddev)

nx = reshape(nx,[noDistribution,samplesPerDist])';
ny = reshape(ny,[noDistribution,samplesPerDist])';

mx = mean(nx); sx = std(nx); ex = tinv(0.975,size(nx,1)).*sx./sqrt(size(nx,1));
my = mean(ny); sy = std(ny); ey = tinv(0.975,size(ny,1)).*sy./sqrt(size(ny,1));

errorbar(mx,my,ey);
title(['Parameter Regression, behaviour std dev = ' num2str(stddev)]);
xlabel('behaviour mean');
ylabel('opinion mean');












