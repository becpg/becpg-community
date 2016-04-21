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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.formulation;

/**
 * A handler in the chain of responsibility scheme.
 * @param <T> Any type passed as context information.
 */
public interface FormulationHandler<T> {
 
    /**
     * Implements processing element logic in a chain.
     * @param context Any type passed as context information.
     * @return <code>true</code>, if should call next handler
     */
    boolean process(T context) throws FormulateException;
 
    /**
     * Sets next handler for the current one; called in post-processing
     */
    void setNextHandler(FormulationHandler<T> next);
 
    /**
     * Entry point to the handler chain
     * @throws FormulateException 
     */
    void start(T context) throws FormulateException;
}
