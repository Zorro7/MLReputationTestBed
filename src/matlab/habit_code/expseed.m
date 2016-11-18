% function for setting the random seed
% Usage: seed = expseed(expname);
%        expseed(seed);
%        seed = expseed;
%
function seed = expseed(initial)

if ~isequal(nargin,0)

   if ~isstruct(initial)
      seed.ustate = sum(100*mean(clock' * initial)/mean(initial));
      neworder = randperm(numel(initial));
      seed.nstate = sum(100*mean(clock' * initial(neworder))/mean(initial));
   else
      seed = initial;
   end
      
   rand('twister',seed.ustate);
   randn('state',seed.nstate);

end


seed.ustate = rand('twister');
seed.nstate = randn('state');

