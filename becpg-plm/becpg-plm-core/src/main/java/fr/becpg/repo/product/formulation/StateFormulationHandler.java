package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * Checks that states of product and sub-products are coherent
 * Evo #1908
 *
    Vérifier l'état du fournisseur
    Si on est à l'état A valider, remonter 1 alerte sur MP non validé ou si allergènes non renseignés (demande eu en démo)
 * @author steven
 *
 */
public class StateFormulationHandler extends FormulationBaseHandler<ProductData>{

	private static final Log logger = LogFactory.getLog(StateFormulationHandler.class);

	private AlfrescoRepository<ProductData> alfrescoRepository;
	
	private DictionaryService dictionaryService;
	
	private NodeService nodeService;
	
	private AssociationService associationService;

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}
	
	public void setDictionaryService(DictionaryService dictionaryService){
		this.dictionaryService = dictionaryService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}	

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	@Override
	public boolean process(ProductData context) throws FormulateException {
		for( CompoListDataItem compo : context.getCompoList()){
			visitPart(compo.getNodeRef(), context);
		}

		return false;
	}

	public void visitPart(NodeRef node, ProductData parent){
		if(logger.isDebugEnabled()){
			logger.debug("Visiting part "+node);
		}
		
		if(node == null || parent == null) return;
		ProductData found = alfrescoRepository.findOne(node);

		if(found != null) {
			visitChildren(found, parent);
		}
	}

	public void visitChildren(ProductData dat, ProductData parent){
		if(logger.isDebugEnabled()){
			logger.debug("Visiting children, state="+dat.getState()+", parent state="+parent.getState());
		}
		
		//check states are coherent
		if((parent.getState() == SystemState.ToValidate && (dat.getState()==SystemState.Archived || dat.getState()==SystemState.Refused)) 
				|| (parent.getState() == SystemState.Valid && dat.getState() != SystemState.Valid)){
			
			ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(parent.getNodeRef(), RequirementType.Forbidden, 
					"Can't have "+parent.getState()+" state with "+dat.getState()+" component", null, 
					new ArrayList<NodeRef>(), RequirementDataType.Formulation);
			
			rclDataItem.getSources().add(dat.getNodeRef());
			dat.getCompoListView().getReqCtrlList().add(rclDataItem);
		}
		
		//allergens must be present on toValidate state
		if(parent.getState()==SystemState.ToValidate){
			if(dat.getAllergenList() == null || dat.getAllergenList().isEmpty()){
				ReqCtrlListDataItem rclDataItem = createReqDataItem("", null);
//				 rclDataItem = new ReqCtrlListDataItem(parent.getNodeRef(), RequirementType.Forbidden, 
//						"Allergens are not present", null, 
//						new ArrayList<NodeRef>(), RequirementDataType.Formulation);
						rclDataItem.getSources().add(dat.getNodeRef());
				
				parent.getCompoListView().getReqCtrlList().add(rclDataItem);
						
			}
		}
		
//		dat.get
		
		//if has no plants
		if(associationService.getTargetAssoc(dat.getNodeRef(), PLMModel.ASSOC_PLANTS) == null){
//			ReqCtrlListDataItem rclDataItem = createReqDataItem(dat.getNodeRef(), "", null);
//			rclDataItem.getSources().add(dat.getNodeRef());
			
		}
		
		
		
		QName qname = PLMModel.TYPE_ALLERGENLIST;
		QName dataListState = BeCPGModel.PROP_ENTITYLIST_STATE;
		
		PropertyDefinition node = dictionaryService.getProperty(dataListState);
		
		//SystemState allergenListState = alfrescoRepository.findOne(id)
		if((parent.getState() == SystemState.ToValidate && dat.getState() != SystemState.Valid)
				){
			//op
		}
		
		
		
		//No ReqCtrl if compo is validated
		if(dat.getState() == SystemState.Valid){
			dat.getCompoListView().getReqCtrlList().clear();
		}
	}
	
	public ReqCtrlListDataItem createReqDataItem(String key, String... params){
		String message = I18NUtil.getMessage(key, (Object[])params);
		
		ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null, new ArrayList<>(), RequirementDataType.Formulation);
		return rclDataItem;
	}
}
