package fr.becpg.repo.helper;

public class SiteHelper {

	static String SITES_SPACE_QNAME_PATH = "/app:company_home/st:sites/";
	
	public static String extractSiteId(String path, String displayPath)
	{
		 String siteId = null;
	
	   if (path.contains(SITES_SPACE_QNAME_PATH))
	   {
	      String tmp = path.substring(SITES_SPACE_QNAME_PATH.length());
	      int  pos = tmp.indexOf('/');
	      if (pos >= 1)
	      {
	    	 siteId = displayPath.split("/")[3];
	      }
	   }
	   
	   return siteId;
	}
	
	public static String extractContainerId(String path)
	{
		
		 String containerId = null;
	   if (path.contains(SITES_SPACE_QNAME_PATH))
	   {
	      String tmp = path.substring(SITES_SPACE_QNAME_PATH.length());
	      int  pos = tmp.indexOf('/');
	      if (pos >= 1)
	      {
	       
	         tmp = tmp.substring(pos + 1);
	         pos = tmp.indexOf('/');
	         if (pos >= 1)
	         {
	            // strip container id from the path
	            containerId = tmp.substring(0, pos);
	            containerId = containerId.substring(containerId.indexOf(":") + 1);
	            
	         }
	      }
	   }
	   
	   return containerId;
	}

	public static String extractDisplayPath(String path, String displayPath) {
		String ret = ""; 
		
		if (path.contains(SITES_SPACE_QNAME_PATH))
		   {
		      String[] splitted = displayPath.split("/");
		      
		      for (int i = Math.min(5,splitted.length); i < splitted.length; i++) {
		    	  if(ret.length()>0){
		    		  ret+="/";
		    	  }
		    	  ret+=splitted[i];
		      }
		      
		   } else {
			   ret = displayPath;
		   }
		   
		   return ret;
	}
	
	public static boolean isSitePath(String path) {
		boolean isSitePath = false;

		if (path.startsWith(SITES_SPACE_QNAME_PATH)) {
			isSitePath = true;
		}

		return isSitePath;
	}
	
}
