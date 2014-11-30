function list.slice(inx, offset, length)
	length = length or #inx
	local out = {} 
	for i = offset, length  do
	    out[#out + 1] = inx[i]
	end
	return out
end