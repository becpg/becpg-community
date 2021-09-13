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

import java.util.Collection;
import java.util.Collections;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.impl.AbstractExprNameExtractor;

/**
 * <p>ContactListNameExtractorPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ContactListNameExtractorPlugin extends AbstractExprNameExtractor {

	/** {@inheritDoc} */
	@Override
	public Collection<QName> getMatchingTypes() {
		return Collections.singletonList(PLMModel.TYPE_CONTACTLIST);
	}

	/** {@inheritDoc} */
	@Override
	public String extractPropName(QName type, NodeRef nodeRef) {
		String ret = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_CONTACT_LIST_FIRST_NAME);

		String lastName = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_CONTACT_LIST_LAST_NAME);
		if ((lastName != null) && !lastName.isEmpty() && (ret != null) && !ret.isEmpty()) {
			ret += " " + lastName;
		} else if ((lastName != null) && !lastName.isEmpty()) {
			return lastName;
		}

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public String extractMetadata(QName type, NodeRef nodeRef) {
		return type.toPrefixString(namespaceService).split(":")[1];
	}

}
