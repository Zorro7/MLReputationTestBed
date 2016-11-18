% returns number of dimensions for this distribution's domain
function n = dims(t)

n = sqrt(2.25 + 2*size(t.params,2)) - 1.5;

