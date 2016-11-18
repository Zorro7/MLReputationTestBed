% Returns the posterior model given the mean and covariance of the
% data.
%
% Usage: model = observe(model,data)
%
%
function model = observe(model,data)

%*************************************************************************
%   If there is no data, return model unchanged.
%*************************************************************************
if isequal(0,numel(data))
   return;
end

%*************************************************************************
%    Check that data is valid (i.e. integers in correct range)
%*************************************************************************
if any(data<0) || ~isequal(data,floor(data))
   error(['Data must be  a positive integer vector of size: ', ...
           num2str(numel(a))]);
end

%*************************************************************************
%   Get the index of all the data in the domain
%*************************************************************************
[isValid dataIndices] = ismember(data,model.d);

if ~all(isValid)
   error('some observed values are not in the recognised domain');
end

%*************************************************************************
%  Update parameters just be adding frequencies of observations
%*************************************************************************
for i=1:numel(model.a)
   
   noOccurences = sum(dataIndices==i);
   model.a(i) = model.a(i) + noOccurences;
   
end
   



