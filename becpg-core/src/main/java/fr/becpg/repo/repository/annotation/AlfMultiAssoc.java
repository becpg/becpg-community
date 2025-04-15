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
package fr.becpg.repo.repository.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>AlfMultiAssoc class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AlfMultiAssoc {
	/**
	 * Indicates whether this association is cacheable.
	 * 
	 * @return true if the association is cacheable, false otherwise
	 */
	boolean isCacheable() default false;

	/**
	 * Indicates whether this association is a child association.
	 * 
	 * @return true if the association is a child association, false otherwise
	 */
	boolean isChildAssoc() default false;

	/**
	 * Indicates whether this association represents an entity.
	 * 
	 * @return true if the association represents an entity, false otherwise
	 */
	boolean isEntity() default false;
}
