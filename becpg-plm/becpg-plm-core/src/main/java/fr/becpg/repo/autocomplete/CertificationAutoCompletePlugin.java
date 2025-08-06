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
package fr.becpg.repo.autocomplete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.api.BeCPGPublicApi;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.autocomplete.impl.extractors.NodeRefAutoCompleteExtractor;
import fr.becpg.repo.autocomplete.impl.plugins.TargetAssocAutoCompletePlugin;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>CertificationAutoCompletePlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 *
 *  Autocomplete plugin that provide certification
 *
 * Example:
 * <pre>
 * {@code
 * <control template="/org/alfresco/components/form/controls/autocomplete-association.ftl">
 *	  <control-param name="ds">becpg/autocomplete/certification
 * </control-param>
 * }
 * </pre>
 *
 *  Datasources:
 *
 *  ds: becpg/autocomplete/certification
 *  param: {none} return certification list of all suppliers associated to product or if entity is not a product all the certifications in certification list.
 */
@Service("certificationAutoCompletePlugin")
@BeCPGPublicApi
public class CertificationAutoCompletePlugin extends TargetAssocAutoCompletePlugin {

	private static final String SOURCE_TYPE_CERTIFICATION = "certification";

	private static Log logger = LogFactory.getLog(CertificationAutoCompletePlugin.class);

	@Autowired
	private EntityListDAO entityListDAO;

	/**
	 * {@inheritDoc}
	 *
	 * <p>getHandleSourceTypes.</p>
	 */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_CERTIFICATION };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		NodeRef entityNodeRef = null;

		if (((String) props.get(AutoCompleteService.PROP_ENTITYNODEREF) != null)

				&& NodeRef.isNodeRef((String) props.get(AutoCompleteService.PROP_ENTITYNODEREF))) {
			entityNodeRef = new NodeRef((String) props.get(AutoCompleteService.PROP_ENTITYNODEREF));
		}
	

		if (entityNodeRef != null) {
		
			
			if(entityDictionaryService.isSubClass(nodeService.getType(entityNodeRef), PLMModel.TYPE_PRODUCT)) {
				
				List<NodeRef> ret = new ArrayList<>();
				
				for(NodeRef plant : associationService.getTargetAssocs(entityNodeRef, PLMModel.ASSOC_PLANTS)) {
					
					ret.addAll(associationService.getTargetAssocs(plant, PLMModel.ASSOC_PLANT_CERTIFICATIONS));
				}
				
				for(NodeRef plant : associationService.getTargetAssocs(entityNodeRef, PLMModel.ASSOC_SUPPLIER_PLANTS)) {
					
					ret.addAll(associationService.getTargetAssocs(plant, PLMModel.ASSOC_PLANT_CERTIFICATIONS));
				}
				
				for(NodeRef plant : associationService.getTargetAssocs(entityNodeRef, PLMModel.ASSOC_SUBSIDIARY)) {
					
					ret.addAll(associationService.getTargetAssocs(plant, PLMModel.ASSOC_SUBSIDIARY_CERTIFICATIONS));
				}
				
				return new AutoCompletePage(ret, pageNum, pageSize,
						new NodeRefAutoCompleteExtractor(org.alfresco.model.ContentModel.PROP_NAME, nodeService));
			} else {
				
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
						Boolean includeDeleted = props != null && props.containsKey(AutoCompleteService.PROP_INCLUDE_DELETED) && (Boolean) props.get(AutoCompleteService.PROP_INCLUDE_DELETED);
						if (!includeDeleted.booleanValue()) {
							beCPGQueryBuilder.excludeProp(BeCPGModel.PROP_IS_DELETED, "true");
						}
						List<NodeRef> ret = beCPGQueryBuilder.maxResults(RepoConsts.MAX_SUGGESTIONS).list();
						return new AutoCompletePage(ret, pageNum, pageSize,
								new NodeRefAutoCompleteExtractor(org.alfresco.model.ContentModel.PROP_NAME, nodeService));
	
					} else {
						logger.warn("No datalists found for type: " + (listName != null ? listName : PLMModel.TYPE_CERTIFICATION));
					}
				}
			}
		}
		return new AutoCompletePage(new ArrayList<>(), pageNum, pageSize, null);
	}

}
