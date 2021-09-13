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
package fr.becpg.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <p>ApplicationContextHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ApplicationContextHelper implements ApplicationContextAware {
		
	private static ApplicationContext ctx = null;

	/**
	 * <p>getApplicationContext.</p>
	 *
	 * @return a {@link org.springframework.context.ApplicationContext} object.
	 */
	public static ApplicationContext getApplicationContext() {
		return ctx;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("static-access")
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.ctx = ctx;
	}
}
