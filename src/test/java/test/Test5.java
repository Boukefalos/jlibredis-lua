package test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import model.Model;

import org.apache.commons.codec.digest.DigestUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import util.DateUtils;
import util.FileUtils;
import util.LuaUtils;

public class Test5 {

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		JedisPool pool = new JedisPool("localhost", 16379);

		Jedis jedis = new Jedis("localhost", 16379);
		jedis.select(0);
		
		String lua1 = FileUtils.resourceToString("test1.lua");
		String lua2 = FileUtils.resourceToString("test2.lua");

		String lua1hash = DigestUtils.sha1Hex(lua1);
		String lua2hash = DigestUtils.sha1Hex(lua2);
		
		System.out.println(jedis.eval(lua1, 0));
		System.out.println(jedis.scriptLoad(lua1));
		System.out.println(jedis.scriptLoad(lua2));
		System.out.println(jedis.evalsha(lua1hash, 0));
		System.out.println(jedis.evalsha(lua2hash, 0));

		jedis.select(1);
		Model model = new Model(DateUtils.getCalendar());
		System.out.println(model.selectDataBetween(new Date(1372629840000L), new Date(1375076100000L)));
		
		LuaUtils.setPool(pool);
		System.out.println(LuaUtils.eval("test3"));
		
		//jedis.close();
	}
}
