% returns number of parameters for this model
function n = dims(t)

noDiag    = size(t.b,1); % number of diagonal terms
noMus     = noDiag;      % number of mean terms
noOffDiag = (noDiag^2-noDiag)/2; % number of off diagonal terms

n = noDiag+noMus+noOffDiag;

% n = 2m + 0.5*m^2 - 0.5*m
% n = 1.5m + 0.5*m^2 
%
% a=0.5 b=1.5 c=-n
%
% m = [-1.5 ± sqrt()]/[2*0.5]
% m = [-1.5 ± sqrt()]
% m = [-1.5 ± sqrt(1.5^2 - 4*0.5*c)]
% m = [-1.5 ± sqrt(2.25 + 2n)]
%
% m = sqrt(2.25 + 2n) - 1.5
% n = 2m + 0.5*m^2 - 0.5*m
%
