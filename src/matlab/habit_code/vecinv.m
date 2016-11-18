% Returns inverse vectorisation of matrix X
function Y=vecinv(X)

len = sqrt(numel(X))

if ~isequal(floor(len),len)
   error('Incorrect vector length to create square matrix');
end

Y = reshape(X,len,len);
