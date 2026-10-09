/*
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
 */
package fr.becpg.repo.olap;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.MLTextHelper;

/**
 * Imports the OLAP queries, dashboards and applications beCPG ships as classpath resources.
 *
 * <p>Each resource is named by its technical id ({@code portfolio-volumes-and-margins.saiku}), which
 * becomes {@code bcpg:olapQueryId} and is the only identity anything downstream relies on:
 * dashboards reference it, the Share dashlet stores it as a user preference, and the Saiku
 * repository resolves its paths on it. What the user reads lives apart, in the
 * {@code beCPG/olap/olapQueries} bundles: the repository locale supplies {@code cm:name}, and every
 * translation found supplies one value of the {@code cm:title} MLText.
 *
 * <p>Nothing here is keyed on a label, so a query can be renamed, translated, or shipped in a new
 * language without breaking a reference.
 *
 * @author matthieu
 */
public class OlapResourceImporter {

	private static final Log logger = LogFactory.getLog(OlapResourceImporter.class);

	/** Bundle holding the display label of every shipped resource, keyed by technical id. */
	private static final String LABEL_BUNDLE = "beCPG.olap.olapQueries";

	private static final String LABEL_KEY_PREFIX = "olap.";

	private NodeService nodeService;

	private ContentService contentService;

	private MimetypeService mimetypeService;

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>Setter for the field <code>mimetypeService</code>.</p>
	 *
	 * @param mimetypeService a {@link org.alfresco.service.cmr.repository.MimetypeService} object
	 */
	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	/**
	 * Imports every classpath resource matching the pattern into the folder.
	 *
	 * <p>Existing nodes are matched on {@code bcpg:olapQueryId}, never on their name, so the import
	 * stays correct after a user or an administrator has renamed a query.
	 *
	 * @param folderNodeRef the folder receiving the resources
	 * @param pattern an Ant-style classpath pattern, such as {@code classpath*:beCPG/olap/fr/*.saiku}
	 */
	public void importResources(NodeRef folderNodeRef, String pattern) {
		Map<String, NodeRef> existing = indexByOlapQueryId(folderNodeRef);

		try {
			for (Resource resource : new PathMatchingResourcePatternResolver().getResources(pattern)) {
				importResource(folderNodeRef, resource, existing);
			}
		} catch (IOException e) {
			logger.error("Cannot import OLAP resources matching " + pattern, e);
		}
	}

	private void importResource(NodeRef folderNodeRef, Resource resource, Map<String, NodeRef> existing) throws IOException {
		String fileName = resource.getFilename();
		if (fileName == null) {
			return;
		}

		String olapQueryId = stripExtension(fileName);
		NodeRef nodeRef = existing.get(olapQueryId);
		if (nodeRef == null) {
			nodeRef = createNode(folderNodeRef, olapQueryId, fileName);
			existing.put(olapQueryId, nodeRef);
		}

		writeContent(nodeRef, resource, fileName);
	}

	private NodeRef createNode(NodeRef folderNodeRef, String olapQueryId, String fileName) {
		String extension = fileName.substring(olapQueryId.length());

		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, label(olapQueryId, Locale.getDefault()) + extension);
		properties.put(ContentModel.PROP_TITLE, titles(olapQueryId));
		properties.put(BeCPGModel.PROP_OLAP_QUERY_ID, olapQueryId);

		if (logger.isDebugEnabled()) {
			logger.debug("Creating OLAP resource " + olapQueryId + " as " + properties.get(ContentModel.PROP_NAME));
		}

		// The title is an MLText: without the ML-aware flag the interceptor keeps only the value of
		// the current locale, and every other language is lost on the way in.
		boolean isMLAware = MLPropertyInterceptor.setMLAware(true);
		try {
			NodeRef nodeRef = nodeService
					.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(olapQueryId)),
							ContentModel.TYPE_CONTENT, properties)
					.getChildRef();

			nodeService.addAspect(nodeRef, BeCPGModel.ASPECT_OLAP_QUERY, null);

			return nodeRef;
		} finally {
			MLPropertyInterceptor.setMLAware(isMLAware);
		}
	}

	private void writeContent(NodeRef nodeRef, Resource resource, String fileName) throws IOException {
		ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
		writer.setMimetype(mimetypeService.guessMimetype(fileName));

		try (InputStream in = resource.getInputStream()) {
			writer.putContent(in);
		}
	}

	/**
	 * Indexes the resources already present in the folder by their technical id.
	 *
	 * @param folderNodeRef the folder to read
	 * @return the nodes carrying {@code bcpg:olapQueryId}, keyed by that id
	 */
	private Map<String, NodeRef> indexByOlapQueryId(NodeRef folderNodeRef) {
		Map<String, NodeRef> ret = new HashMap<>();

		List<ChildAssociationRef> children = nodeService.getChildAssocs(folderNodeRef);
		for (ChildAssociationRef child : children) {
			String olapQueryId = (String) nodeService.getProperty(child.getChildRef(), BeCPGModel.PROP_OLAP_QUERY_ID);
			if (olapQueryId != null) {
				ret.put(olapQueryId, child.getChildRef());
			}
		}

		return ret;
	}

	/**
	 * Builds the multilingual title of a resource from every translation the bundle actually
	 * carries. A locale whose bundle is missing is skipped rather than filled with the fallback,
	 * which would leave the reader an English label announced as a translation.
	 *
	 * @param olapQueryId the technical id of the resource
	 * @return the title, one value per translated locale
	 */
	private MLText titles(String olapQueryId) {
		MLText ret = new MLText();

		for (String key : RepoConsts.SUPPORTED_UI_LOCALES.split(",")) {
			Locale locale = MLTextHelper.parseLocale(key);
			ResourceBundle bundle = bundleFor(locale);
			if ((bundle != null) && locale.getLanguage().equals(bundle.getLocale().getLanguage())) {
				ret.addValue(locale, label(bundle, olapQueryId));
			}
		}

		return ret;
	}

	private String label(String olapQueryId, Locale locale) {
		return label(bundleFor(locale), olapQueryId);
	}

	private String label(ResourceBundle bundle, String olapQueryId) {
		if (bundle == null) {
			return olapQueryId;
		}

		try {
			return bundle.getString(LABEL_KEY_PREFIX + olapQueryId);
		} catch (MissingResourceException e) {
			logger.warn("No label for OLAP resource " + olapQueryId + ", falling back to its technical id");
			return olapQueryId;
		}
	}

	/**
	 * Loads the label bundle of a locale, or null when no module on the classpath ships one.
	 *
	 * @param locale the locale to load
	 * @return the bundle, or null
	 */
	private static ResourceBundle bundleFor(Locale locale) {
		try {
			return ResourceBundle.getBundle(LABEL_BUNDLE, locale);
		} catch (MissingResourceException e) {
			return null;
		}
	}

	private static String stripExtension(String fileName) {
		int dot = fileName.lastIndexOf('.');
		return dot > 0 ? fileName.substring(0, dot) : fileName;
	}

}
