% NIG/PDF probability density function for normal-inverse wishart
%
% Usage: p = pdf(model,x) where x is in param vector format
%
function p = pdf(t,x)

%******************************************************************************
%   put data into variance and mean form
%******************************************************************************
gausDist = multinormal(x);

covar = covariance(gausDist);
   mu = mean(gausDist);
    d = dims(gausDist);

%******************************************************************************
%   Calculate log normalising constant for precision distribution
%******************************************************************************
precConstantln = t.a*log(det(t.b)) - 2^(d*t.a) - multGammaln(d,t.a);

%******************************************************************************
%   Allocate space for probability density
%******************************************************************************
p = zeros(1,size(x,1));

%******************************************************************************
%   Calculate density for each point
%******************************************************************************
for i=1:numel(p)

   %***************************************************************************
   %   Retrieve current mean, covariance and precision matrix
   %***************************************************************************
   curMu = mu(i,:); curCov = covar(:,:,i); curPrec = inv(curCov);

   %***************************************************************************
   %   Calculate marginal density for precision
   %***************************************************************************
   pln = log( (t.a-(d+1)/2) * det(curPrec) ) - trace(t.b*curPrec)/2;
   varpdf = exp(pln);

   %***************************************************************************
   %   Calculate conditional density for mean
   %***************************************************************************
   mupdf = mvnpdf(curMu,t.m,curCov/t.v);

   %***************************************************************************
   %   Multiply together to get joint density
   %***************************************************************************
   p(i) = varpdf*mupdf;

end

%******************************************************************************
%******************************************************************************
%   Log multivariate gamma function
%******************************************************************************
%******************************************************************************
function g = multGammaln(p,alpha)

g = (p*(p-1)/4)*log(pi) + sum( gammaln(alpha + 0.5 - [1:p]*0.5) );












