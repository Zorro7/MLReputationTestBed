function p = dirichlet_pdf(x,a)

b = multibetaln(a);

a = repmat(a,size(x,1),1);

p = x .^ (a-1);

p = prod(p,2) ./ exp(b);



% the multivariate beta functiona
% assume each row is a separate parameter vector
% returns 1-N vector, one entry for each row of the input.
function b = multibetaln(a)

% put parameters in columns first for convenience
a = a';

b = sum(gammaln(a)) - gammaln(sum(a));