package fr.becpg.repo.web.scripts;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;

public abstract class AbstractCachingWebscript extends AbstractWebScript {


	private static Log logger = LogFactory.getLog(AbstractCachingWebscript.class);
	
	/**
	 * format definied by RFC 822, see
	 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3
	 */
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);
	
	
	protected boolean shouldReturnNotModified(WebScriptRequest req, Date lastModified) {
		// May be null - if so treat as just changed 
				if(lastModified == null)
		        {
					lastModified = new Date();
		        }

				long modifiedSince = -1;

				String modifiedSinceStr = req.getHeader("If-Modified-Since");
				if (modifiedSinceStr != null && lastModified != null) {
					try {
						modifiedSince = dateFormat.parse(modifiedSinceStr).getTime();
					} catch (Throwable e) {
						if (logger.isWarnEnabled()) {
							logger.warn("Browser sent badly-formatted If-Modified-Since header: " + modifiedSinceStr);
						}
					}

					if (modifiedSince > 0L) {
						// round the date to the ignore millisecond value which is
						// not supplied by header
						long modDate = (lastModified.getTime() / 1000L) * 1000L;
						if (modDate <= modifiedSince) {
							return true;
						}
					}
				}
				
				return false;
	}
	
	
}
