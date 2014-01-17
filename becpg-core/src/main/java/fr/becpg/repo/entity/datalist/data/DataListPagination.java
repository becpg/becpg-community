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
package fr.becpg.repo.entity.datalist.data;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

import fr.becpg.repo.RepoConsts;

/**
 * DataList Pagination container
 * 
 * @author matthieu
 * 
 */
public class DataListPagination {

	private int maxResults;

	private int pageSize;

	private int fullListSize;

	private int page = 1;
	
	private String queryExecutionId = null;

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(Integer maxResults) {
		if (maxResults == null) {
			maxResults = -1;
		}
		this.maxResults = maxResults;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		if (pageSize == null) {
			pageSize = RepoConsts.DATA_LISTS_PAGESIZE;
		}
		this.pageSize = pageSize;
	}

	public int getFullListSize() {
		return fullListSize;
	}

	public void setFullListSize(int fullListSize) {
		this.fullListSize = fullListSize;
	}

	public int getPage() {
		return page;
	}

	public void setPage(Integer page) {
		if (page == null) {
			page = 1;
		}
		this.page = page;
	}


	public String getQueryExecutionId() {
		return queryExecutionId;
	}

	public void setQueryExecutionId(String queryExecutionId) {
		this.queryExecutionId = queryExecutionId;
	}
	
	public <T> List<T> paginate(List<T> list) {
		if (list != null) {
			fullListSize = list.size();

			// Pagination
			if (fullListSize > 0) {
				list = list.subList(Math.max((getPage() - 1) * getPageSize(), 0), Math.min(getPage() * getPageSize(), fullListSize));
			}
		}
		return list;
	}

	public List<NodeRef> paginate(PagingResults<FileInfo> pageOfNodeInfos) {
   		    List<FileInfo> nodeInfos = pageOfNodeInfos.getPage();
	        int size = nodeInfos.size();
	        List<NodeRef> ret = new LinkedList<NodeRef>();
	        for (int i=0; i<size; i++)
	        {
	            FileInfo nodeInfo = nodeInfos.get(i);
	            ret.add(nodeInfo.getNodeRef());
	        }
	        
	        Pair<Integer, Integer> totalResultCount = pageOfNodeInfos.getTotalResultCount();
	        if (totalResultCount != null)
	        {
	            fullListSize =  (totalResultCount.getSecond() != null ? totalResultCount.getSecond() : -1);
	        }
	        queryExecutionId = pageOfNodeInfos.getQueryExecutionId();
	        
		return ret;
	}
	

	@Override
	public String toString() {
		return "DataListPagination [maxResults=" + maxResults + ", pageSize=" + pageSize + ", fullListSize=" + fullListSize + ", page=" + page + "]";
	}

	



	
	
}
