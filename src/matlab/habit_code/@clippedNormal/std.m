% Returns the distribution standard deviation or higher dimensional
% equivalent.
function c = std(t)

%*************************************************************************
%   Return covariance matrix
%*************************************************************************

n = dims(t);

c = zeros(n,n,size(t.params,1));

for i = 1:size(t.params,1)

   c(:,:,i) = inv(vechinv( t.params(i,(n+1):end) ));

end

squeeze(c);


