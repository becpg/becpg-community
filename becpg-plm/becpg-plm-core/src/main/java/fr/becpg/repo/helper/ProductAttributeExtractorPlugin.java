/*
Copyright (C) 2010-2021 beCPG. 
 
This file is part of beCPG 
 
beCPG is free software: you can redistribute it and/or modify 
it under the terms of the GNU Lesser General Public License as published by 
the Free Software Foundation, either version 3 of the License, or 
(at your option) any later version. 
 
beCPG is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 

MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
GNU Lesser General Public License for more details. 
 
You should have received a copy of the GNU Lesser General Public License 
along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.becpg.repo.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.WUsedListService.WUsedOperator;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.helper.impl.AbstractExprNameExtractor;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>ProductAttributeExtractorPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ProductAttributeExtractorPlugin extends AbstractExprNameExtractor {

	@Autowired
	private WUsedListService wUsedListService;

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	@Autowired
	private AssociationService associationService;

	private String productNameFormat() {
		return systemConfigurationService.confValue("beCPG.product.name.format");
	}

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	/** {@inheritDoc} */
	@Override
	public Collection<QName> getMatchingTypes() {
		return entityDictionaryService.getSubTypes(PLMModel.TYPE_PRODUCT);
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractPropName(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		String result = extractExpr(nodeRef, productNameFormat());
		return result != null ? result : "";
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractPropName(JSONObject jsonEntity) {
		String result = expressionService.extractExpr(jsonEntity, productNameFormat());
		return result != null ? result : "";
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractMetadata(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		StringBuilder ret = new StringBuilder();
		
		// Add type CSS
		String[] typeParts = type.toPrefixString(namespaceService).split(":");
		String typeCss = typeParts.length > 1 ? typeParts[1] : "";
		
		// Add base type and state
		Object productState = nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_STATE);
		ret.append(typeCss).append(" ").append(typeCss).append("-").append(productState != null ? productState.toString() : "");
		
		// Add nutrient class if applicable
		if (nodeService.hasAspect(nodeRef, PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE)) {
			String nutClass = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_NUTRIENT_PROFILING_CLASS);
			if (nutClass != null && !nutClass.isEmpty() && nutClass.length() < 5) {
				ret.append(" nutrientClass-").append(nutClass);
			}
		}

		// Add entity template reference if available
		NodeRef entityTplRef = associationService.getTargetAssoc(nodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF);
		if (entityTplRef != null) {
			ret.append(" ").append(typeCss).append("-").append(entityTplRef.getId());
		}

		return ret.toString();
	}

	/** {@inheritDoc} */
	@Override
	public boolean matchCriteria(@Nonnull NodeRef nodeRef, Map<String, String> criteriaMap) {
		return matchWUsedCriteria(nodeRef, criteriaMap, PLMModel.ASSOC_COMPOLIST_PRODUCT)
				|| matchWUsedCriteria(nodeRef, criteriaMap, PLMModel.ASSOC_PACKAGINGLIST_PRODUCT)
				|| matchWUsedCriteria(nodeRef, criteriaMap, MPMModel.ASSOC_PL_RESOURCE);
	}

	private boolean matchWUsedCriteria(NodeRef node, Map<String, String> criteriaMap, QName criteriaAssoc) {

		String assocString = criteriaAssoc.toPrefixString(namespaceService);

		if (criteriaMap != null && criteriaMap.containsKey(assocString)) {

			String propValue = criteriaMap.get(assocString);

			if ((propValue != null) && !propValue.isBlank()) {

				List<NodeRef> toFilterByNodes = extractNodeRefs(propValue);

				if (!toFilterByNodes.isEmpty()) {

					MultiLevelListData ret = wUsedListService.getWUsedEntity(toFilterByNodes, WUsedOperator.OR, criteriaAssoc, -1);
					if (ret != null) {
						return ret.getAllChilds().contains(node);
					}
				}
			}
		}

		return false;
	}

	@Nonnull
	private List<NodeRef> extractNodeRefs(@Nonnull String propValue) {
		String[] arrValues = propValue.split(RepoConsts.MULTI_VALUES_SEPARATOR);
		List<NodeRef> ret = new ArrayList<>();

		for (String strNodeRef : arrValues) {

			if (!strNodeRef.isBlank()) {

				NodeRef nodeRef = new NodeRef(strNodeRef);

				if (nodeService.exists(nodeRef)) {
					ret.add(nodeRef);
				}
			}

		}

		return ret;
	}

}
