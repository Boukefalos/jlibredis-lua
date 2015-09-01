package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Transaction;
import redis.clients.util.SafeEncoder;
import util.DateUtils;
import util.StringUtils;
import data.Entry;

public class Model {
    public static final int BATCH_SIZE = 10;

    protected Calendar calendar;

    protected JedisPool pool;
    protected Jedis jedis;
    protected Transaction transaction;

    protected int insertDataBatchCount, insertExtremesBatchStatementCount;
    protected PreparedStatement insertDataBatchStatement, insertExtremesBatchStatement;

    //private Pipeline pipeline;

    public Model(Calendar calendar) {
        this.calendar = calendar;
        pool = new JedisPool("localhost", 16379);
        jedis = pool.getResource();
        jedis.select(1);
        //clear();
    }

    public void clear() {
        Set<String> set = jedis.keys("*");
        if (set.size() > 0) {
            jedis.del(set.toArray(new String[0]));
        }
    }

    public void createDataTable() throws SQLException {
    }

    public void createIntervalsTable() throws SQLException {
    }

    public void createExtremesTable() throws SQLException {
    }

    public Date getFirstEntryDate() throws SQLException {
        List<String> list = jedis.sort("data", new SortingParams().limit(1, 1));
        long timestamp = 0;
        if (list.size() > 0) {
            timestamp = Long.valueOf(list.get(0));
        }
        calendar.setTimeInMillis(1000 * timestamp);
        return calendar.getTime();
    }

    public Date getLastEntryDate() throws SQLException {
        List<String> list = jedis.sort("data", new SortingParams().limit(1, 1).desc());
        long timestamp = 0;
        if (list.size() > 0) {
            timestamp = Long.valueOf(list.get(0));
        }
        calendar.setTimeInMillis(1000 * timestamp);
        return calendar.getTime();
    }

    public void insertInterval(String name) throws SQLException {
        Long id = jedis.incr("global:next-interval-id");
        String parameter = StringUtils.parameterize(name);
        jedis.set(String.format("interval:%d:name", id), name);
        jedis.set(String.format("interval:%s:id", parameter), String.valueOf(id));
    }

    public int selectIntervalId(String name) throws Exception {
        String parameter = StringUtils.parameterize(name);
        return Integer.valueOf(jedis.get(String.format("interval:%s:id", parameter)));
    }

