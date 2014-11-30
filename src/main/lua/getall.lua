local result = {}
local ids = redis.call('SMEMBERS', ARGV[1] .. ':_id')
for _, id in pairs(ids) do
	result[#result + 1] = redis.call('HGETALL',  ARGV[1] .. ':' .. id)	
end
return result