package test;

import java.io.IOException;

import redis.clients.jedis.JedisPool;
import util.LuaUtils;

public class Test7 {
    public static void main(String[] args) throws IOException {
        JedisPool pool = new JedisPool("localhost", 16379);
        LuaUtils.setPool(pool);

        // http://blog.jupo.org/2013/06/12/bitwise-lua-operations-in-redis/
        Object o1 = LuaUtils.eval("anon");
        System.out.println(o1);
    }
}