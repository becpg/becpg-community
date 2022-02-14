/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;
import fr.becpg.repo.listvalue.impl.NodeRefListValueExtractor;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>CertificationValuePlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class CertificationValuePlugin extends EntityListValuePlugin {

	private static final String SOURCE_TYPE_CERTIFICATION = "certification";

	private static Log logger = LogFactory.getLog(CertificationValuePlugin.class);

	@Autowired
	private EntityListDAO entityListDAO;

	/**
	 * <p>getHandleSourceTypes.</p>
	 *
	 * @return an array of {@link java.lang.String} objects.
	 */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_CERTIFICATION };
	}

	/** {@inheritDoc} */
	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		NodeRef entityNodeRef = null;

		if (((String) props.get(ListValueService.PROP_ENTITYNODEREF) != null)

				&& NodeRef.isNodeRef((String) props.get(ListValueService.PROP_ENTITYNODEREF))) {
			entityNodeRef = new NodeRef((String) props.get(ListValueService.PROP_ENTITYNODEREF));
		}

		if (entityNodeRef != null) {
			String listName = null;

			if (BeCPGModel.TYPE_SYSTEM_ENTITY.equals(nodeService.getType(entityNodeRef))) {
				listName = PlmRepoConsts.PATH_CERTIFICATIONS;
			}

			NodeRef listsContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
			if (listsContainerNodeRef != null) {
				NodeRef dataListNodeRef;
				if (listName == null) {
					dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, PLMModel.TYPE_CERTIFICATION);
				} else {
					dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, listName);
				}
				if (dataListNodeRef != null) {

					BeCPGQueryBuilder beCPGQueryBuilder = BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_CERTIFICATION).parent(dataListNodeRef);
					beCPGQueryBuilder.andPropQuery(org.alfresco.model.ContentModel.PROP_NAME, prepareQuery(query));

					List<NodeRef> ret = beCPGQueryBuilder.maxResults(RepoConsts.MAX_SUGGESTIONS).list();
					return new ListValuePage(ret, pageNum, pageSize,
							new NodeRefListValueExtractor(org.alfresco.model.ContentModel.PROP_NAME, nodeService));

				} else {
					logger.warn("No datalists found for type: " + (listName != null ? listName : PLMModel.TYPE_CERTIFICATION));
				}
			}
		}
		return new ListValuePage(new ArrayList<>(), pageNum, pageSize, null);
	}

}
