local foo = function(a)
   return {a, 2 * a}
end

return {foo(3), foo(4)}