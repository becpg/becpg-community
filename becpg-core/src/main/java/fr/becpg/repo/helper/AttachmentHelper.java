package fr.becpg.repo.helper;

import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * <p>AttachmentHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class AttachmentHelper {

	  private static final String HEADER_USER_AGENT     = "User-Agent";
	 
	  private AttachmentHelper() {
		  
	  }
	  
	  /**
	   * <p>setAttachment.</p>
	   *
	   * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object.
	   * @param res a {@link org.springframework.extensions.webscripts.WebScriptResponse} object.
	   * @param attachFileName a {@link java.lang.String} object.
	   */
	  public  static void setAttachment(WebScriptRequest req, WebScriptResponse res, String attachFileName)
	    {
	            String headerValue = "attachment";
	            if (attachFileName != null && attachFileName.length() > 0)
	            {
	            	 if (req == null)
		                {
		                    headerValue += "; filename*=UTF-8''" + URLEncoder.encode(attachFileName)
		                            + "; filename=\"" + filterNameForQuotedString(attachFileName) + "\"";
		                }
		                else
		                {
		                    String userAgent = req.getHeader(HEADER_USER_AGENT);
		                    boolean isLegacy = (null != userAgent) && (userAgent.contains("MSIE 8") || userAgent.contains("MSIE 7"));
		                    if (isLegacy)
		                    {
		                        headerValue += "; filename=\"" + URLEncoder.encode(attachFileName);
		                    }
		                    else
		                    {
		                        headerValue += "; filename=\"" + filterNameForQuotedString(attachFileName) + "\"; filename*=UTF-8''"
		                                + URLEncoder.encode(attachFileName);
		                    }
		                }
	            }
	            
	            // set header based on filename - will force a Save As from the browse if it doesn't recognize it
	            // this is better than the default response of the browser trying to display the contents
	            res.setHeader("Content-Disposition", headerValue);
	        }
	  
	  
	    
	    private static String filterNameForQuotedString(String s)
	    {
	        StringBuilder sb = new StringBuilder();
	        for(int i = 0; i < s.length(); i++)
	        {
	            char c = s.charAt(i);
	            if(isValidQuotedStringHeaderParamChar(c))
	            {
	                sb.append(c);
	            }
	            else
	            {
	                sb.append(" ");
	            }
	        }
	        return sb.toString();
	    }
	    
	    private static boolean isValidQuotedStringHeaderParamChar(char c)
	    {
	        // see RFC2616 section 2.2: 
	        // qdtext         = <any TEXT except <">>
	        // TEXT           = <any OCTET except CTLs, but including LWS>
	        // CTL            = <any US-ASCII control character (octets 0 - 31) and DEL (127)>
	        // A CRLF is allowed in the definition of TEXT only as part of a header field continuation.
	        // Note: we dis-allow header field continuation
	        return     (c < 256)  // message header param fields must be ISO-8859-1. Lower 256 codepoints of Unicode represent ISO-8859-1
	                && (c != 127) // CTL - see RFC2616 section 2.2
	                && (c != '"') // <">
	                && (c > 31);  // CTL - see RFC2616 section 2.2
	    }
	    
}
