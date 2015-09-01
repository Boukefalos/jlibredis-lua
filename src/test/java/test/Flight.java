package test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import util.FileUtils;
import util.LuaUtils;

public class Flight {
    private SimpleDateFormat inputSDF;
    private JedisPool pool;

    public Flight() {
        inputSDF = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        pool = new JedisPool("localhost", 16379);
        LuaUtils.setPool(pool);
    }

    public static void main(String[] args) throws Exception {
        new Flight().start();
    }

    private void start() throws IOException, ParseException {
        clear();
        insert();
        list();
    }

    private void clear() {
        Jedis jedis = pool.getResource();
        Set<String> set = jedis.keys("*");
        if (set.size() > 0) {
            jedis.del(set.toArray(new String[0]));
        }
        pool.returnResource(jedis);
    }

    private void insert() throws IOException {
        // http://robots.thoughtbot.com/post/46335890055/redis-set-intersection-using-sets-to-filter-data
        Scanner lineScanner = FileUtils.resourceToScanner("flight.txt");
        lineScanner.nextLine();
        while (lineScanner.hasNextLine()) {
            Scanner scanner = new Scanner(lineScanner.nextLine());            
            HashMap<String,String> map = new HashMap<String,String>();            
            scanner.useDelimiter("\t");
            scanner.nextInt(); // id
            String date = scanner.next();
            long time;
            try {
                time = inputSDF.parse(date).getTime() / 1000;
                map.put("departure_time", String.valueOf(time));
                map.put("airline", scanner.next());
                map.put("departure", scanner.next());
                map.put("arrival", scanner.next());
                long id = (Long) LuaUtils.insert("departures", map);
                System.out.println(id);

                Jedis jedis = pool.getResource();
                jedis.zadd("departures:departure_time", time, String.valueOf(id));
                pool.returnResource(jedis);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            scanner.close();
        }
        lineScanner.close();        
    }

    private void list() throws IOException {        
        Jedis jedis = pool.getResource();
        System.out.println(jedis.hgetAll("departures:1"));
        pool.returnResource(jedis);

        List<Map<String, String>> listMap;

        listMap = LuaUtils.getSome("departures", "airline", "arrival");
        for (Map<String, String> map : listMap) {
            System.out.println(map);
        }

        listMap = LuaUtils.getSomeX("departures", "airline", "arrival");
        for (Map<String, String> map : listMap) {
            System.out.println(map);
        }
        
        listMap = LuaUtils.getAll("departures");
        for (Map<String, String> map : listMap) {
            System.out.println(map);
        }
    }
}
