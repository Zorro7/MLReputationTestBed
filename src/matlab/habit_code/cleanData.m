function data = cleanData(data)


data = log(data);

for k=1:53
   for q=1:4
      
      missing = isnan(data(:,k,q));
      
      data(missing,k,q) = randsample(data(~missing,k,q),sum(missing));
      
      neworder = randperm(size(data,1));
      
      data(:,k,q) = data(neworder,k,q);
    
   end
end

