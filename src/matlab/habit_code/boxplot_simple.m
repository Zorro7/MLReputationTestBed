% creates a simple box plot from the clean data

%%
load cleanData;
data = exp(data);

%%

boxplot(data(:,randsample(50,20),1),...
   'notch','on','datalim',[0,300],'extrememode','compress','jitter',0)
%%

%%

ylabel('response time (ms)');
xlabel('webserver');