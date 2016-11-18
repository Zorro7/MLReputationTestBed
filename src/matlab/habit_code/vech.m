% Returns vectorisation of matrix X
function y=vech(x)

y = x(0<tril(ones(size(x))));

y = y(:);
