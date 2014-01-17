package fr.becpg.repo.listvalue;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
 public class ListValuePage {
        
        private List<ListValueEntry> results;
        private Integer pageSize;
        private Integer page;
        private Integer fullListSize;
        
        
        @SuppressWarnings("unchecked")
		public <T> ListValuePage(List<T> fullList, Integer pageNum, Integer pageSize, ListValueExtractor<T> listValueExtractor) {
        	if(pageNum==null || pageNum <1){
        		pageNum = 1;
        	}
        	
            this.page = pageNum;
            this.pageSize = pageSize;
            this.fullListSize = fullList.size();
            if(!fullList.isEmpty()){
            	if(listValueExtractor == null){
            		results = (List<ListValueEntry>) fullList.subList(Math.max((page-1) * pageSize,0), Math.min(page * pageSize, fullListSize));
            	}else {
            		results = listValueExtractor.extract(fullList.subList(Math.max((page-1) * pageSize,0), Math.min(page * pageSize, fullListSize))); 
            	}
            } else {
            	results = new ArrayList<ListValueEntry>();
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

