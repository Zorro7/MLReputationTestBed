function a = subsasgn(a, s, b)

if isequal(s.type,'.')
 eval(['a.' s.subs '=b;']);
else
   error('Only structure subscript supported');
end
