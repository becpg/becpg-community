/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.hierarchy.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.hierarchy.HierarchicalEntity;
import fr.becpg.repo.hierarchy.HierarchyHelper;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Service that manages hierarchies
 * 
 * @author quere
 * 
 */
@Service("hierarchyService")
public class HierarchyServiceImpl implements HierarchyService {

	private static Log logger = LogFactory.getLog(HierarchyServiceImpl.class);

	private static final String SUFFIX_ALL = "*";

	@Autowired
	private NamespaceService namespaceService;
	@Autowired
	private NodeService nodeService;
	@Autowired
	private RepoService repoService;
	@Autowired
	private DictionaryService dictionaryService;
	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	@Autowired
	private Repository repositoryHelper;

	@Override
	public NodeRef getHierarchyByPath(String path, NodeRef parentNodeRef, String value) {

		NodeRef hierarchyNodeRef = getHierarchyByQuery(getLuceneQuery(path, parentNodeRef, BeCPGModel.PROP_CODE, value, false), value);

		if (hierarchyNodeRef == null) {
			hierarchyNodeRef = getLuceneQuery(path, parentNodeRef, BeCPGModel.PROP_LKV_VALUE, value, false).singleValue();
		}

		return hierarchyNodeRef;
	}

	@Override
	public List<NodeRef> getHierarchiesByPath(String path, NodeRef parentNodeRef, String value) {
		return getLuceneQuery(path, parentNodeRef, BeCPGModel.PROP_LKV_VALUE, value, false).list();
	}

	@Override
	public List<NodeRef> getAllHierarchiesByPath(String path, String value) {
		return getLuceneQuery(path, null, BeCPGModel.PROP_LKV_VALUE, value, true).list();
	}

	@Override
	public NodeRef createRootHierarchy(NodeRef dataListNodeRef, String hierachy1) {
		return createHierarchy(dataListNodeRef, null, hierachy1);
	}

	@Override
	public NodeRef createHierarchy(NodeRef dataListNodeRef, NodeRef parentHierachy, String hierachy) {

		logger.debug("createHierarchy, parent hierarchy : " + parentHierachy + " - hierarchy: " + hierachy);
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(BeCPGModel.PROP_LKV_VALUE, hierachy);
		if (parentHierachy != null) {
			properties.put(BeCPGModel.PROP_PARENT_LEVEL, parentHierachy);
		} else {
			properties.put(BeCPGModel.PROP_DEPTH_LEVEL, 1);
		}

		NodeRef entityNodeRef = nodeService.getChildByName(dataListNodeRef, ContentModel.ASSOC_CONTAINS, hierachy);

		if (entityNodeRef == null) {
			entityNodeRef = nodeService.createNode(dataListNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(hierachy)), BeCPGModel.TYPE_LINKED_VALUE,
					properties).getChildRef();
		}

		return entityNodeRef;
	}

	private NodeRef getHierarchyByQuery(BeCPGQueryBuilder queryBuilder, String value) {

		List<NodeRef> ret = queryBuilder.list();

		if (ret.size() == 1) {
			return ret.get(0);
		} else if (ret.size() > 1) {
			for (NodeRef n : ret) {
				if (value.equals(nodeService.getProperty(n, BeCPGModel.PROP_LKV_VALUE))) {
					return n;
				}
			}
		}

		return null;
	}

	private BeCPGQueryBuilder getLuceneQuery(String path, NodeRef parentNodeRef, QName property, String value, boolean all) {

		NodeRef listContainerNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(repositoryHelper.getCompanyHome(),
				BeCPGQueryBuilder.encodePath(path));

		BeCPGQueryBuilder ret = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_LINKED_VALUE).maxResults(RepoConsts.MAX_SUGGESTIONS)
				.parent(listContainerNodeRef);

		if (parentNodeRef != null) {
			ret.andPropEquals(BeCPGModel.PROP_PARENT_LEVEL, parentNodeRef.toString());
		} else if (!all) {
			ret.andPropEquals(BeCPGModel.PROP_DEPTH_LEVEL, "1");
		}

		// value == * -> return all
		if (!isAllQuery(value)) {
			ret.andPropEquals(property, value);
		}

		return ret;
	}

	/**
	 * Classify according to the hierarchy.
	 * 
	 * @param containerNodeRef
	 *            : documentLibrary of site
	 * @param entityNodeRef
	 *            : entity
	 */
	@Override
	public void classifyByHierarchy(final NodeRef containerNodeRef, final NodeRef entityNodeRef) {

		AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {

			@Override
			public Void doWork() throws Exception {

				RepositoryEntity entity = alfrescoRepository.findOne(entityNodeRef);

				if (entity instanceof HierarchicalEntity) {

					NodeRef hierarchyNodeRef = ((HierarchicalEntity) entity).getHierarchy2();
					if (hierarchyNodeRef == null) {
						hierarchyNodeRef = ((HierarchicalEntity) entity).getHierarchy1();
					}

					QName type = nodeService.getType(entityNodeRef);
					ClassDefinition classDef = dictionaryService.getClass(type);

					NodeRef destinationNodeRef = repoService.getOrCreateFolderByPath(containerNodeRef, type.getLocalName(),
							classDef.getTitle(dictionaryService));

					if (hierarchyNodeRef != null) {
						destinationNodeRef = getOrCreateHierachyFolder(hierarchyNodeRef, destinationNodeRef);
					} else {
						logger.warn("Cannot classify entity since it doesn't have a hierarchy.");
					}

					if (destinationNodeRef != null) {
						// classify
						repoService.moveNode(entityNodeRef, destinationNodeRef);
					} else {
						logger.warn("Failed to classify entity. entityNodeRef: " + entityNodeRef);
					}

				} else {
					logger.warn("Cannot classify entity since is not implemented HierarchicalEntity");
				}
				return null;
			}

		});

	}

	private NodeRef getOrCreateHierachyFolder(NodeRef hierarchyNodeRef, NodeRef parentNodeRef) {
		NodeRef destinationNodeRef = null;

		NodeRef parent = HierarchyHelper.getParentHierachy(hierarchyNodeRef, nodeService);
		if (parent != null) {
			parentNodeRef = getOrCreateHierachyFolder(parent, parentNodeRef);
		}
		String name = HierarchyHelper.getHierachyName(hierarchyNodeRef, nodeService);
		if (name != null) {
			destinationNodeRef = repoService.getOrCreateFolderByPath(parentNodeRef, name, name);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Cannot create folder for productHierarchy since hierarchyName is null. productHierarchy: " + hierarchyNodeRef);
			}
		}

		return destinationNodeRef;
	}

	protected boolean isAllQuery(String query) {
		return query != null && query.trim().equals(SUFFIX_ALL);
	}

}
