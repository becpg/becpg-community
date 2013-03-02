package fr.becpg.olap.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author matthieu
 *
 */
public class UserNameHelper {

	public static Pattern userNamePattern = Pattern.compile("(.*)\\$(.*)@(.*)");
	
	
	public static String extractLogin(String username){
		Matcher ma = userNamePattern.matcher(username);
		if (ma.matches()) {
			
			if(!"default".equals(ma.group(3))){
				return ma.group(2)+"@"+ma.group(3);
			} else {
				return ma.group(2);
			}
		}
		return null;
	}
	
	
	
	
}
