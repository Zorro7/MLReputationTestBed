% Reorder the specified data
%
% Usage: [conditions data1 data2...] = reorderData(order,conditions,data1,...)
%
% order is array of indices, or []. If [] then the condition variable with
% the largest number of values is chosen for the x-axis.
%
function [conditions, varargout] = reorderData(order,conditions,varargin)

%******************************************************************************
%   If no order is specified, then put the largest number of condition values
%   first.
%******************************************************************************
if isequal(0,numel(order))
   
   maxSize = 0;
   maxVariable = 1;
   for i=1:numel(conditions.order)
      curSize = numel( conditions.(conditions.order{i}) );
      if curSize > maxSize
         maxSize = curSize;
         maxVariable = i;
      end
   end
   order = [maxVariable [1:(maxVariable-1)] ...
      [(maxVariable+1):numel(conditions.order)] ];
   
end

%******************************************************************************
%   Reorder conditions
%******************************************************************************
originalConditions = conditions;
conditions.order = conditions.order(order);
conditions = orderfields(conditions,[1 1+order]);

%******************************************************************************
%   Reorder each data set in turn
%******************************************************************************
for varIt=1:numel(varargin)
   
   %***************************************************************************
   %   Reshape the data if necessary (i.e. 1 dimension per condition as
   %   opposed to one dimension for all conditions) according to original
   %   order.
   %***************************************************************************
   data = varargin{varIt};
   
   shape = ones(1,1+numel(originalConditions.order));
   shape(1) = size(data.m,1);
   for i=1:numel(conditions.order)
      shape(i+1) = numel( originalConditions.(originalConditions.order{i}) );
   end

   data.m = reshape(data.m,shape);
   data.s = reshape(data.s,shape);
   data.n = reshape(data.n,shape);
   
   %***************************************************************************
   %   Order the data
   %***************************************************************************
   data.m = permute(data.m,[1 1+order]);
   data.s = permute(data.s,[1 1+order]);
   data.n = permute(data.n,[1 1+order]);
   
   varargout{varIt} = data;
   
end



