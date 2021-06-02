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
package fr.becpg.repo.repository.model;

import java.io.Serializable;



/**
 * Base class for Model objects.  Child objects should implement toString(),
 * equals() and hashCode();
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class BaseObject implements Serializable {    

	/**
	 * 
	 */
	private static final long serialVersionUID = 1281409754709493657L;
	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public abstract String toString();
    /** {@inheritDoc} */
    public abstract boolean equals(Object o);
    /**
     * <p>hashCode.</p>
     *
     * @return a int.
     */
    public abstract int hashCode();
}
