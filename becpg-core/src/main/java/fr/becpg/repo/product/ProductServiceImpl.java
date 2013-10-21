/*
 * 
 */
package fr.becpg.repo.product;

import java.util.List;

import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.AlfrescoRepository;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductServiceImpl.
 *
 * @author querephi
 */
@Service
public class ProductServiceImpl implements ProductService {

	private static Log logger = LogFactory.getLog(ProductServiceImpl.class);
	
	private AlfrescoRepository<ProductData> alfrescoRepository;
	
	private FormulationService<ProductData> formulationService;
	
	public BehaviourFilter policyBehaviourFilter;

	private CharactDetailsVisitorFactory charactDetailsVisitorFactory;
	
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setCharactDetailsVisitorFactory(CharactDetailsVisitorFactory charactDetailsVisitorFactory) {
		this.charactDetailsVisitorFactory = charactDetailsVisitorFactory;
	}

    public void setFormulationService(FormulationService<ProductData> formulationService) {
		this.formulationService = formulationService;
	}

   
	@Override
    public void formulate(NodeRef productNodeRef) throws FormulateException {
		formulate(productNodeRef,false);
    }       
    
   
	@Override
	public void formulate(NodeRef productNodeRef, boolean fast) throws FormulateException {
		
		try {
			policyBehaviourFilter.disableBehaviour(productNodeRef, ReportModel.ASPECT_REPORT_ENTITY);

			if(fast){
				formulationService.formulate(productNodeRef,"fastProductFormulationChain");
			}  else {
				formulationService.formulate(productNodeRef);
			}

		} finally {
			policyBehaviourFilter.enableBehaviour(productNodeRef, ReportModel.ASPECT_REPORT_ENTITY);
		}
		
	}
	
    @Override
    public ProductData formulate(ProductData productData) throws FormulateException {
    	return formulationService.formulate(productData);    	
    }    

	@Override
	public CharactDetails formulateDetails(NodeRef productNodeRef, QName datatType, String dataListName, List<NodeRef> elements) throws FormulateException {

    	ProductData productData = alfrescoRepository.findOne(productNodeRef);
    	        	
    	CharactDetailsVisitor visitor  = charactDetailsVisitorFactory.getCharactDetailsVisitor(datatType, dataListName);		
		return visitor.visit(productData, elements);		
	}
  

}
