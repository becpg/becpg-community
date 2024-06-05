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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.autocomplete.impl.plugins.TargetAssocAutoCompletePlugin;

/**
 * <p>VariantListValuePlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 *
 *  Autocomplete plugin that provide available variants for current entity
 *
 * Example:
 * <pre>
 * {@code
 * <control template="/org/alfresco/components/form/controls/autocomplete.ftl">
 *		<control-param name="ds">becpg/autocomplete/variantList
 * </control-param>
 * }
 * </pre>
 *
 *  Datasources available:
 *
 *  becpg/autocomplete/variantList
 */
@Service("variantAutoCompletePlugin")
public class VariantAutoCompletePlugin extends TargetAssocAutoCompletePlugin {

	private static final Log logger = LogFactory.getLog(VariantAutoCompletePlugin.class);

	private static final String SOURCE_TYPE_VARIANT_LIST = "variantList";
	
	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_VARIANT_LIST };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {
		
		
		NodeRef entityNodeRef = null;
		String entityNodeRefStr = (String) props.get(AutoCompleteService.PROP_ENTITYNODEREF);
		if(entityNodeRefStr !=null && NodeRef.isNodeRef(entityNodeRefStr)) {
			entityNodeRef = new NodeRef(entityNodeRefStr);
		}
		
		@SuppressWarnings("unchecked")
		Map<String, String> extra =  (Map<String, String>) props.get(AutoCompleteService.EXTRA_PARAM);
		if((entityNodeRef == null || !nodeService.exists(entityNodeRef)) && extra != null && !extra.isEmpty()){
			NodeRef itemRef =  new NodeRef (extra.get("itemId"));
			entityNodeRef = getParentEntity(itemRef);
		}
		if(logger.isDebugEnabled()){
			logger.debug("VariantListValuePlugin sourceType: " + sourceType + " - entityNodeRef: " + entityNodeRef);
		}
		List<NodeRef> ret = new ArrayList<>(associationService.getChildAssocs(entityNodeRef, BeCPGModel.ASSOC_VARIANTS));
		NodeRef entityTplNodeRef = associationService.getTargetAssoc(entityNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF);
		if (entityTplNodeRef != null) {
			List<NodeRef> entityTplVariants = associationService.getChildAssocs(entityTplNodeRef, BeCPGModel.ASSOC_VARIANTS);
			if (entityTplVariants != null && !entityTplVariants.isEmpty()) {
				ret.addAll(entityTplVariants);
			}
		}
			
		return new AutoCompletePage(ret, pageNum, pageSize, new VariantListValueExtractor());

	}
	
	private NodeRef getParentEntity(NodeRef itemRef){
		
		if(dictionaryService.isSubClass(nodeService.getType(itemRef), PLMModel.TYPE_PRODUCT)) {
			return itemRef;
		}
	
		ChildAssociationRef childAssociationRef = nodeService.getPrimaryParent(itemRef);
		NodeRef parent = childAssociationRef.getParentRef();

		while (!dictionaryService.isSubClass(nodeService.getType(parent), PLMModel.TYPE_PRODUCT)){
			childAssociationRef = nodeService.getPrimaryParent(parent);
			parent = childAssociationRef.getParentRef();
		}
		
		return parent; 
	}
	
	public class VariantListValueExtractor implements AutoCompleteExtractor<NodeRef> {

		@Override
		public List<AutoCompleteEntry> extract(List<NodeRef> nodeRefs) {
			List<AutoCompleteEntry> suggestions = new ArrayList<>();
	    	if(nodeRefs!=null){
	    		for(NodeRef nodeRef : nodeRefs){
	    			String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	    			Boolean isDefault = (Boolean) nodeService.getProperty(nodeRef, BeCPGModel.PROP_IS_DEFAULT_VARIANT);		
	    			suggestions.add(new AutoCompleteEntry(nodeRef.toString(),name, Boolean.TRUE.equals(isDefault) ? "variant-default": "variant"));
	    			
	    		}
	    	}
			return suggestions;
		}
	}

}
