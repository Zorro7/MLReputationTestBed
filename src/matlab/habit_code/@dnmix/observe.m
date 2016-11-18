% Returns the posterior model given the mean and covariance of the data.
%
% Usage: model = observe(model,data)
%
% The first column gives the discrete part of the vector
%
function model = observe(model,data)

%*************************************************************************
%   Ensure the data has correct dimensions
%*************************************************************************
if ~isequal(size(data,2),dims(model))
   error('dnmix:observe:Incorrect number of dimensions in data');

%*************************************************************************
%   Ensure the discrete part as the correct domain
%*************************************************************************
if any( data(:,1)>numel(model.n) || data(:,1)~=floor(data(:,1)) ...
        || data(:,1) < 1 )

   error("dnmix:observe:Discrete element is not in domain");

end

%*************************************************************************
%   Initialise vector to store observed frequency for each discrete value
%*************************************************************************
discreteFreq = zeros(1,numel(model.n)); 

%*************************************************************************
%   For each possible discrete value
%*************************************************************************
for i=1:numel(model.n)
   
   %**********************************************************************
   %   Select the subset of data for this nig model
   %**********************************************************************
   selectedRows = find(data(:,1)==i);
   model.n{i} = observe(model.n{i},data(selectedRows,2:end));

   %**********************************************************************
   %   Store the frequency of discrete observations with this value
   %   for updating the dirichlet part
   %**********************************************************************
   discreteFreq(i) = numel(selectedRows);

end

%*************************************************************************
%   Update the dirchlet distribution
%*************************************************************************
model.d = observe(model.d,discreteFreq);











