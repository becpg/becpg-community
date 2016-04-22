/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
package fr.becpg.tools.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

/**
 * 
 * @author matthieu
 *
 */
public class UserNameHelper {

	public static final Pattern userNamePattern = Pattern.compile("(.*)\\$(.*)@(.*)");
	public static final Pattern userNameAndTicketPattern = Pattern.compile("(.*)#(.*)");

	public static String extractLogin(String username) {

		Matcher ma = userNamePattern.matcher(username);
		if (ma.matches()) {

			if (!"default".equals(ma.group(3))) {
				return ma.group(2) + "@" + ma.group(3);
			} else {
				return ma.group(2);
			}
		}
		return null;
	}

	public static String extractTicket(String authToken) {
		if (Base64.isBase64(authToken)) {
			authToken = StringUtils.newStringUtf8(Base64.decodeBase64(authToken));
		}
		Matcher ma = userNameAndTicketPattern.matcher(authToken);
		if (ma.matches()) {
			return ma.group(2);
		}
		return null;

	}

	public static String buildAuthToken(String username, String alfTicket) {
		return username + "#" + alfTicket;
	}

	public static String extractUserName(String authToken) {
		if (Base64.isBase64(authToken)) {
			authToken = StringUtils.newStringUtf8(Base64.decodeBase64(authToken));
		}
		
		Matcher ma = userNameAndTicketPattern.matcher(authToken);
		if (ma.matches()) {
			return ma.group(1);
		}
		return null;
	}

}
