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
package fr.becpg.repo.entity.datalist.data;

import java.util.List;

import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

import fr.becpg.repo.RepoConsts;

/**
 * DataList Pagination container
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DataListPagination {

	private int maxResults;

	private int pageSize;

	private int fullListSize;

	private int page = 1;
	
	private String queryExecutionId = null;

	/**
	 * <p>Getter for the field <code>maxResults</code>.</p>
	 *
	 * @return a int.
	 */
	public int getMaxResults() {
		return maxResults;
	}

	/**
	 * <p>Setter for the field <code>maxResults</code>.</p>
	 *
	 * @param maxResults a {@link java.lang.Integer} object.
	 */
	public void setMaxResults(Integer maxResults) {
		if (maxResults == null) {
			maxResults = -1;
		}
		this.maxResults = maxResults;
	}

	/**
	 * <p>Getter for the field <code>pageSize</code>.</p>
	 *
	 * @return a int.
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * <p>Setter for the field <code>pageSize</code>.</p>
	 *
	 * @param pageSize a {@link java.lang.Integer} object.
	 */
	public void setPageSize(Integer pageSize) {
		if (pageSize == null) {
			pageSize = RepoConsts.DATA_LISTS_PAGESIZE;
		}
		this.pageSize = pageSize;
	}

	/**
	 * <p>Getter for the field <code>fullListSize</code>.</p>
	 *
	 * @return a int.
	 */
	public int getFullListSize() {
		return fullListSize;
	}

	/**
	 * <p>Setter for the field <code>fullListSize</code>.</p>
	 *
	 * @param fullListSize a int.
	 */
	public void setFullListSize(int fullListSize) {
		this.fullListSize = fullListSize;
	}

	/**
	 * <p>Getter for the field <code>page</code>.</p>
	 *
	 * @return a int.
	 */
	public int getPage() {
		return page;
	}

	/**
	 * <p>Setter for the field <code>page</code>.</p>
	 *
	 * @param page a {@link java.lang.Integer} object.
	 */
	public void setPage(Integer page) {
		if (page == null) {
			page = 1;
		}
		this.page = page;
	}


	/**
	 * <p>Getter for the field <code>queryExecutionId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getQueryExecutionId() {
		return queryExecutionId;
	}

	/**
	 * <p>Setter for the field <code>queryExecutionId</code>.</p>
	 *
	 * @param queryExecutionId a {@link java.lang.String} object.
	 */
	public void setQueryExecutionId(String queryExecutionId) {
		this.queryExecutionId = queryExecutionId;
	}
	
	/**
	 * <p>paginate.</p>
	 *
	 * @param list a {@link java.util.List} object.
	 * @return a {@link java.util.List} object.
	 */
	public <T> List<T> paginate(List<T> list) {
		if (list != null) {
			fullListSize = list.size();

			// Pagination
			if (fullListSize > 0 && (fullListSize >= Math.min(getPage() * getPageSize(), fullListSize))) {
				list = list.subList(Math.max((getPage() - 1) * getPageSize(), 0), Math.min(getPage() * getPageSize(), fullListSize));
			}
		}
		return list;
	}

	/**
	 * <p>paginate.</p>
	 *
	 * @param pageOfNodeInfos a {@link org.alfresco.query.PagingResults} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<NodeRef> paginate(PagingResults<NodeRef> pageOfNodeInfos) {
   		    List<NodeRef> ret = pageOfNodeInfos.getPage();

	        Pair<Integer, Integer> totalResultCount = pageOfNodeInfos.getTotalResultCount();
	        if (totalResultCount != null)
	        {
	            fullListSize =  (totalResultCount.getSecond() != null ? totalResultCount.getSecond() : -1);
	        }
	        queryExecutionId = pageOfNodeInfos.getQueryExecutionId();
	        
		return ret;
	}
	

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "DataListPagination [maxResults=" + maxResults + ", pageSize=" + pageSize + ", fullListSize=" + fullListSize + ", page=" + page + "]";
	}

	



	
	
}
