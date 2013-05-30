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
		return cleanName(name).replaceAll(".","-");
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
