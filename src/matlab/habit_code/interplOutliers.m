function data = interplOutliers(data,r)
% Remove outliers and interplorate to fill the holes left behind.

% find and remove outliers
p = prctile(data,r);
p = repmat(p,[size(data,1) 1 1]);

data(data>p)=nan;

% for each column
for j=1:size(data,2)
   
   for k=1:size(data,3)
      
      % interpolate missing values
      missing = isnan(data(:,j,k));
      present = find(~missing);
      missing = find(missing);
      
      yi = interp1(present,data(present,j,k),missing);
      
      data(missing,j,k) = yi;
      
   end
   
end