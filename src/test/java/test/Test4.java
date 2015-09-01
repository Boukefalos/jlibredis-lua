package test;

import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;

public class Test4 {

    public static void main(String[] args) {
        List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
        JedisShardInfo si = new JedisShardInfo("localhost", 16379);
        //si.setPassword("foobared");
        shards.add(si);
        ShardedJedis jedis = new ShardedJedis(shards);
        
        jedis.set("fooder", "bar");
        for (int i = 0; i < 100; ++i ) {
            jedis.zadd(String.format("item"), i, String.valueOf(i));
        }

        //jedis.zrange(key, start, end)
        String value = jedis.get("fooder");
        System.out.println(value);
        
        //jedis.close();
    }
}
