package fr.becpg.repo.listvalue;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

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
        
   
        public ListValuePage(List<NodeRef> fullList, Integer pageNum, Integer pageSize, ListValueExtractor listValueExtractor) {
        	if(pageNum==null || pageNum <1){
        		pageNum = 1;
        	}
        	
            this.page = pageNum;
            this.pageSize = pageSize;
            this.fullListSize = fullList.size();
            results = listValueExtractor.extract(fullList.subList(Math.max((page-1) * pageSize,0), Math.min(page * pageSize, fullListSize))); 
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
		
 
 }

