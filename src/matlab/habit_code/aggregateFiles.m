%******************************************************************************
%******************************************************************************
%   Standalone Utility for aggregating files
%******************************************************************************
%******************************************************************************
function aggregateFiles(outputfile,varargin)

disp(sprintf('loading file %s...',varargin{1}));
load(varargin{1});

 errDataOut = errData;
timeDataOut = timeData;

clear timeData errData;

oldc = conditions;

for i=2:numel(varargin)
   disp(sprintf('loading file %s...',varargin{i}));
   load(varargin{i});
   
   if ~isequal(conditions,oldc)
      warning(sprintf('conditions listed in %s do not match.',varargin{i}));
   end

    errDataOut = aggresults(errDataOut,errData);
   timeDataOut = aggresults(timeDataOut,timeData);

   clear errData timeData;
   disp('done.');

end

disp('Saving aggregated file...');
errData = errDataOut;
timeData = timeDataOut;
clear errDataOut timeDataOut oldc;
save(outputfile,'errData','timeData','conditions','trusters');
disp('done.');



