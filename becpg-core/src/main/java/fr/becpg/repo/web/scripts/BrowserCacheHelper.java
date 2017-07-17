/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG.
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
package fr.becpg.repo.web.scripts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.ibm.icu.util.Calendar;

public abstract class BrowserCacheHelper   {

	private static final Log logger = LogFactory.getLog(BrowserCacheHelper.class);
	
	public static boolean isBrowserHasInCache(WebScriptRequest req){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -5);
		return shouldReturnNotModified(req, cal.getTime());
	}

	public static boolean shouldReturnNotModified(WebScriptRequest req, Date lastModified) {

		/**
		 * format definied by RFC 822, see
		 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3
		 */
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

		// May be null - if so treat as just changed
		if (lastModified == null) {
			lastModified = new Date();
		}

		long modifiedSince = -1;

		String modifiedSinceStr = req.getHeader("If-Modified-Since");
		if (modifiedSinceStr != null && lastModified != null) {
			try {
				modifiedSince = dateFormat.parse(modifiedSinceStr).getTime();
			} catch (ParseException e) {
				if (logger.isWarnEnabled()) {
					logger.warn("Browser sent badly-formatted If-Modified-Since header: " + modifiedSinceStr);
				}
			}

			if (modifiedSince > 0L) {
				// round the date to the ignore millisecond value which is
				// not supplied by header
				long modDate = (lastModified.getTime() / 1000L) * 1000L;
				if (modDate <= modifiedSince)
					return true;
			}
		}

		return false;
	}

}
