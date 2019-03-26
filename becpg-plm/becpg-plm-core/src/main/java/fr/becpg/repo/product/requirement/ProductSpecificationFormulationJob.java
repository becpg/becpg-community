package fr.becpg.repo.product.requirement;


// Un cron régulier qui formule et recalcul tous les cahiers des charges

/*
 *  TODO 
 *  Ajouter une assoc respectedProductSpecifications
 *  Ajouter une liste cas d'emploi sur les spécifications avec MP - Famille /SS Famille - Date de formulation
 *  Pour toutes les MP et PFs pour chaque cahier des charges tester
 *   si les dates de modifs et de formulation si pas à jour rechecker le cahier des charges et mettre à jour l'assoc
 *   
 */

public class ProductSpecificationFormulationJob {

//	AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	
	
//	
//	if(formulatedProduct instanceof ProductSpecificationData) {
//		for(NodeRef productNodeRef : getProductNodeRefs((ProductSpecificationData) formulatedProduct)) {
//			ProductData  productData = (ProductData) alfrescoRepository.findOne(productNodeRef);
//			for (RequirementScanner scanner : requirementScanners) {
//				for(ReqCtrlListDataItem reqCtrlListDataItem :  scanner.checkRequirements(productData, Arrays.asList((ProductSpecificationData)formulatedProduct))) {
//					if(RequirementType.Forbidden.equals(reqCtrlListDataItem.getReqType()) && RequirementDataType.Specification.equals(reqCtrlListDataItem.getReqDataType())) {
//						//TODO 
//					}
//				}					
//			}
//		}
//	}
	//
//	private List<NodeRef> getProductNodeRefs(ProductSpecificationData formulatedProduct) {
//		return BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_RAWMATERIAL).excludeDefaults().maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list();
//	}
	
}
