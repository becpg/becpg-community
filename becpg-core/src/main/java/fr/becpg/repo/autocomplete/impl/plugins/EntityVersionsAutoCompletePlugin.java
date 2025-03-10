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
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.autocomplete.impl.plugins;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.AutoCompletePlugin;
import fr.becpg.repo.autocomplete.AutoCompleteService;
import fr.becpg.repo.autocomplete.impl.extractors.TargetAssocAutoCompleteExtractor;
import fr.becpg.repo.entity.version.EntityVersionService;

/**
 * <p>EntityVersionsListValuePlugin class.</p>
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 *
 * Autocomplete plugin that allows to get current entity branch or versions
 *
 * Example:
 *
 * <pre>
 * {@code
 * 	 <control template="/org/alfresco/components/form/controls/autocomplete-association.ftl">
 *       <control-param name="ds">becpg/autocomplete/branches</control-param>
 *       <control-param name="urlParamsToPass">itemId</control-param>
 *    </control>
 * }
 * </pre>
 *
 *  Datasources available:
 *
 * Return all survey questions by code or questionLabel, if parentAssoc is provided filter by parent question
 *
 *  becpg/autocomplete/branches return all entity branches
 *  becpg/autocomplete/versions return all entity versions
 */
@Service
public class EntityVersionsAutoCompletePlugin implements AutoCompletePlugin {

	private static final String SOURCE_TYPE_BRANCHES = "branches";
	private static final String SOURCE_TYPE_VERSIONS = "versions";

	@Autowired
	private VersionService versionService;
	
	@Autowired
	private EntityVersionService entityVersionService;

	@Autowired
	@Qualifier("NodeService")
	private NodeService nodeService;
	
	@Autowired
	private TargetAssocAutoCompleteExtractor targetAssocValueExtractor;

	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_BRANCHES, SOURCE_TYPE_VERSIONS };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		String strNodeRef = (String) props.get(AutoCompleteService.PROP_ENTITYNODEREF);
		if (strNodeRef == null) {
			strNodeRef = (String) props.get(AutoCompleteService.PROP_NODEREF);
		}

		NodeRef entityNodeRef = null;
		if (strNodeRef != null) {
			entityNodeRef = new NodeRef(strNodeRef);
		}

		List<NodeRef> branches = Collections.emptyList();
		
		if(entityNodeRef!=null) {
			
			branches = entityVersionService.getAllVersionBranches(entityNodeRef);
			
			if (SOURCE_TYPE_VERSIONS.equals(sourceType)) {
				
				VersionHistory versionHistory = versionService.getVersionHistory(entityNodeRef);
					
				if (versionHistory != null) {
					for (Version version : versionHistory.getAllVersions()) {
						branches.add(VersionUtil.convertNodeRef(version.getFrozenStateNodeRef()));
					}
				}
			}
	
			for (Iterator<NodeRef> iterator = branches.iterator(); iterator.hasNext();) {
				if (entityNodeRef.equals(iterator.next())) {
					iterator.remove();
				}
	
			}
		}

		return new AutoCompletePage(branches, pageNum, pageSize, targetAssocValueExtractor);

	}

}
