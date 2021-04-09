/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG. 
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

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * <p>FormulationService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface FormulationService<T extends FormulatedEntity>  {
	
	 /** Constant <code>DEFAULT_CHAIN_ID="default"</code> */
	 static final String DEFAULT_CHAIN_ID = "default";
	 /** Constant <code>FAST_FORMULATION_CHAINID="fastFormulationChain"</code> */
	 static final String FAST_FORMULATION_CHAINID = "fastFormulationChain";

     /**
      * <p>formulate.</p>
      *
      * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
      * @param chainId a {@link java.lang.String} object.
      * @return a T object.
      * @throws fr.becpg.repo.formulation.FormulateException if any.
      */
     T formulate(NodeRef entityNodeRef, String chainId);
    
     /**
      * <p>formulate.</p>
      *
      * @param repositoryEntity a T object.
      * @param chainId a {@link java.lang.String} object.
      * @return a T object.
      * @throws fr.becpg.repo.formulation.FormulateException if any.
      */
     T formulate(T repositoryEntity, String chainId);
	
     /**
      * <p>formulate.</p>
      *
      * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
      * @return a T object.
      * @throws fr.becpg.repo.formulation.FormulateException if any.
      */
     T formulate(NodeRef entityNodeRef);
    
     /**
      * <p>formulate.</p>
      *
      * @param repositoryEntity a T object.
      * @return a T object.
      * @throws fr.becpg.repo.formulation.FormulateException if any.
      */
     T formulate(T repositoryEntity);

	/**
	 * <p>registerFormulationChain.</p>
	 *
	 * @param clazz a {@link java.lang.Class} object.
	 * @param chain a {@link fr.becpg.repo.formulation.FormulationChain} object.
	 */
	void registerFormulationChain(Class<T> clazz, FormulationChain<T> chain);

	/**
	 * <p>shouldFormulate.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	boolean shouldFormulate(NodeRef entityNodeRef);
	
}
