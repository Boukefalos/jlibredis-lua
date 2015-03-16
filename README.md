This project aims to extend [jedis](https://github.com/xetorthio/jedis) or its fork [jlibredis](https://github.com/Boukefalos/jlibredis) to enable relational database-type operations. Inspiration is taken from the great articles [Redis Set Intersection - Using Sets to Filter Data](http://robots.thoughtbot.com/redis-set-intersection-using-sets-to-filter-data) and [Bitwise Lua Operations in Redis](http://blog.jupo.org/2013/06/12/bitwise-lua-operations-in-redis/).

To this end, Lua scripts are executed at the redis server. Dependencies in these scripts are automatically resolved by the Java wrapper.

Currently, the project is a rather unorganised set of test code. Interesting Lua snippets awaiting implementation are listed in evalsha.txt.