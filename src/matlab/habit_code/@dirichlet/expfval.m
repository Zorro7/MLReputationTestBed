% Evaluate the expected value of a function
% 
% Usage: [expval stderr] = expfval(t,utilf)
% 
function [expval stderr utilf] = expfval(t,utilf)

%******************************************************************************
% evaluate function for domain
%******************************************************************************
if isa(utilf,'funch')

   [values utilf] = fheval(utilf,t.d);

else

   values = feval(utilf,t.d);

end

%******************************************************************************
%   Retrieve predictive distribution
%******************************************************************************
predDist = mean(t);

%******************************************************************************
%   Calculate expected value
%******************************************************************************
expval = sum(predDist.*values);

%******************************************************************************
%   Calculate standard error
%******************************************************************************
variance = sum( predDist .* (values.^2 - expval) );

stderr = sqrt(variance);


