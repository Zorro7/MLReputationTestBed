% GGTRUSTER/ISSAMPLINGOK checks if hyperparamaters encode sufficient
% information to enable valid sampling.
function ok = isSamplingOK(obj)
                     
   ok = ( size(obj.b,1) <= obj.a );
                              
end
