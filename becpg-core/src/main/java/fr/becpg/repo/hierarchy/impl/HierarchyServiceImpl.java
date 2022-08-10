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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.hierarchy.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.hierarchy.HierarchicalEntity;
import fr.becpg.repo.hierarchy.HierarchyHelper;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.impl.AbstractBeCPGQueryBuilder;

/**
 * Service that manages hierarchies
 *
 * @author quere
 * @version $Id: $Id
 */
@Service("hierarchyService")
public class HierarchyServiceImpl implements HierarchyService {

	private static final Log logger = LogFactory.getLog(HierarchyServiceImpl.class);

	private static final String SUFFIX_ALL = "*";

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

	/** {@inheritDoc} */
	@Override
	public NodeRef getHierarchyByPath(String path, NodeRef parentNodeRef, String value) {

		NodeRef hierarchyNodeRef = getHierarchyByQuery(getLuceneQuery(path, parentNodeRef, BeCPGModel.PROP_CODE, value, false), value);

		if (hierarchyNodeRef == null) {
			hierarchyNodeRef = getLuceneQuery(path, parentNodeRef, BeCPGModel.PROP_LKV_VALUE, value, false).singleValue();
		}
		
		if (hierarchyNodeRef == null) {
			hierarchyNodeRef = getLuceneQuery(path, parentNodeRef, BeCPGModel.PROP_ERP_CODE, value, false).singleValue();
		}

		return hierarchyNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> getHierarchiesByPath(String path, NodeRef parentNodeRef, String value) {
		return getLuceneQuery(path, parentNodeRef, BeCPGModel.PROP_LKV_VALUE, value, false).andPropEquals(BeCPGModel.PROP_IS_DELETED, "false").list();
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> getAllHierarchiesByPath(String path, String value) {
		return getLuceneQuery(path, null, BeCPGModel.PROP_LKV_VALUE, value, true).list();
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createRootHierarchy(NodeRef dataListNodeRef, String hierachy1) {
		return createHierarchy(dataListNodeRef, null, hierachy1);
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createHierarchy(NodeRef dataListNodeRef, NodeRef parentHierachy, String hierachy) {

		logger.debug("createHierarchy, parent hierarchy : " + parentHierachy + " - hierarchy: " + hierachy);
		Map<QName, Serializable> properties = new HashMap<>();
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
				AbstractBeCPGQueryBuilder.encodePath(path));

		BeCPGQueryBuilder ret = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_LINKED_VALUE).parent(listContainerNodeRef)
				.maxResults(RepoConsts.MAX_SUGGESTIONS);

		if (parentNodeRef != null) {
			ret.andPropEquals(BeCPGModel.PROP_PARENT_LEVEL, parentNodeRef.toString());
			if (value.contains(SUFFIX_ALL)) {
				ret.addSort(BeCPGModel.PROP_LKV_VALUE,true);
			} else {
				ret.addSort(BeCPGModel.PROP_SORT,true);
			}
		} else if (!all) {
			ret.andPropEquals(BeCPGModel.PROP_DEPTH_LEVEL, "1");
			if (value.contains(SUFFIX_ALL)) {
				ret.addSort(BeCPGModel.PROP_LKV_VALUE,true);
			} else {
				ret.addSort(BeCPGModel.PROP_SORT,true);
			}
		} else {
			ret.addSort(BeCPGModel.PROP_SORT,true);
		}

		
		// value == * -> return all
		if (!isAllQuery(value)) {
			if (value.contains(SUFFIX_ALL)) {
				ret.andPropQuery(property, value).ftsLanguage();
			} else {
				ret.andPropEquals(property, value).inDB();
			}
		} else {
			ret.inDB();
		}

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public void classifyByHierarchy(NodeRef containerNodeRef, NodeRef entityNodeRef) {
		classifyByHierarchy(containerNodeRef, entityNodeRef, null, Locale.getDefault());

	}

	/**
	 * {@inheritDoc}
	 *
	 * Classify according to the hierarchy.
	 */
	@Override
	public boolean classifyByHierarchy(final NodeRef containerNodeRef, final NodeRef entityNodeRef, final QName hierarchyQname, Locale locale) {

		return AuthenticationUtil.runAsSystem(() -> {

			Locale currentLocal = I18NUtil.getLocale();
			Locale currentContentLocal = I18NUtil.getContentLocale();

			try {
				I18NUtil.setLocale(locale);
				I18NUtil.setContentLocale(null);
				
					QName type = nodeService.getType(entityNodeRef);
					ClassDefinition classDef = dictionaryService.getClass(type);
					
					NodeRef destinationNodeRef = repoService.getOrCreateFolderByPath(containerNodeRef, type.getLocalName(),
							classDef.getTitle(dictionaryService));

					destinationNodeRef = getOrCreateHierachyFolder(entityNodeRef, hierarchyQname, destinationNodeRef);

					if (destinationNodeRef != null) {
						if (destinationNodeRef != entityNodeRef) {
							// classify
							if (!ContentModel.TYPE_FOLDER.equals(nodeService.getType(destinationNodeRef))) {
								logger.warn("Incorrect destination node type:" + nodeService.getType(destinationNodeRef));
							} else {
								return repoService.moveNode(entityNodeRef, destinationNodeRef);
							}
						} else {
							logger.warn("Failed to classify entity. entityNodeRef: " + entityNodeRef + " cannot classify into itselfs");
						}
					} else {
						logger.warn("Failed to classify entity. entityNodeRef: " + entityNodeRef);
					}

				return false;

			} finally {
				I18NUtil.setLocale(currentLocal);
				I18NUtil.setContentLocale(currentContentLocal);
			}
		});

	}

	private NodeRef getHierarchyNodeRef(NodeRef entityNodeRef, QName hierarchyQname) {

		NodeRef hierarchyNodeRef = null;

		if (hierarchyQname != null) {
			hierarchyNodeRef = (NodeRef) nodeService.getProperty(entityNodeRef, hierarchyQname);
		}

		if (hierarchyNodeRef == null) {
			RepositoryEntity entity = alfrescoRepository.findOne(entityNodeRef);

			if (entity instanceof HierarchicalEntity) {

				hierarchyNodeRef = ((HierarchicalEntity) entity).getHierarchy2();
				if (hierarchyNodeRef == null) {
					hierarchyNodeRef = ((HierarchicalEntity) entity).getHierarchy1();
				}
			}
		}
		return hierarchyNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getOrCreateHierachyFolder(NodeRef entityNodeRef,  QName hierarchyQname , NodeRef destinationNodeRef) {
		
		NodeRef hierarchyNodeRef = getHierarchyNodeRef(entityNodeRef, hierarchyQname);
		
		if (hierarchyNodeRef != null) {
			destinationNodeRef = getOrCreateHierachyFolder(hierarchyNodeRef, destinationNodeRef);
		}

		return destinationNodeRef;
	}
	
	private NodeRef getOrCreateHierachyFolder(NodeRef hierarchyNodeRef, NodeRef parentNodeRef) {
		NodeRef destinationNodeRef = null;

		NodeRef parent = HierarchyHelper.getParentHierachy(hierarchyNodeRef, nodeService);
		if (parent != null) {
			if (!parent.equals(hierarchyNodeRef)) {
				parentNodeRef = getOrCreateHierachyFolder(parent, parentNodeRef);
			} else {
				logger.warn(
						"Warning hierarchy cycle for :" + HierarchyHelper.getHierachyName(hierarchyNodeRef, nodeService) + " " + hierarchyNodeRef);
			}
		}
		String name = PropertiesHelper.cleanFolderName(HierarchyHelper.getHierachyName(hierarchyNodeRef, nodeService));
		if (name != null) {
			destinationNodeRef = repoService.getOrCreateFolderByPath(parentNodeRef, name, name);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Cannot create folder for productHierarchy since hierarchyName is null. productHierarchy: " + hierarchyNodeRef);
			}
		}

		return destinationNodeRef;
	}

	/**
	 * <p>isAllQuery.</p>
	 *
	 * @param query a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	protected boolean isAllQuery(String query) {
		return (query != null) && query.trim().equals(SUFFIX_ALL);
	}

}
