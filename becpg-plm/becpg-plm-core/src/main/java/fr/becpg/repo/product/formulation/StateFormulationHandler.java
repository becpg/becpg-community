package fr.becpg.repo.product.formulation;

import java.util.ArrayList;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
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

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}
	
	public void setDictionaryService(DictionaryService dictionaryService){
		this.dictionaryService = dictionaryService;
	}


	@Override
	public boolean process(ProductData context) throws FormulateException {
		// TODO Auto-generated method stub
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
		
		if(parent.getState()==SystemState.ToValidate){
			if(dat.getAllergenList() == null || dat.getAllergenList().isEmpty()){
				
				ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(parent.getNodeRef(), RequirementType.Forbidden, 
						"Allergens are not present", null, 
						new ArrayList<NodeRef>(), RequirementDataType.Formulation);
						rclDataItem.getSources().add(dat.getNodeRef());
				
				parent.getCompoListView().getReqCtrlList().add(rclDataItem);
						
			}
		}
		
//		dat.get
		
		
		
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





}
