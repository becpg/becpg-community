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

import javax.annotation.Nonnull;

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
	@Nonnull
	public String extractPropName(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		String firstName = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_CONTACT_LIST_FIRST_NAME);
		String lastName = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_CONTACT_LIST_LAST_NAME);

		StringBuilder nameBuilder = new StringBuilder();
		if (firstName != null && !firstName.isEmpty()) {
			nameBuilder.append(firstName);
		}

		if (lastName != null && !lastName.isEmpty()) {
			if (nameBuilder.length() > 0) {
				nameBuilder.append(" ");
			}
			nameBuilder.append(lastName);
		}

		return nameBuilder.toString();
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractMetadata(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		String[] parts = type.toPrefixString(namespaceService).split(":");
		return parts.length > 1 ? parts[1] : "";
	}

}
