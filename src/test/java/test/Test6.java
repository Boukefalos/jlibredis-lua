package test;

import java.io.IOException;

import redis.clients.jedis.JedisPool;
import util.LuaUtils;

public class Test6 {
	public static void main(String[] args) throws IOException {
		JedisPool pool = new JedisPool("localhost", 16379);
		LuaUtils.setPool(pool);
		
		Object o1 = LuaUtils.eval("returnnil");
		System.out.println("1 returnnil: " + o1);
		
		Object o2 = LuaUtils.eval("returnint");
		System.out.println("2 returnint: " + o2);
		
		Object o3 = LuaUtils.eval("returnstring");
		System.out.println("3 returnstring: " + o3);

		Object o4 = LuaUtils.eval("returnset");
		System.out.println("4 returnset: " + o4);

		// solves: https://github.com/xetorthio/jedis/issues/383
	}
}
