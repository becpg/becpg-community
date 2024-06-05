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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

/**
 * <p>Abstract FormulationBaseHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class FormulationBaseHandler<T> implements FormulationHandler<T> {
 

	private static final Log logger = LogFactory.getLog(FormulationBaseHandler.class);
	
    private FormulationHandler<T> nextHandler;
 
    /** {@inheritDoc} */
    public void setNextHandler(FormulationHandler<T> next) {
        nextHandler = next;
    }
 
    /**
     * <p>start.</p>
     *
     * @param context a T object.
     * @throws fr.becpg.repo.formulation.FormulateException if any.
     */
    public void start(T context)  {
    	
    	StopWatch watch = null;
		if(logger.isDebugEnabled()){
		   watch = new StopWatch();
			watch.start();
		}
		
        boolean processed = process(context);
        
        if(logger.isDebugEnabled() && watch!=null ){
        	watch.stop();
        	logger.debug("Call handler : "+this.getClass().getName()+" takes " + watch.getTotalTimeSeconds() + " seconds");
        }
        
        if (processed && nextHandler != null){
            // Note that next handler's method is called through "start", not "process"
            nextHandler.start(context);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void onError(T repositoryEntity) {
    	if (nextHandler != null) {
    		nextHandler.onError(repositoryEntity);
    	}
    }
}
