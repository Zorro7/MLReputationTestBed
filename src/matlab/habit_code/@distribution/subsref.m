function b = subsref(a,s)

if isequal(s.type,'.')
 b = eval(['a.' s.subs]);
end
