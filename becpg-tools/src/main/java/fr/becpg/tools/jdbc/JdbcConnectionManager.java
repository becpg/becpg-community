/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.tools.jdbc.JdbcUtils.RowMapper;

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

	private static final Log logger = LogFactory.getLog(JdbcConnectionManager.class);

	public interface JdbcConnectionManagerCallBack {

		void execute(Connection connection) throws Exception;

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

		try(Connection connection = createConnection()){
			return JdbcUtils.list(connection, sql, rowMapper, objects);
			
		}
		
	}

}
