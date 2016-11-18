% p = pdf(t,x)
% Returns the probability density of x
% Assume that x is a N-D vector
% output is N-K matrix where K is the number of represented
% distributions.
%
% Note that plot(pdf(t,x)) will plot the data for each distribution
%
function p = pdf(t,x)

% number of dirichlets represented
noDist = size(t.a,1);

% number of dimensions
noDims = size(t.a,2);

% check data size
if ~isequal(size(x,2),noDims)
   error('data has unexpected number of dimensions');
end

% number of data
noData = size(x,1);

% replicate alpha parameters and data
x = repmat(reshape(x',[noDims 1 noData]),[1 noDist 1]);
a = repmat(t.a',[1 1 noData]);

b = repmat(multibetaln(t.a),[1 1 noData]);


% calculate terms in log density
pln = (a-1).*log(x);

% fix terms that won't calculate correctly
pln(a==1) = 0;

% now do the sum
pln = sum(pln) - b;

% fix any out of range calculations
xsum = sum(x);
pln(xsum~=1) = -inf;
pln(imag(pln)>0) = -inf;

p = squeeze(exp(pln))';

p(imag(pln)>0) = 0;
p(p<0) = 0;

p = real(p);

% the multivariate beta function
% assume each row is a separate parameter vector
% returns 1-N vector, one entry for each row of the input.
function b = multibetaln(a)

% put parameters in columns first for convenience
a = a';

b = sum(gammaln(a)) - gammaln(sum(a));



