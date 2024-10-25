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
package fr.becpg.repo.entity.datalist;

import java.util.Date;
import java.util.List;

import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.helper.impl.AttributeExtractorField;

/**
 * Used to extract datalist datas
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface DataListExtractor {

	/**
	 * <p>extract.</p>
	 *
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object.
	 * @param metadataFields a {@link java.util.List} object.
	 * @return a {@link fr.becpg.repo.entity.datalist.PaginatedExtractedItems} object.
	 */
	PaginatedExtractedItems extract(DataListFilter dataListFilter, List<AttributeExtractorField> metadataFields);

	/**
	 * <p>applyTo.</p>
	 *
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object.
	 * @return a boolean.
	 */
	boolean applyTo(DataListFilter dataListFilter);

	/**
	 * <p>isDefaultExtractor.</p>
	 *
	 * @return a boolean.
	 */
	boolean isDefaultExtractor();

	/**
	 * <p>computeLastModified.</p>
	 *
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object.
	 * @return a {@link java.util.Date} object.
	 */
	Date computeLastModified(DataListFilter dataListFilter);

	/**
	 * <p>hasWriteAccess.</p>
	 *
	 * @return a boolean.
	 */
	boolean hasWriteAccess();
	
	/**
	 * <p>Defines the extractor priority. Extractor with the highest priority is chosen first. Default priority is 0.</p>
	 *
	 * @return an integer.
	 */
	int getPriority();

}
