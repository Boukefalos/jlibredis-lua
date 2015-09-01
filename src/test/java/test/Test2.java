package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONValue;

import redis.clients.jedis.JedisPool;
import util.LuaUtils;

public class Test2 {
    public static void main(String[] args) throws IOException {
        JedisPool pool = new JedisPool("localhost", 16379);
        LuaUtils.setPool(pool);

        Object o1 = LuaUtils.getSome("departures", "airline", "arrival");
        System.out.println(o1);

        List<String> src = new ArrayList<String>();
        src.add("msgpack");
        src.add("kumofs");
        src.add("viver");

        String arg = JSONValue.toJSONString(src);
        Object object = LuaUtils.eval("msgpack", 0, arg);
        System.out.println(object);
    }
}