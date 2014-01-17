/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.web.extensibility;

import java.util.Map;

import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.extensibility.impl.DefaultSubComponentEvaluator;

/**
 * Test url parameters agains evaluator params
 * @author matthieu
 *
 */
public class UrlParamsEvaluator extends DefaultSubComponentEvaluator
{
	
	
    /**
     * Returns true all url parameters values match 
     *
     * @param context
     * @param params
     * @return true if all url parameters values match 
     */
    @Override
    public boolean evaluate(RequestContext context, Map<String, String> params)
    {
    	
    	
    	for(Map.Entry<String, String> param : params.entrySet() ){
    		if(!param.getValue().equals(context.getParameter(param.getKey()))){
    			return false;
    		}
    	}
        return true;
    }
}
