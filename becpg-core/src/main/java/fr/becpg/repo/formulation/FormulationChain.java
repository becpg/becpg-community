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

import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Chain of responsibility executor.
 *
 *            Any type passed as context information.
 * @author matthieu
 * @version $Id: $Id
 */
public class FormulationChain<T extends FormulatedEntity> {
	private final Log logger = LogFactory.getLog(FormulationChain.class);

	private Class<T> contextClass;

	private FormulationService<T> formulationService;

	private List<FormulationHandler<T>> handlers;

	private String chainId;

	private boolean updateFormulatedDate = true;

	/**
	 * <p>Setter for the field <code>updateFormulatedDate</code>.</p>
	 *
	 * @param updateFormulatedDate a boolean.
	 */
	public void setUpdateFormulatedDate(boolean updateFormulatedDate) {
		this.updateFormulatedDate = updateFormulatedDate;
	}

	/**
	 * <p>Setter for the field <code>chainId</code>.</p>
	 *
	 * @param chainId a {@link java.lang.String} object.
	 */
	public void setChainId(String chainId) {
		this.chainId = chainId;
	}

	/**
	 * <p>Getter for the field <code>chainId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getChainId() {
		return chainId;
	}

	/**
	 * <p>Setter for the field <code>formulationService</code>.</p>
	 *
	 * @param formulationService a {@link fr.becpg.repo.formulation.FormulationService} object.
	 */
	public void setFormulationService(FormulationService<T> formulationService) {
		this.formulationService = formulationService;
	}

	/**
	 * <p>Setter for the field <code>contextClass</code>.</p>
	 *
	 * @param contextClass a {@link java.lang.Class} object.
	 */
	public void setContextClass(Class<T> contextClass) {
		this.contextClass = contextClass;
	}

	/**
	 * <p>init.</p>
	 */
	public void init() {
		if (handlers != null && !handlers.isEmpty()) {
			prepareHandlerChain();
			formulationService.registerFormulationChain(contextClass, this);
		}
	}

	/**
	 * <p>executeChain.</p>
	 *
	 * @param context a T object.
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 */
	public void executeChain(T context)  {
		if (handlers != null && !handlers.isEmpty()) {
			handlers.get(0).start(context);
		}
	}

	/**
	 * Sets handler list. Intended to be injected.
	 *
	 * @param handlers
	 *            Handler list.
	 */
	public void setHandlers(List<FormulationHandler<T>> handlers) {
		this.handlers = handlers;
	}


	/**
	 * <p>Getter for the field <code>handlers</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<FormulationHandler<T>> getHandlers() {
		return handlers;
	}

	private void prepareHandlerChain() {

		ListIterator<FormulationHandler<T>> handlersIt = handlers.listIterator();
		FormulationHandler<T> current = handlersIt.next();

		if (logger.isDebugEnabled()) {
			logger.debug("Prepare Handler Chain: ");
			logger.debug("  - First: " + current.getClass().getName());
		}

		while (handlersIt.hasNext()) {

			FormulationHandler<T> next = handlersIt.next();
			if (logger.isDebugEnabled()) {
				logger.debug("  - Next: " + next.getClass().getName());

			}

			current.setNextHandler(next);
			current = next;
		}
	}

	/**
	 * <p>shouldUpdateFormulatedDate.</p>
	 *
	 * @return a boolean.
	 */
	public boolean shouldUpdateFormulatedDate() {
		return updateFormulatedDate;
	}

	/**
	 * <p>onError.</p>
	 *
	 * @param repositoryEntity a T object
	 */
	public void onError(T repositoryEntity) {
		if (handlers != null && !handlers.isEmpty()) {
			handlers.get(0).onError(repositoryEntity);
		}
	}
}
