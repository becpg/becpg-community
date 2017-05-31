/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;

@Service
public class SupplierPlantValuePlugin extends EntityListValuePlugin {

	private static final String SOURCE_TYPE_SUPPLIER_PLANTS = "supplierPlants";
	
	@Autowired
	private EntityListDAO entityListDAO;
	
	@Autowired
	private AssociationService associationService;
	
	@Autowired 
	private PersonService personService;

	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_SUPPLIER_PLANTS};
	}

	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize,
			Map<String, Serializable> props) {
		
		NodeRef connectedUser = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
		
		List<NodeRef> suppliers = associationService.getSourcesAssocs(connectedUser, PLMModel.ASSOC_SUPPLIER_ACCOUNT);
		if(suppliers !=null && !suppliers.isEmpty()){
			List<NodeRef> ret = new ArrayList<NodeRef>();
			for(NodeRef supplierNodeRef: suppliers){
				NodeRef listsContainerNodeRef = entityListDAO.getListContainer(supplierNodeRef);
				if (listsContainerNodeRef != null) {
					NodeRef datalistNodeRef =  entityListDAO.getList(listsContainerNodeRef, PLMModel.TYPE_PLANT);
					if(datalistNodeRef!=null){
						ret.addAll(entityListDAO.getListItems(datalistNodeRef, PLMModel.TYPE_PLANT));
					}
					
				}	
			}
			
			return new ListValuePage(ret, pageNum, pageSize, targetAssocValueExtractor);
		}
		
		props.put(ListValueService.PROP_CLASS_NAME,PLMModel.TYPE_PLANT.toPrefixString(namespaceService));
		
		return super.suggest(SOURCE_TYPE_TARGET_ASSOC, query, pageNum, pageSize, props);
	}


}
