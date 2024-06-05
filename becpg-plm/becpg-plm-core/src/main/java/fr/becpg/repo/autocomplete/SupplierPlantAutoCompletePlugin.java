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

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.api.BeCPGPublicApi;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.autocomplete.impl.plugins.TargetAssocAutoCompletePlugin;
import fr.becpg.repo.entity.EntityListDAO;

/**
 * <p>SupplierPlantAutoCompletePlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 *
 * Autocomplete plugin that allows to get plants for supplier
 *
 * Example:
 * <pre>
 * {@code
 * 	 <control template="/org/alfresco/components/form/controls/autocomplete-association.ftl">
 *           <control-param name="ds">becpg/autocomplete/supplierPlants</control-param>
 *    </control>
 *  }
 * </pre>
 *
 * Datasources:
 *
 * ds: becpg/autocomplete/supplierPlants
 * param: {none} If current connected user is associated to supplier return all plants in the plant list of this supplier else return all plants
 */
@BeCPGPublicApi
@Service("supplierPlantAutoCompletePlugin")
public class SupplierPlantAutoCompletePlugin extends TargetAssocAutoCompletePlugin {

	private static final String SOURCE_TYPE_SUPPLIER_PLANTS = "supplierPlants";

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private PersonService personService;

	/**
	 * {@inheritDoc}
	 *
	 * <p>getHandleSourceTypes.</p>
	 */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_SUPPLIER_PLANTS };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		NodeRef connectedUser = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());

		List<NodeRef> suppliers = associationService.getSourcesAssocs(connectedUser, PLMModel.ASSOC_SUPPLIER_ACCOUNTS);

		if ((suppliers != null) && !suppliers.isEmpty()) {
			List<NodeRef> ret = new ArrayList<>();
			for (NodeRef supplierNodeRef : suppliers) {
				NodeRef entityNodeRef = supplierNodeRef;

				if (nodeService.getType(supplierNodeRef).equals(PLMModel.TYPE_CONTACTLIST)) {
					entityNodeRef = entityListDAO.getEntity(supplierNodeRef);
				}

				if ((entityNodeRef != null) && nodeService.exists(entityNodeRef)
						&& entityDictionaryService.isSubClass(nodeService.getType(entityNodeRef), BeCPGModel.TYPE_ENTITY_V2)) {
					NodeRef listsContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
					if (listsContainerNodeRef != null) {
						NodeRef datalistNodeRef = entityListDAO.getList(listsContainerNodeRef, PLMModel.TYPE_PLANT);
						if (datalistNodeRef != null) {
							ret.addAll(entityListDAO.getListItems(datalistNodeRef, PLMModel.TYPE_PLANT));
						}

					}
				}
			}

			return new AutoCompletePage(ret, pageNum, pageSize, targetAssocValueExtractor);
		}

		props.put(AutoCompleteService.PROP_CLASS_NAME, PLMModel.TYPE_PLANT.toPrefixString(namespaceService));

		return super.suggest(SOURCE_TYPE_TARGET_ASSOC, query, pageNum, pageSize, props);

	}

}
