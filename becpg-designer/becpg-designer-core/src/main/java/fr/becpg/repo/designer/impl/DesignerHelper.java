/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
package fr.becpg.repo.designer.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.model.BeCPGModel;

/**
 * 
 * @author matthieu
 *
 */
public class DesignerHelper {

	public static void sort(List<ChildAssociationRef> assocs, final NodeService nodeService) {
		Collections.sort(assocs, new Comparator<ChildAssociationRef>() {

			@Override
			public int compare(ChildAssociationRef o1, ChildAssociationRef o2) {
				Integer sort1 = null;
				if (o1 != null && o1.getChildRef() != null) {
					sort1 = (Integer) nodeService.getProperty(o1.getChildRef(), BeCPGModel.PROP_SORT);
				}
				Integer sort2 = null;
				if (o2 != null && o2.getChildRef() != null) {
					sort2 = (Integer) nodeService.getProperty(o2.getChildRef(), BeCPGModel.PROP_SORT);
				}

				if (sort1 != null && sort2 != null) {
					return Integer.compare(sort1, sort2);
				} else if (sort1 != null && sort2 == null) {
					return 1;
				} else if (sort2 != null && sort1 == null) {
					return -1;
				} else {
					return 0;
				}

			}

		});
	}
}
