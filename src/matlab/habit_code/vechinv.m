% Returns inverse half-vectorisation of matrix X
function Y=vecinv(X)

% application of the quadratic equation
len = (sqrt(1+8*numel(X))-1)/2;

if ~isequal(floor(len),len)
   error('Incorrect vector length to create square matrix');
end

Y = zeros(len);

Y(0<tril(ones(len))) = X;