    public ArrayList<Entry> selectDataBetween(Date intervalStartDate, Date intervalEndDate) {
//        PreparedStatement selectStatement = connection.prepareStatement("select * from data where date between ? and ?");
//        selectStatement.setLong(1, intervalStartDate.getTime() / 1000);
//        selectStatement.setLong(2, intervalEndDate.getTime() / 1000);
//        ResultSet resultSet = selectStatement.executeQuery();
        long min = intervalStartDate.getTime() / 1000;
        long max = intervalEndDate.getTime() / 1000;
        String key = String.format("data:%s:%s", min, max);        
        
        Set<String> dateSet = jedis.zrangeByScore("data", min, max);

        String[] dateArray = dateSet.toArray(new String[0]);
        ArrayList<Entry> entryList = new ArrayList<Entry>();
        if (dateArray.length == 0) {
            return entryList;
        }
        jedis.sadd(SafeEncoder.encode(key), SafeEncoder.encodeMany(dateArray));
        List<String> valueList = jedis.sort(key, new SortingParams().nosort().get("data:*:value"));
        
        Iterator<String> iterator = dateSet.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            calendar.setTimeInMillis(1000 * Long.valueOf(iterator.next()));
            entryList.add(new Entry(calendar.getTime(), Double.valueOf(valueList.get(i++))));
        }
        return entryList;
    }

    public void insertDataBatch(long date, double value) throws SQLException {
//        if (pipeline == null) {
//            pipeline = jedis.pipelined();
//            pipeline.multi();
//        }
//        if (insertDataBatchStatement == null) {
//            insertDataBatchStatement = connection.prepareStatement("insert or ignore into data values(?, ?)");
//            insertDataBatchCount = 0;
//        }
//        setDate(insertDataBatchStatement, 1, date);
//        insertDataBatchStatement.setDouble(2, value);
//        insertDataBatchStatement.addBatch();
//
//         if(++insertDataBatchCount % BATCH_SIZE == 0) {
//             insertDataBatchStatement.executeBatch();
//             System.out.println(insertDataBatchCount);
//        }
        date /= 1000;
        jedis.zadd(String.format("data"), date, String.valueOf(date));
        jedis.set(String.format("data:%s:value", date), String.valueOf(value));
    }

    public void insertDataBatchLast() throws SQLException {
        //pipeline.exec();
        //pipeline = null;
    }

    public void insertExtremesBatch(int id, Date date, Date firstDate, Date lastDate, int count, Double min, Double max) throws SQLException {
//        if (pipeline == null) {
//            pipeline = jedis.pipelined();
//            pipeline.multi();
//        }
        
//        HashMap<String, String> map = new HashMap<String, String>();
//        map.put("first", String.valueOf(firstDate.getTime() / 1000));
//        map.put("last", String.valueOf(lastDate.getTime() / 1000));
//        map.put("count", String.valueOf(count));
//        map.put("min", String.valueOf(min));
//        map.put("max", String.valueOf(max));

        int dateInt = (int) date.getTime() / 1000;
        
        
        String key = String.format("extreme:%d:%d:", id, dateInt);
        jedis.set(key + "first", String.valueOf(firstDate.getTime() / 1000));
        jedis.set(key + "last", String.valueOf(lastDate.getTime() / 1000));
        jedis.set(key + "count", String.valueOf(count));
        jedis.set(key + "min", String.valueOf(min));
        jedis.set(key + "max", String.valueOf(max));
        
        
        jedis.zadd(String.format("interval:%d", id), dateInt, String.valueOf(dateInt));
        
        //jedis.hmset(String.format("extreme:%d:%d", id, date.getTime() / 1000), map);

        if(++insertExtremesBatchStatementCount % BATCH_SIZE == 0) {
             //pipeline.exec();
             //pipeline.multi();
             System.out.println(insertExtremesBatchStatementCount);
        }
    }
    
    public void insertExtremesBatchLast() throws SQLException {
        //pipeline.exec();
        //pipeline = null;
    }

    protected void listData() throws SQLException {
//        ResultSet resultSet = statement.executeQuery("select *,datetime(date, 'unixepoch') as fmt from data");
//        while (resultSet.next()) {
//            System.out.println("--------------------------");
//            System.out.println("date = " + resultSet.getString("date"));
//            System.out.println("value = " + resultSet.getFloat("value"));
//            System.out.println("format = " + resultSet.getString("fmt"));
//        }
    }

    protected String getField(ResultSet resultSet, String columnLabel) throws SQLException {
        return resultSet.getString(columnLabel);
    }
    
    protected void printField(ResultSet resultSet, String columnLabel) throws SQLException {
        System.out.printf("%s = %s\n", columnLabel, resultSet.getString(columnLabel));
    }

    protected void setDate(PreparedStatement statement, int parameterIndex, Date date) throws SQLException {
        setDate(statement, parameterIndex, date.getTime());
    }

    protected void setDate(PreparedStatement statement, int parameterIndex, long date) throws SQLException {
        statement.setLong(parameterIndex, date / 1000);
    }

    public ResultSet getExtremes(long startDate, long endDate, String name) throws SQLException {
//        PreparedStatement selectStatement = connection.prepareStatement(
//                "select extremes.date date, extremes.first date_first, extremes.last date_last, extremes.count count, extremes.min min_value, extremes.max max_value, first.value first_value, last.value last_value " +
//                "from extremes " +
//                "left join data first on extremes.first = first.date " +
//                "left join data last on extremes.last = last.date " +
//                "where interval_id = (select id from intervals where name = ?) and extremes.date between ? and ?");
//        selectStatement.setString(1, name);
//        setDate(selectStatement, 2, startDate);
//        setDate(selectStatement, 3, endDate);
//        return selectStatement.executeQuery();
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public String getJSON(long startDate, long endDate, String name) throws SQLException {
        ResultSet resultSet = getExtremes(
                DateUtils.makeTimestamp(2012, 1, 1, 14, 0, 0, 0),
                DateUtils.makeTimestamp(2013, 7, 1, 14, 0, 0, 0), "Day");
        JSONArray jsonList = new JSONArray();
        while (resultSet.next()) {
            JSONArray jsonEntry = new JSONArray();
            jsonEntry.add(1000 * resultSet.getLong("date"));
            jsonEntry.add(resultSet.getDouble("first_value"));
            jsonEntry.add(resultSet.getDouble("min_value"));
            jsonEntry.add(resultSet.getDouble("max_value"));
            jsonEntry.add(resultSet.getDouble("last_value"));
            jsonList.add(jsonEntry);
        }
        return jsonList.toJSONString();
    }
    
    
    public void test(long startDate, long endDate, String name) throws SQLException {
        ResultSet resultSet = getExtremes(startDate, endDate, name);
        while (resultSet.next()) {
            System.out.println("----------------");
            System.out.printf("# %d\n", resultSet.getRow());
            printField(resultSet, "date");
            printField(resultSet, "date_first");
            printField(resultSet, "count");
            printField(resultSet, "min_value");
            printField(resultSet, "max_value");
            printField(resultSet, "first_value");
            printField(resultSet, "last_value");
        }
    }

    public void test() {
        /*Transaction transaction = jedis.multi();
        Pipeline pipeline = jedis.pipelined();
        Client client = jedis.getClient();*/
        
        // ZRANGEBYSCORE data 1375085460 1375088460
        // SORT data BY NOSORT GET data:*:value

        int min = 1375085460;
        int max = 1375088460;
        String key = String.format("data:%s:%s", min, max);
        
        Set<String> set = jedis.zrangeByScore("data", min, max);
        jedis.sadd(SafeEncoder.encode(key), SafeEncoder.encodeMany(set.toArray(new String[0])));
        List<String> list = jedis.sort(key, new SortingParams().nosort().get("data:*:value"));
        for (String string : list) {
            System.out.println(string);
        }
        
        Map<String, String> a = jedis.hgetAll("extreme:2");
        System.out.println(a.get("date"));
        //jedis.sort(date, new SortingParams().nosort().get("extreme:*"));
        

        //set = jedis.zrange("interval:1", 0, -1);
        
        min = 0;
        max = Integer.MAX_VALUE;
        key = String.format("interval:1:%s:%s", min, max);
        
        set = jedis.zrangeByScore("interval:1", min, max);
        jedis.sadd(SafeEncoder.encode(key), SafeEncoder.encodeMany(set.toArray(new String[0])));
        
        list = jedis.sort(key, new SortingParams().nosort().get(new String[] {
            "extreme:1:*:count",
            "extreme:1:*:first"}));
        //Iterator<String> iterator = set.iterator();
        for (String string : list) {
            System.out.println(string);
        }
    }
}
