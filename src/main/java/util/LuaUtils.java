package util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;







import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisDataException;

public class LuaUtils {
	public static final boolean LOAD = true;
	public static final List<String> EMPTY = new ArrayList<String>();
	public static final String[] LIBRARY_ARRAY = {"redis", "table", "string", "math", "debug", "cjson", "cmsgpack"};
	public static final List<String> LIBRARY_LIST = Arrays.asList(LIBRARY_ARRAY);

	protected static JedisPool jedisPool;
	protected static HashMap<String, String> hashMap = new HashMap<String, String>();

	public static void setPool(JedisPool pool) {
		LuaUtils.jedisPool = pool;		
	}

	public static Object eval(String name, List<String> keys, List<String> args) throws IOException {
		Jedis jedis = jedisPool.getResource();
		String hash = getHash(name, jedis);	
		System.out.println(hash);
		Object object = jedis.evalsha(hash, keys, args);
		jedisPool.returnResource(jedis);
		return object;
	}

	public static Object eval(String name, int keys, String... args) throws IOException {
		Jedis jedis = getResource();
		String hash = getHash(name, jedis);
		try {
			return jedis.evalsha(hash, keys, args);
		} catch (JedisDataException e) {
			if (e.getMessage().startsWith("NOSCRIPT")) {
				// Script not loaded correctly
			}
			System.out.println(e);
			throw e;
		} finally {
			returnResource(jedis);
		}
	}

	public static Object eval(String name) throws IOException {
		return eval(name, 0);
	}

	public static Object eval(String string, List<String> params) throws IOException {
		return eval(string, EMPTY, params);
	}
	
	protected static String getHash(String name, Jedis jedis) throws IOException {
		name = String.format("%s.lua", name);
		if (hashMap.containsKey(name)) {
			return hashMap.get(name);
		} else {
			String lua = FileUtils.resourceToString(name);

			Map<String, List<String>> libraryMap = new HashMap<String, List<String>>();

			lua = resolveDepencencies(lua, libraryMap);
			for (String library : libraryMap.keySet()) {
				lua = String.format("local %s = {}\n%s", library, lua);
			}
			System.out.println("======");
			System.out.println(lua);
			System.out.println("======");
			String hash = DigestUtils.sha1Hex(lua);
			if (!jedis.scriptLoad(lua).equals(hash)) {
				// Hashes don't match
			}
			hashMap.put(name, hash);
			return hash;
		}
	}

	protected static String resolveDepencencies(String lua, Map<String, List<String>> libraryMap) throws IOException {
		Pattern pattern = Pattern.compile(String.format("([a-z]+)%s([a-z]+)", Pattern.quote(".")));
		Matcher matcher = pattern.matcher(lua);
		String depencencyLua = "";
		while (matcher.find()) {
			String library = matcher.group(1);
			if (!LIBRARY_LIST.contains(library)) {
				if (!libraryMap.containsKey(library)) {					
					libraryMap.put(library, new ArrayList<String>());
				}
				List<String> methodList = libraryMap.get(library);
				String method = matcher.group(2);
				if (!methodList.contains(method)) {
					String file = String.format("%s/%s.lua", library, method);
					System.out.println(file);
					String methodLua = FileUtils.resourceToString(file);					
					methodList.add(method);
					String subDepencencyLua = resolveDepencencies(methodLua, libraryMap);
					if (depencencyLua.isEmpty()) {
						depencencyLua = subDepencencyLua;
					} else if (!subDepencencyLua.isEmpty()) {
						depencencyLua = String.format("%s\n%s", depencencyLua, subDepencencyLua);
					}
				}
			}
		}
		if (depencencyLua.isEmpty()) {
			return lua;
		} else {
			return String.format("%s\n%s", depencencyLua, lua);
		}
	}

    protected static void returnResource(Jedis jedis) {
        jedisPool.returnResource(jedis);
    }

    protected static Jedis getResource() {
        return jedisPool.getResource();
    }

	public static Object insert(String table, Map<String, String> hash) throws IOException {
		List<String> params = new ArrayList<String>();
		for (final Entry<String, String> entry : hash.entrySet()) {
			params.add(entry.getKey());
			params.add(entry.getValue());
			params.add(StringUtils.parameterize(entry.getValue()));
		}
		params.add(0, table);
		return LuaUtils.eval("insert", params);		
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, String>> getAll(String table) throws IOException {
		List<String> params = new ArrayList<String>();
		params.add(table);		
		List<List<String>> listList = (List<List<String>>) LuaUtils.eval("getall", params);
		List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
		System.out.println(listList.size());
		for (List<String> list : listList) {
			mapList.add(TypeUtils.listToMap(list));
		}
		return mapList;
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, String>> getSome(String... stringArray) throws IOException {
		List<List<String>> listList = (List<List<String>>) LuaUtils.eval("getsome", Arrays.asList(stringArray));
		List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
		for (List<String> list : listList) {
			Map<String, String> map = new HashMap<String, String>();
			for (int i = 1; i < stringArray.length; ++i) {
				map.put(stringArray[i], list.get(i - 1));
			}
			mapList.add(map);
		}
		return mapList;
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, String>> getSomeX(String... stringArray) throws IOException {
		List<String> list = (List<String>) LuaUtils.eval("getsomex", Arrays.asList(stringArray));
		Iterator<String> iterator = list.iterator();
		List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
		while (iterator.hasNext()) {
			Map<String, String> map = new HashMap<String, String>();
			for (int i = 1; i < stringArray.length; ++i) {
				map.put(stringArray[i], iterator.next());
			}
			mapList.add(map);
		}
		return mapList;
	}
}
