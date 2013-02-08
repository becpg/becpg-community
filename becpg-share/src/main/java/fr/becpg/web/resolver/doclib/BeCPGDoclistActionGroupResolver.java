/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.becpg.web.resolver.doclib;

import org.alfresco.web.resolver.doclib.DoclistActionGroupResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Resolves which action group if to use in the document library's document
 * list.
 * 
 * @author matthieu
 */
public class BeCPGDoclistActionGroupResolver implements DoclistActionGroupResolver {

	private static Log logger = LogFactory.getLog(BeCPGDoclistActionGroupResolver.class);

	/**
	 * Will return the action group id matching action group configs in a, i.e.
	 * share-config-custom.xml file.
	 * 
	 * @param jsonObject
	 *            An item (i.e. document or folder) in the doclist.
	 * @param view
	 *            Name of the type of view in which the action will be
	 *            displayed. I.e. "details"
	 * @return The action group id to use for displaying actions
	 */
	public String resolve(JSONObject jsonObject, String view) {
		String actionGroupId;
		JSONObject node = (JSONObject) jsonObject.get("node");
		JSONArray nodeAspects = getNodeAspects(jsonObject);
		boolean isContainer = (Boolean) node.get("isContainer");

		if (nodeAspects != null && nodeAspects.contains("bcpg:entityListsAspect")) {
			actionGroupId = "entity-";
		} else {
		
			if (isContainer) {
				actionGroupId = "folder-";
			} else {
				actionGroupId = "document-";
			}
		}

		boolean isLink = (Boolean) node.get("isLink");
		if (isLink) {
			actionGroupId += "link-";
		}
		if (view.equals("details")) {
			actionGroupId += "details";
		} else {
			actionGroupId += "browse";
		}
		
		return actionGroupId;
	}

	/**
	 * Retrieve a JSONArray of aspects for a node
	 * 
	 * @param jsonObject
	 *            JSONObject containing a "node" object as returned from the
	 *            ApplicationScriptUtils class.
	 * @return JSONArray containing aspects on the node
	 */
	public final JSONArray getNodeAspects(JSONObject jsonObject) {
		JSONArray aspects = null;

		try {
			JSONObject node = (JSONObject) jsonObject.get("node");

			if (node != null) {
				aspects = (JSONArray) node.get("aspects");
			}
		} catch (Exception err) {
			logger.error(err, err);
		}

		return aspects;
	}

}
