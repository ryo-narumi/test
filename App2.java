package sakuma.sanple;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;

/**
 * Hello world!
 *
 */
public class App2{

	private static Logger logger = LoggerFactory.getLogger(App2.class);

    public static void main( String[] args ){
    	logger.info("program start");
    	CqlSession session = null;

        try {
        	session = CqlSession.builder()
                    .addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
                    .withKeyspace("sakumatest")  // キースペース名
//                    .withLocalDatacenter("datacenter1") // データセンター名
                    .build();

        	// INSERT処理
        	insert(session);

        	// SELECT処理
        	select(session);

        } catch(Exception e) {

        	logger.info("",e);

        } finally {

        	logger.info("finally処理");
        	session.close();
        }
    }

    private static void insert(CqlSession session) {

    	logger.info( "start insert" );

    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String now = sdf.format(new Date());

    	session.execute(
        	    SimpleStatement.builder("INSERT INTO sakumatest.feed (id, timestamp, message) values (?,toTimestamp(now()),?)")
        	            .addPositionalValues("1", "{deviceId:d005, eventId:e002,eventTime:" + now + ",columnValue:あいう}")
        	            .build());
    }

    /**
     * CassandraからSELECTし、コンソールに出力する.
     *
     * @param session CqlSession
     */
    private static void select(CqlSession session) {

    	logger.info( "start select" );

        ResultSet rs = session.execute(
        	    SimpleStatement.builder("SELECT * FROM sakumatest.feed WHERE id=?")
        	            .addPositionalValue("1")
        	            .build());

        List<Row> rows = rs.all();

        for(Row temp : rows) {
        	logger.info("ID = " + temp.getString("id") + " message = " + temp.getString("message"));
        }
    }
}
