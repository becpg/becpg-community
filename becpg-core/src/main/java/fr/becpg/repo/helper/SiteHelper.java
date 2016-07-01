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
package fr.becpg.repo.helper;

public class SiteHelper {

	public static final String SITES_SPACE_QNAME_PATH = "/app:company_home/st:sites/";

	public static String extractContainerId(String path) {

		String containerId = null;
		if (isSitePath(path)) {
			String tmp = path.substring(SITES_SPACE_QNAME_PATH.length());
			int pos = tmp.indexOf('/');
			if (pos >= 1) {
				tmp = tmp.substring(pos + 1);
				pos = tmp.indexOf('/');
				if (pos >= 1) {
					// strip container id from the path
					containerId = tmp.substring(0, pos);
					containerId = containerId.substring(containerId.indexOf(":") + 1);
				}
			}
		}

		return containerId;
	}

	public static boolean isSitePath(String path) {
		boolean isSitePath = false;

		if (path.startsWith(SITES_SPACE_QNAME_PATH)) {
			isSitePath = true;
		}

		return isSitePath;
	}

	public static String extractSiteId(String path) {
		String siteId = null;
		if (isSitePath(path)) {

			String tmp = path.substring(SITES_SPACE_QNAME_PATH.length());
			int pos = tmp.indexOf(':');
			if (pos > 0) {
				siteId = tmp.split("/")[0];
				siteId = siteId.split(":")[1];
			}
		}
		return siteId;

	}

	public static String extractDisplayPath(String path, String displayPath) {
		String ret = "";

		if (isSitePath(path)) {
			String[] splitted = displayPath.split("/");

			for (int i = Math.min(5, splitted.length); i < splitted.length; i++) {
				if (ret.length() > 0) {
					ret += "/";
				}
				ret += splitted[i];
			}

		} else {
			ret = displayPath;
		}

		return ret;
	}

}
