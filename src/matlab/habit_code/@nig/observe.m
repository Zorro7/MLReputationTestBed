% Returns the posterior model given the mean and covariance of the
% data.
%
% Usage: model = observe(model,data)
%
%
function model = observe(model,data)

%*************************************************************************
%    Calculate mean and sum of squares and number of samples
%*************************************************************************
mu = nanmean(data)';
n  = size(data,1);
ss = nancov(data,1)*n;

if isequal(0,n)
   return;
end

%*************************************************************************
%    Calculate updated parameter values
%*************************************************************************
a = model.a + n;
v = model.v + n;
m = model.v*model.m; m(:)=m(:)+n*mu(:); m=m/v;
b = ss + n*mu*mu' + model.v*model.m*model.m' - v*m*m' + model.b;

model.a = a;
model.v = v;
model.m = m;
model.b = b;


