% generate cholesky decomposition of beta parameter
function t = chol(t)

if isequal(t.binvCholesky,-1)
   [t.binvCholesky,p] = cholcov(inv(b),1);
   if p~=0
      error('nig:chol:BadCovariance',...
            't.b matrix must be symmetric and positive semi-definite.')
   end
end

