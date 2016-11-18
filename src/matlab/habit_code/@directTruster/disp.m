%  TRUSTER/DISP displays interesting instance variables for this truster.
%
%   instance variables displayed :
%
%    dirModelPrior : prior distribution for directModels       ''
%
function disp(t)

%******************************************************************************
%   Fields to display
%******************************************************************************
toDisplay = {'noTrustees','dirModelPrior'};

classDisplay = cell(1,0);

for i = 1:numel(toDisplay)
   disp(toDisplay{i});
   disp(getfield(t,toDisplay{i}));
end

for i = 1:numel(classDisplay)
   c = eval(['class(t.' classDisplay{i} ');']);
   disp([classDisplay{i} ' is a ' c]);
end



