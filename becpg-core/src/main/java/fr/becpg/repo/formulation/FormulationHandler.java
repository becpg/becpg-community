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
package fr.becpg.repo.formulation;

/**
 * A handler in the chain of responsibility scheme.
 *
 * T Any type passed as context information.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface FormulationHandler<T> {
 
    /**
     * Implements processing element logic in a chain.
     *
     * @param context Any type passed as context information.
     * @return <code>true</code>, if should call next handler
     * @throws fr.becpg.repo.formulation.FormulateException if any.
     */
    boolean process(T context);
 
    /**
     * Sets next handler for the current one; called in post-processing
     *
     * @param next a {@link fr.becpg.repo.formulation.FormulationHandler} object.
     */
    void setNextHandler(FormulationHandler<T> next);
 
    /**
     * Entry point to the handler chain
     *
     * @param context a T object.
     * @throws fr.becpg.repo.formulation.FormulateException
     */
    void start(T context);

	/**
	 * <p>onError.</p>
	 *
	 * @param repositoryEntity a T object
	 */
	void onError(T repositoryEntity);
}
