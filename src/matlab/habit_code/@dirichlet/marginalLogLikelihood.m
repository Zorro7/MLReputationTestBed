% dirichlet/marginalLogLikelihood
%
% Usage p = marginalLogLikelihood(model,data)
%
function p = marginalLogLikelihood(model,data)

% priorPredictive = multinomial(mean(model));
%               p = sum(log(pdf(priorPredictive,data))); 


updatedModel = observe(model,data);


p = lnbeta(updatedModel.a) - lnbeta(model.a);

%******************************************************************************
%******************************************************************************
%   Sub function for calculating the log of the multivariate beta function
%******************************************************************************
%******************************************************************************
function b = lnbeta(alpha)

b = sum(gammaln(alpha)) - gammaln(sum(alpha));

