% Evaluate adaptive function handle.
%
% Usage: [y fhu] = fheval(fh,x);
%
% x   - input
% y   - output
% fh  - function handle
% fhu - function handle with updated state information
%
function [y fhu] = fheval(fh,x)

fhu = fh; % function handle remains unchanged

% true is input is row vector
rowInputFlag = isequal(ndims(x),2) && isequal(1,size(x,1));

%******************************************************************************
%   If x is a vector, we should present in consistent form depending on
%   what the function expects.
%******************************************************************************
if isvector(x)
    if fh.scalarFlag
       x = reshape(x,[1 numel(x)]);
    else
        x = reshape(x,[numel(x) 1]);
    end
end

%******************************************************************************
%   Calculate x^m for m=0:n
%******************************************************************************
y = fh.w{1};

for i=2:numel(fh.w)

   y = y + fh.w{i} * ( x.^(i-1) );

end

%******************************************************************************
%   If the input was a column vector we should return a column vector
%******************************************************************************
if isvector(y) && rowInputFlag
   y = y(:)';
elseif isvector(y) && ~rowInputFlag
   y = y(:);
end


