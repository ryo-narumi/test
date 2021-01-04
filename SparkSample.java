package sakuma;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import com.datastax.spark.connector.japi.CassandraJavaUtil;

import sakuma.bean.valueobject.Feed;

public class SparkSample {

	private static final Logger logger = Logger.getLogger(SparkSample.class);

	public static void main(String[] args) {
	    // Spark設定
	    SparkConf conf = new SparkConf();
	    conf.setAppName("SampleReader");
	    conf.setMaster("local[*]");
	    conf.set("spark.cassandra.connection.host", "127.0.0.1");
	    conf.set("spark.cassandra.connection.port", "9042");

	    // Cassandraのkeyspaceとテーブル名
	    String keyspace = "sakumatest";
	    String tableUser = "feed";

	    JavaSparkContext sc = new JavaSparkContext(conf);
	    try {
	        SparkSession sparkSession = SparkSession.builder().master("local").appName("SampleReader")
	                .config("spark.sql.warehouse.dir", "file:////C:/apache/spark-data").getOrCreate();

	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	String now = sdf.format(new Date());

	        Feed feed = new Feed("060", new Date(), "{deviceId:c005, eventId:s005,eventTime:" + now + ",columnValue:かきくけこ}");
	        List<Feed> feeds = Arrays.asList(feed);

	        JavaRDD<Feed> feedsRdd = sc.parallelize(feeds);

//	        JavaRDD<Feed> feedRdd = CassandraJavaUtil.javaFunctions(sc)
//	                .cassandraTable(keyspace, tableUser, mapRowTo(Feed.class));

	        CassandraJavaUtil.javaFunctions(feedsRdd)
            .writerBuilder(keyspace, tableUser, mapToRow(Feed.class)).saveToCassandra();

	        // Cassandraからデータを読み出す
	        Dataset<Row> dataset = sparkSession.read().format("org.apache.spark.sql.cassandra")
	                .option("keyspace", keyspace)
	                .option("table", tableUser).load();

	        // データセットから配列を取得する
	        List<Row> asList = dataset.collectAsList();
	        for (Row r : asList) {
	            logger.info(r);
	        }
	    } catch (Exception e) {
	        logger.error(e);
	    } finally {
	        sc.stop();
	        sc.close();
	    }
	}

}
