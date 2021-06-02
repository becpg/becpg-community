/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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
package fr.becpg.repo.olap.data;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * <p>OlapContext class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class OlapContext implements Closeable{

	final CloseableHttpClient session;
	
	final String currentUser;
	
	final String authToken;
	
	final String uuid;

	/**
	 * <p>Getter for the field <code>session</code>.</p>
	 *
	 * @return a {@link org.apache.http.impl.client.CloseableHttpClient} object.
	 */
	public CloseableHttpClient getSession() {
		return session;
	}


	/**
	 * <p>Getter for the field <code>currentUser</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCurrentUser() {
		return currentUser;
	}


	/**
	 * <p>Getter for the field <code>authToken</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getAuthToken() {
		return authToken;
	}


	/**
	 * <p>Getter for the field <code>uuid</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getUuid() {
		return uuid;
	}


	/**
	 * <p>Constructor for OlapContext.</p>
	 *
	 * @param currentUser a {@link java.lang.String} object.
	 * @param authToken a {@link java.lang.String} object.
	 */
	public OlapContext(String currentUser, String authToken) {
		super();
		this.session = HttpClientBuilder.create().build();
		this.currentUser = currentUser;
		this.authToken = authToken;
		this.uuid = UUID.randomUUID().toString();
	}


	/** {@inheritDoc} */
	@Override
	public void close() throws IOException {
		session.close();
	}

}
