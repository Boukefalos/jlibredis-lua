package util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TypeUtils {
    public static <T> Map<T,T> listToMap(List<T> list) {
        Map<T, T> map = new HashMap<T, T>();
        Iterator<T> iterator = list.iterator();
        while (iterator.hasNext()) {
            map.put(iterator.next(), iterator.next());
        }
        return map;
    }
}
