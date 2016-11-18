%******************************************************************************
%******************************************************************************
%   Utility function for adding fields from different structures
%******************************************************************************
%******************************************************************************
function s = aggregateStructs(varargin)

s = struct;

for i=1:nargin
   paramNames = fieldnames(varargin{i});
   for f=1:numel(paramNames)
      s.(paramNames{f}) = varargin{i}.(paramNames{f});
   end
end

