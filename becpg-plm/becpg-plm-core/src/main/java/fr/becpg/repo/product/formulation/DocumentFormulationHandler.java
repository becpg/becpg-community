package fr.becpg.repo.product.formulation;

import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.entity.policy.DocumentAspectPolicy;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;

public class DocumentFormulationHandler  extends FormulationBaseHandler<ProductData> {
	

	private FileFolderService fileFolderService;
	
	
	private static final Log logger = LogFactory.getLog(DocumentAspectPolicy.class);

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}




	@Override
	public boolean process(ProductData productData) {
	
		//Set document permissions
		for (FileInfo folder : fileFolderService.listFolders(productData.getNodeRef())) {
			logger.debug("TODO");
		}
		
		
		return true;
	}

}
