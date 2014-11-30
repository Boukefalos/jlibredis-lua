local a = cmsgpack.pack("abcdefg")
local b = cmsgpack.unpack(a)
return cjson.decode(ARGV[1])