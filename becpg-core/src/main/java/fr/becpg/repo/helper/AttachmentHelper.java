package fr.becpg.repo.helper;

import org.alfresco.repo.webdav.WebDAVHelper;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class AttachmentHelper {

	  private static final String HEADER_USER_AGENT     = "User-Agent";
	 
	  public  static void setAttachment(WebScriptRequest req, WebScriptResponse res, String attachFileName)
	    {
	            String headerValue = "attachment";
	            if (attachFileName != null && attachFileName.length() > 0)
	            {
	                if (req == null)
	                {
	                    headerValue += "; filename*=UTF-8''" + WebDAVHelper.encodeURL(attachFileName)
	                            + "; filename=\"" + attachFileName + "\"";
	                }
	                else
	                {
	                    String userAgent = req.getHeader(HEADER_USER_AGENT);
	                    boolean isLegacy = (null != userAgent) && (userAgent.contains("MSIE 8") || userAgent.contains("MSIE 7"));
	                    if (isLegacy)
	                    {
	                        headerValue += "; filename=\"" + WebDAVHelper.encodeURL(attachFileName)+"\"";
	                    }
	                    else
	                    {
	                        headerValue += "; filename=\"" + attachFileName + "\"; filename*=UTF-8''"
	                                + WebDAVHelper.encodeURL(attachFileName);
	                    }
	                }
	            }
	            
	            // set header based on filename - will force a Save As from the browse if it doesn't recognize it
	            // this is better than the default response of the browser trying to display the contents
	            res.setHeader("Content-Disposition", headerValue);
	        }
	    
}
