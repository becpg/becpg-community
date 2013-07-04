package fr.becpg.olap.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author matthieu
 * 
 */
public class JdbcConnectionManager {

	private String dbUser;

	private String dbPassword;

	private String dbConnectionUrl;

	private DataSource dataSource;

	private static Log logger = LogFactory.getLog(JdbcConnectionManager.class);

	public interface JdbcConnectionManagerCallBack {

		public void execute(Connection connection) throws Exception;

	}

	public void doInTransaction(JdbcConnectionManagerCallBack callback) throws Exception {

		try (Connection connection = createConnection()) {
			connection.setAutoCommit(false);
			try {
				callback.execute(connection);
			} catch (Exception e) {
				logger.error("Rollback transaction caused by: " + e.getMessage(), e);
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

	public JdbcConnectionManager() {
		super();
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private Connection createConnection() throws SQLException {
		if (dataSource != null) {
			return dataSource.getConnection();
		} else {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
			} catch (Exception e) {
				logger.error(e, e);
			}
			return DriverManager.getConnection(dbConnectionUrl, dbUser, dbPassword);
		}
	}

	

	public <T> List<T> list(String sql, RowMapper<T> rowMapper, Object[] objects) throws SQLException {

		List<T> ret = new ArrayList<T>();
		try (Connection connection = createConnection(); PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
			for (int i = 0; i < objects.length; i++) {
				pst.setObject(i + 1, objects[i]);
			}
			try (ResultSet rs = pst.executeQuery();) {
				if (rs != null) {
					int line = 0;
					while (rs.next()) {
						ret.add(rowMapper.mapRow(rs, line));
						line++;
					}

				}
			}

			return ret;
		}
	}

}
