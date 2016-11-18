% Draws Samples from dirichlet-nig distribution
function [samples t] = sample(t,varargin)

%*************************************************************************
%   Error checking originally performed in wishrnd
%*************************************************************************
if nargin<2
   error('dirichlet:sample:TooFewInputs','Two arguments are required.');
end

noSamples = varargin{1};

% allocate space for samples
samples = zeros(noSamples,dims(t));
for i=1:noSamples

   %**********************************************************************
   %   First generate dirchlet parameter sample
   %**********************************************************************
   samples(i,1:dims(t.d)) = sample(t.d,1);

   %**********************************************************************
   %   Based on this, choose the nig mixture component
   %**********************************************************************
   c = find(mnrnd(1,samples(i,1:dims(t.d))));

   %**********************************************************************
   %   Now, generate the parameters for the normal part from the 
   %   selected nig model
   %**********************************************************************
   samples(i,(dims(t.d)+1):end) = sample(t.n{c},1);

end 


