package fr.becpg.olap.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JdbcConnectionManager {

	private String dbUser;

	private String dbPassword;

	private String dbConnectionUrl;

	
	
	public JdbcConnectionManager(String dbUser, String dbPassword, String dbConnectionUrl) {
		super();
		this.dbUser = dbUser;
		this.dbPassword = dbPassword;
		this.dbConnectionUrl = dbConnectionUrl;
	}

	private static Log logger = LogFactory.getLog(JdbcConnectionManager.class);

	static {

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			logger.error(e, e);
		}
	}

	public int update(String sql, Object[] objects) throws SQLException {
		try (Connection connection = DriverManager.getConnection(dbConnectionUrl, dbUser, dbPassword);
				PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {

			for (int i = 0; i < objects.length; i++) {
				pst.setObject(i+1, objects[i]);
			}
			pst.executeUpdate();
			try (ResultSet rs = pst.getGeneratedKeys();) {
				if (rs != null && rs.next()) {
					return rs.getInt(1);
				}
			}
		}
		throw new IllegalStateException();
	}

}
