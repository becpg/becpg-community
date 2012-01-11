package fr.becpg.repo.listvalue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
 public class ListValuePage {
        
        private Map<String, String> results;
        private Integer pageSize;
        private Integer page;
        private Integer fullListSize;
        private Boolean isNodeRef;
        private String type = "unknow";
   
        public <T> ListValuePage(List<T> fullList, Integer pageNum, Integer pageSize, ListValueExtractor<T> listValueExtractor) {
        	if(pageNum==null || pageNum <1){
        		pageNum = 1;
        	}
        	
            this.page = pageNum;
            this.pageSize = pageSize;
            this.fullListSize = fullList.size();
            this.isNodeRef = true; //TODO test T
            if(fullList.size()>0){
            	results = listValueExtractor.extract(fullList.subList(Math.max((page-1) * pageSize,0), Math.min(page * pageSize, fullListSize))); 
            } else {
            	results = new HashMap<String, String>();
            }
        }
        
        public <T> ListValuePage(List<T> fullList, Integer pageNum, Integer pageSize, ListValueExtractor<T> listValueExtractor, String type) {
        	if(pageNum==null || pageNum <1){
        		pageNum = 1;
        	}
        	
            this.page = pageNum;
            this.pageSize = pageSize;
            this.fullListSize = fullList.size();
            this.isNodeRef = false;  //TODO test T
            this.type = type;
            
            if(fullList.size()>0){
                results = listValueExtractor.extract(fullList.subList(Math.max((page-1) * pageSize,0), Math.min(page * pageSize, fullListSize))); 
            } else {
            	results = new HashMap<String, String>();
            }
            
        }
       

		public Map<String, String> getResults() {
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

		/**
		 * @return the isNodeRef
		 */
		public Boolean getIsNodeRef() {
			return isNodeRef;
		}

		/**
		 * @param isNodeRef the isNodeRef to set
		 */
		public void setIsNodeRef(Boolean isNodeRef) {
			this.isNodeRef = isNodeRef;
		}

		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/**
		 * @param type the type to set
		 */
		public void setType(String type) {
			this.type = type;
		}
		
		
		
		
 
 }

