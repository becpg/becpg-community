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
	public String extractPropName(QName type, NodeRef nodeRef) {
		return extractExpr(nodeRef, productNameFormat());
	}

	/** {@inheritDoc} */
	@Override
	public String extractPropName(JSONObject jsonEntity) {
		return expressionService.extractExpr(jsonEntity, productNameFormat());
	}

	/** {@inheritDoc} */
	@Override
	public String extractMetadata(QName type, NodeRef nodeRef) {
		String typeCss =  type.toPrefixString(namespaceService).split(":")[1];
		
		String ret = typeCss+" "+typeCss + "-" + nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_STATE);
		if (nodeService.hasAspect(nodeRef, PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE)) {
			String nutClass = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_NUTRIENT_PROFILING_CLASS);
			if (nutClass != null && !nutClass.isEmpty() && nutClass.length() < 5) {
				ret += " nutrientClass-" + nutClass;
			}
		}

		NodeRef entityTplRef = associationService.getTargetAssoc(nodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF);
		if (entityTplRef != null) {
			ret += " "+typeCss + "-" + entityTplRef.getId();
		}

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public boolean matchCriteria(NodeRef nodeRef, Map<String, String> criteriaMap) {
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

	private List<NodeRef> extractNodeRefs(String propValue) {
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
