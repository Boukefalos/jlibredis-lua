package test;

import java.util.Set;

import redis.clients.jedis.Jedis;

public class Test1 {

    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost", 16379);
        //jedis.select(0);
        
        jedis.set("foo", "bar");

        String value = jedis.get("foo");
        System.out.println(value);
        
        jedis.del("item");
        for (int i = 0; i < 10; ++i ) {
            jedis.zadd(String.format("item"), 2*i, String.valueOf(Math.random()));
            //jedis.sadd(String.format("items"), String.valueOf(i));
        }
        
        Set<String> set = jedis.zrange("item", 0, -1);
        for (String item : set) {
            System.out.println(item);
        }
        System.out.println("-----------");
        set = jedis.zrangeByScore("item", 4, 7);
        for (String item : set) {
            System.out.println(item);
        }
        
        //jedis.close();
    }
}
