local result = {}
local ids = redis.call('SMEMBERS', ARGV[1] .. ':_id')
for _, id in pairs(ids) do
	result[#result + 1] = redis.call('HGETALL',  ARGV[1] .. ':' .. id)	
end
local fields = {}
for i = 2, #ARGV  do
    fields[#fields + 1] = 'GET'
    fields[#fields + 1] = ARGV[1] .. ':*:' .. ARGV[i]
end
return redis.call('SORT', ARGV[1] .. ':_id', 'BY', 'NOSORT', unpack(fields))