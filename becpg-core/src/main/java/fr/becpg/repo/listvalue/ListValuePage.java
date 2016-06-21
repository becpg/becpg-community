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
package fr.becpg.repo.listvalue;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
 public class ListValuePage {
        
        private final List<ListValueEntry> results;
        private final Integer pageSize;
        private final Integer page;
        private final Integer fullListSize;
        
        
        @SuppressWarnings("unchecked")
		public <T> ListValuePage(List<T> fullList, Integer pageNum, Integer pageSize, ListValueExtractor<T> listValueExtractor) {
        	if(pageNum==null || pageNum <1){
        		pageNum = 1;
        	}
        	
            this.page = pageNum;
            this.pageSize = pageSize;
            this.fullListSize = fullList.size();
            
            int fromIndex = Math.max((page-1) * pageSize,0);
            int toIndex = Math.min(page * pageSize, fullListSize);
            
            if(!fullList.isEmpty() && toIndex >= fromIndex){
            	if(listValueExtractor == null){
            		results = (List<ListValueEntry>) fullList.subList(fromIndex, toIndex);
            	}else {
            		results = listValueExtractor.extract(fullList.subList(fromIndex, toIndex)); 
            	}
            } else {
            	results = new ArrayList<>();
            }
        }
     

		public List<ListValueEntry> getResults() {
			return results;
		}

		public Integer getPageNumber() {
			return page;
		}

		public Integer getObjectsPerPage() {
			return pageSize;
		}

		public Integer getFullListSize() {
			return fullListSize;
		}

		
		
 
 }

