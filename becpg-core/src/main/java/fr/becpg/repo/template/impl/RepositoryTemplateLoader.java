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
package fr.becpg.repo.template.impl;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import freemarker.cache.TemplateLoader;

/**
 * <p>Loads FreeMarker templates from a flat repository folder, so that a customer can override a
 * template shipped on the classpath by dropping a node of the same name in that folder.</p>
 *
 * <p>Lookup misses must stay silent: FreeMarker probes several localized names before falling back
 * to the base one, and the next loader of the chain is only tried when this one returns null.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class RepositoryTemplateLoader implements TemplateLoader {

	private static final Log logger = LogFactory.getLog(RepositoryTemplateLoader.class);

	private final NodeService nodeService;

	private final ContentService contentService;

	private final RepoService repoService;

	private final String folderPath;

	/**
	 * <p>Constructor for RepositoryTemplateLoader.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object
	 * @param repoService a {@link fr.becpg.repo.helper.RepoService} object
	 * @param folderPath a {@link java.lang.String} object, the folder holding the templates
	 */
	public RepositoryTemplateLoader(NodeService nodeService, ContentService contentService, RepoService repoService, String folderPath) {
		this.nodeService = nodeService;
		this.contentService = contentService;
		this.repoService = repoService;
		this.folderPath = folderPath;
	}

	/** {@inheritDoc} */
	@Override
	public Object findTemplateSource(String name) {
		return AuthenticationUtil.runAsSystem(() -> findTemplateNode(name));
	}

	private NodeRef findTemplateNode(String name) {
		try {
			NodeRef folderNodeRef = repoService.getFolderByPath(folderPath);
			if (folderNodeRef == null) {
				return null;
			}
			return BeCPGQueryBuilder.createQuery().selectNodeByPath(folderNodeRef, name);
		} catch (Exception e) {
			logger.debug("Cannot look up template '" + name + "' in " + folderPath, e);
			return null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public long getLastModified(Object templateSource) {
		return AuthenticationUtil.runAsSystem(() -> {
			Date modified = (Date) nodeService.getProperty((NodeRef) templateSource, ContentModel.PROP_MODIFIED);
			return modified != null ? modified.getTime() : 0L;
		});
	}

	/** {@inheritDoc} */
	@Override
	public Reader getReader(Object templateSource, String encoding) {
		return AuthenticationUtil.runAsSystem(() -> {
			ContentReader reader = contentService.getReader((NodeRef) templateSource, ContentModel.PROP_CONTENT);
			return new InputStreamReader(reader.getContentInputStream(), encoding);
		});
	}

	/** {@inheritDoc} */
	@Override
	public void closeTemplateSource(Object templateSource) {
		// The reader returned by getReader owns the stream and is closed by FreeMarker
	}

}
