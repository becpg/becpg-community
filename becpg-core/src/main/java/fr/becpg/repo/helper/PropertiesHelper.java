package fr.becpg.repo.helper;

public class PropertiesHelper {

	/**
	 * remove invalid characters.
	 *
	 * @param name the name
	 * @return the string
	 */
	public static String cleanName(String name) {
		/*(.*[\"\*\\\>\<\?\/\:\|]+.*)|(.*[\.]?.*[\.]+$)|(.*[ ]+$) */
		return name!=null? name.replaceAll("([\"*\\><?/:|])", "-").trim(): null;
	}	
	
	public static String cleanFolderName(String name) {
		String ret = cleanName(name);
		return ret!=null ? ret.replaceAll("\\.","-"): null;
	}	
	
	
	/**
	 * remove invalid characters (trim).
	 *
	 * @param value the value
	 * @return the string
	 */
	public static String cleanValue(String value) {		
		return value!=null? value.trim(): null;		
	}
	
}
