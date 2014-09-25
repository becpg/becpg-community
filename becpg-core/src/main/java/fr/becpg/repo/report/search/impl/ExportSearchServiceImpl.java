/*
 * 
 */
package fr.becpg.repo.report.search.impl;

import java.io.OutputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.report.search.ExportSearchService;
import fr.becpg.repo.report.search.SearchReportRenderer;
import fr.becpg.report.client.ReportFormat;

/**
 * Class used to render the result of a search in a report
 *
 * @author matthieu
 */
@Service("exportSearchService")
public class ExportSearchServiceImpl implements ExportSearchService{		
	
	
	private static Log logger = LogFactory.getLog(ExportSearchServiceImpl.class);	
	
	@Autowired
	SearchReportRenderer[] searchReportRenderers;
	
	@Override
	public void createReport(QName nodeType, NodeRef templateNodeRef, List<NodeRef> searchResults, ReportFormat reportFormat, OutputStream outputStream) {
		
		if(templateNodeRef != null){
						
			
			SearchReportRenderer searchReportRender = getSearchReportRender(templateNodeRef, reportFormat);
			if(searchReportRender!=null){
				searchReportRender.renderReport(templateNodeRef, searchResults, reportFormat, outputStream);
			} else {
				logger.error("No search report renderer found for : "+reportFormat.toString()+" "+templateNodeRef);
			}
					
		}
	}
	
	private SearchReportRenderer getSearchReportRender(NodeRef templateNodeRef, ReportFormat reportFormat) {
	    if(searchReportRenderers!=null){
	    	for(SearchReportRenderer searchReportRenderer : searchReportRenderers){
	    		if(searchReportRenderer.isApplicable(templateNodeRef, reportFormat)){
	    			return searchReportRenderer;
	    		}
	    	}
	    }
		return null;
	}


		

}
