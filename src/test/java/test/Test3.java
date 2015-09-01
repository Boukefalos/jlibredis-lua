package test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import util.StringUtils;

public class Test3 {

    public static void main(String[] args) {
        System.out.println(StringUtils.parameterize("aDa halod823d!!@"));

        JedisPool pool = new JedisPool("localhost", 16379);
        Jedis jedis1 = pool.getResource();
        jedis1.select(2);
        
        jedis1.del("bla");
        jedis1.del("da");

        Jedis jedis2 = pool.getResource();
        
        Pipeline pipeline = jedis2.pipelined();
        pipeline.select(2);
        for (int i = 0; i < 10000; ++i) {
            pipeline.sadd("bla", String.valueOf(i));
            if (i % 10 == 0) {
                jedis1.sadd("da", String.valueOf(i));
            }
            if (i % 100 == 0) {
                System.out.println(jedis1.scard("bla"));
                System.out.println(jedis1.scard("da"));
            }
        }
        

        pipeline.sync();

        System.out.println(jedis1.scard("bla"));
        System.out.println(jedis1.scard("da"));
        
        pool.returnResource(jedis1);
        pool.returnResource(jedis2);
    }
}
