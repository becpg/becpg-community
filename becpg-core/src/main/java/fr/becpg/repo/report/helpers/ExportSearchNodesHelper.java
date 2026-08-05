/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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
package fr.becpg.repo.report.helpers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * <p>Stores the nodes of an asynchronous search export on their download node, as a JSON array in
 * its <code>cm:content</code> property.</p>
 *
 * Attaching one association per exported node saturates the transactional caches and holds the
 * database locks of the download node for the whole request: a single content write does not, and
 * lets the client progress bar start without waiting. The content is later replaced by the produced
 * archive once the export completes.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public final class ExportSearchNodesHelper {

	private static final Log logger = LogFactory.getLog(ExportSearchNodesHelper.class);

	private ExportSearchNodesHelper() {
		// Helper class
	}

	/**
	 * <p>Store the nodes to export on the download node.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object
	 * @param downloadNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param nodeRefs a {@link java.util.List} object
	 */
	public static void storeNodes(ContentService contentService, NodeRef downloadNodeRef, List<NodeRef> nodeRefs) {

		JSONArray jsonArray = new JSONArray();

		for (NodeRef nodeRef : nodeRefs) {
			jsonArray.put(nodeRef.toString());
		}

		ContentWriter writer = contentService.getWriter(downloadNodeRef, ContentModel.PROP_CONTENT, true);
		writer.setMimetype(MimetypeMap.MIMETYPE_JSON);
		writer.setEncoding(StandardCharsets.UTF_8.name());
		writer.putContent(jsonArray.toString());
	}

	/**
	 * <p>Read back the nodes to export, or an empty array if the download node doesn't carry any:
	 * the caller then falls back on the associations of the download request.</p>
	 *
	 * <p>A node deleted between the search and the asynchronous export is left out: the exporter
	 * would fail on it and lose the whole archive, whereas it is simply out of scope.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param downloadNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return an array of {@link org.alfresco.service.cmr.repository.NodeRef} objects
	 */
	public static NodeRef[] readNodes(ContentService contentService, NodeService nodeService, NodeRef downloadNodeRef) {

		ContentReader reader = contentService.getReader(downloadNodeRef, ContentModel.PROP_CONTENT);

		if ((reader == null) || !reader.exists() || !MimetypeMap.MIMETYPE_JSON.equals(reader.getMimetype())) {
			return new NodeRef[0];
		}

		try {
			JSONArray jsonArray = new JSONArray(reader.getContentString());

			List<NodeRef> nodeRefs = new ArrayList<>(jsonArray.length());

			for (int i = 0; i < jsonArray.length(); i++) {
				NodeRef nodeRef = new NodeRef(jsonArray.getString(i));
				if (nodeService.exists(nodeRef)) {
					nodeRefs.add(nodeRef);
				}
			}

			return nodeRefs.toArray(new NodeRef[0]);
		} catch (JSONException e) {
			logger.error("Cannot read the nodes to export stored on download node: " + downloadNodeRef, e);
			return new NodeRef[0];
		}
	}

}
