/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.node.getchildren;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * GetChildren CQ parameters - for query context and filtering
 *
 * @author janv
 * @since 4.0
 */
//beCPG No Changes
public class GetChildrenCannedQueryParams
{
    private NodeRef parentRef;
    
    private Set<QName> childTypeQNames = Collections.emptySet();
    private List<FilterProp> filterProps = Collections.emptyList();
    private Set<QName> assocTypeQNames = null;
    private String pattern = null;
    private Set<QName> inclusiveAspects = null;
    private Set<QName> exclusiveAspects = null;
    
    /**
     * <p>Constructor for GetChildrenCannedQueryParams.</p>
     *
     * @param parentRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @param assocTypeQNames a {@link java.util.Set} object
     * @param childTypeQNames a {@link java.util.Set} object
     * @param inclusiveAspects a {@link java.util.Set} object
     * @param exclusiveAspects a {@link java.util.Set} object
     * @param filterProps a {@link java.util.List} object
     * @param pattern a {@link java.lang.String} object
     */
    public GetChildrenCannedQueryParams(
            NodeRef parentRef,
            Set<QName> assocTypeQNames,
            Set<QName> childTypeQNames,
            Set<QName> inclusiveAspects,
            Set<QName> exclusiveAspects,
            List<FilterProp> filterProps,
            String pattern)
    {
        this.parentRef = parentRef;
        this.assocTypeQNames = assocTypeQNames;

        if (childTypeQNames != null) { this.childTypeQNames = childTypeQNames; }
        this.inclusiveAspects = inclusiveAspects;
        this.exclusiveAspects = exclusiveAspects;
        if (filterProps != null) { this.filterProps = filterProps; }
        if (pattern != null)
        {
        	this.pattern = pattern;
        } 
    }
    
    /**
     * <p>Getter for the field <code>parentRef</code>.</p>
     *
     * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
     */
    public NodeRef getParentRef()
    {
        return parentRef;
    }
    
    /**
     * <p>Getter for the field <code>childTypeQNames</code>.</p>
     *
     * @return a {@link java.util.Set} object
     */
    public Set<QName> getChildTypeQNames()
    {
        return childTypeQNames;
    }
    
    /**
     * <p>Getter for the field <code>assocTypeQNames</code>.</p>
     *
     * @return a {@link java.util.Set} object
     */
    public Set<QName> getAssocTypeQNames()
    {
		return assocTypeQNames;
	}

	/**
	 * <p>Getter for the field <code>filterProps</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<FilterProp>  getFilterProps()
    {
        return filterProps;
    }

	/**
	 * <p>Getter for the field <code>pattern</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getPattern()
	{
		return pattern;
	}
	
	/**
	 * <p>Getter for the field <code>inclusiveAspects</code>.</p>
	 *
	 * @return a {@link java.util.Set} object
	 */
	public Set<QName> getInclusiveAspects()
	{
	    return inclusiveAspects;
	}

	/**
	 * <p>Getter for the field <code>exclusiveAspects</code>.</p>
	 *
	 * @return a {@link java.util.Set} object
	 */
	public Set<QName> getExclusiveAspects()
    {
        return exclusiveAspects;
    }
}
