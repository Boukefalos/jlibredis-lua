function util.getsome(table, fields)
	local result = {}
	local ids = redis.call('SMEMBERS', table .. ':_id')
	for _, id in pairs(ids) do
		result[#result + 1] = redis.call('HMGET',  table .. ':' .. id, unpack(fields))	
	end
	util.bla()
	return result
end