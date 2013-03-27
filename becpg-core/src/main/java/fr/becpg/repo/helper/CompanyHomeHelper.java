package fr.becpg.repo.helper;

public class CompanyHomeHelper {

	static String USER_SPACE_QNAME_PATH = "/app:company_home/app:user_homes/";
	
	
	public static boolean isInUserHome(String path) {
		boolean isInUserHome = false;

		if (path.startsWith(USER_SPACE_QNAME_PATH)) {
			isInUserHome = true;
		}

		return isInUserHome;
	}
	
}
