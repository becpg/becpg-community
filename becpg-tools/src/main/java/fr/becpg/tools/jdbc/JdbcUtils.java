/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.tools.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcUtils {
	

	public interface RowMapper<T> {
		T mapRow(ResultSet rs, int line) throws SQLException;
	}
	
	public static Long update(Connection connection, String sql, Object[] objects) throws SQLException {

		try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			for (int i = 0; i < objects.length; i++) {
				pst.setObject(i + 1, objects[i]);
			}
			pst.executeUpdate();
			try (ResultSet rs = pst.getGeneratedKeys()) {
				if (rs != null && rs.next()) {
					return rs.getLong(1);
				}
			}
		}

	return -1L;
}
	
	
	public static <T> List<T> list(Connection connection, String sql, RowMapper<T> rowMapper, Object[] objects) throws SQLException {

		List<T> ret = new ArrayList<>();
		try ( PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			for (int i = 0; i < objects.length; i++) {
				pst.setObject(i + 1, objects[i]);
			}
			try (ResultSet rs = pst.executeQuery()) {
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
