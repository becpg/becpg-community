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
package fr.becpg.web.extensibility;

import java.util.Map;

import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.extensibility.impl.DefaultSubComponentEvaluator;

/**
 * Test url parameters agains evaluator params
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CustomViewEvaluator extends DefaultSubComponentEvaluator
{
	
	
    /**
     * {@inheritDoc}
     *
     * Returns true all url parameters values match
     */
    @Override
    public boolean evaluate(RequestContext context, Map<String, String> params)
    {
    	if(context.getParameter("list")!=null && context.getParameter("list").indexOf("View-")==0){
    		context.getAttributes().put("customview", context.getParameter("list").split("-")[1]);  
    		return true; 
    	}
        return false;
    }
}
