package fr.becpg.olap.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcUtils {
	public static Long update(Connection connection, String sql, Object[] objects) throws SQLException {

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
}
