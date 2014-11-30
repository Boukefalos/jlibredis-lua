local id = redis.call('INCR', '_id:' .. ARGV[1])
redis.call('SADD', ARGV[1] .. ':_id', id) -- table:_id

local hash = {}
for i = 2, #ARGV, 3  do
	hash[#hash + 1] = ARGV[i]
	hash[#hash + 1] = ARGV[i + 1]
	redis.call('SET', ARGV[1] .. ':' .. id .. ':' ..  ARGV[i], ARGV[i + 1]) -- table:id:field
	redis.call('SADD', ARGV[1] .. ':' .. ARGV[i] .. ':' ..  ARGV[i + 2], id) -- table:field:value
	-- redis.call('SADD', ARGV[1] .. ':' .. ARGV[i], ARGV[i + 2]) -- table:field
end
--[[
for i = 1, 10, 1 do
	redis.call('KEYS', '*')
end
--]]
redis.call('HMSET', ARGV[1] .. ':' .. id, unpack(hash)) -- table:id
return id