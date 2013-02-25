package fr.becpg.olap.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author matthieu
 * Warning not thread safe transaction (use single thread or modify)
 */
public class JdbcConnectionManager {

	private String dbUser;

	private String dbPassword;

	private String dbConnectionUrl;
	

	public interface JdbcConnectionManagerCallBack {

		public void execute(JdbcConnectionManager jdbcConnectionManager) throws Exception;

	}

	private Connection connection;
	

	public static void doInTransaction(JdbcConnectionManager  jdbcConnectionManager, JdbcConnectionManagerCallBack callback ) throws Exception{
		jdbcConnectionManager.initConnection();
		
		try(Connection connection = jdbcConnectionManager.connection){
			connection.setAutoCommit(false);
			try {
				callback.execute(jdbcConnectionManager);
			} catch(Exception e){
				logger.error("Rollback transaction caused by: "+e.getMessage(),e);
				connection.rollback();
			}
			connection.commit();
		}
		
		
	};
	

	public interface RowMapper<T> {
		public T mapRow(ResultSet rs, int line) throws SQLException;
	}

	public JdbcConnectionManager(String dbUser, String dbPassword, String dbConnectionUrl) {
		super();
		this.dbUser = dbUser;
		this.dbPassword = dbPassword;
		this.dbConnectionUrl = dbConnectionUrl;
		
	}
	
	public void initConnection() throws SQLException{
		this.connection = DriverManager.getConnection(dbConnectionUrl, dbUser, dbPassword);
	}

	private static Log logger = LogFactory.getLog(JdbcConnectionManager.class);

	static {

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			logger.error(e, e);
		}
	}

	public Long update(String sql, Object[] objects) throws SQLException {
		
	
		
		try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {

			for (int i = 0; i < objects.length; i++) {
				pst.setObject(i + 1, objects[i]);
			}
			pst.executeUpdate();
			try (ResultSet rs = pst.getGeneratedKeys();) {
				if (rs != null && rs.next()) {
					return rs.getLong(1);
				}
			}
		}
		
		return -1L;
	}

	public <T> List<T> list(String sql, RowMapper<T> rowMapper) throws SQLException {

		List<T> ret = new ArrayList<T>();
		try (Connection connection = DriverManager.getConnection(dbConnectionUrl, dbUser, dbPassword);
				Statement st = connection.createStatement();
				ResultSet rs = st.executeQuery(sql);) {
			if (rs != null) {
				int line = 0;
				while (rs.next()) {
					ret.add(rowMapper.mapRow(rs, line));
					line++;
				}

			}

			return ret;
		}
	}

}
