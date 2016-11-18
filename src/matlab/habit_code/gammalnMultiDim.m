% GAMMALNMULTIDIM Multidimensional log gamma function
% Usage y = gammalnMultiDim(alpha,nodims) where alpha is a real vector
%
function y = gammalnMultiDim(alpha,k)

if ~isreal(alpha)
   error('alpha must be a real');
end

if ~isscalar(k) || ~isreal(k)
   error('nodims must be a real scalar');
end

% reshape alpha to row vector if necessary
oldSize = size(alpha);
alpha = reshape(alpha,[1 numel(alpha)]);

%******************************************************************************
% calculate pi Part
%******************************************************************************
piPart = ( k.*(k-1)/4 ) * log(pi);

%******************************************************************************
%  Calculate gamma part
%******************************************************************************
kArray = repmat([1:k]',1,numel(alpha));
alpha = repmat(alpha,k,1);
gammaPart = gammaln(alpha+0.5-kArray*0.5);

%******************************************************************************
% sum to get result
%******************************************************************************
y = piPart + sum(gammaPart,1);

%******************************************************************************
% restore original shape
%******************************************************************************
y = reshape(y,oldSize);


