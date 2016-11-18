

% plot figure to illustrate gaussian smoothing

% generate samples
r = randn(1,30);

% fit distribution
m = mean(r);
s = std(r);

% plot figure
hist(r,2000);
hold on;
x = linspace(-5,5,200);
y = normpdf(x,m,s);
y = 3*y ./ max(y);

plot(x,y);

xlabel('random variable');
ylabel('probability density');


hold off;




