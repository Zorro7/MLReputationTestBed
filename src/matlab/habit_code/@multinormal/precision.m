% Returns the distribution precision
function c = covariance(t)

%*************************************************************************
%   Return covariance matrix
%*************************************************************************
n = dims(t);

c = zeros(n,n,size(t.params,1));

for i = 1:size(t.params,1)

   L = vechinv( t.params(i,(n+1):end) );
   c(:,:,i) = L*L';

end

squeeze(c);


