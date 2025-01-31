/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.SupplierData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.project.formulation.ScoreListFormulationHandler;
import fr.becpg.repo.repository.model.SimpleListDataItem;
import fr.becpg.repo.survey.data.SurveyableEntity;

/**
 * <p>LCACalculatingFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ProductScoreListFormulationHandler extends ScoreListFormulationHandler {

	private static final Log logger = LogFactory.getLog(ProductScoreListFormulationHandler.class);

	
	/** {@inheritDoc} */
	@Override
	protected boolean accept(SurveyableEntity surveyableEntity) {
		return !isTemplateEntity(surveyableEntity) &&surveyableEntity instanceof ProductSpecificationData &&
	    		surveyableEntity.getScoreList() != null && alfrescoRepository.hasDataList(surveyableEntity, ProjectModel.TYPE_SCORE_LIST);
		
		
	}


//	/** {@inheritDoc} */
//	@Override
//	public boolean process(ProductData formulatedProduct) {
//
//		if (accept(formulatedProduct)) {
//
//			if (getDataListVisited(formulatedProduct) == null) {
//				formulatedProduct.setScoreList(new ArrayList<>());
//			}
//
//			boolean hasCompoEl = formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
//					|| formulatedProduct.hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
//					|| formulatedProduct.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE));
//
//			formulateSimpleList(formulatedProduct, getDataListVisited(formulatedProduct), new CostListQtyProvider(formulatedProduct), hasCompoEl);
//
//			if (getDataListVisited(formulatedProduct) != null) {
//
//				computeFormulatedList(formulatedProduct, getDataListVisited(formulatedProduct), ProjectModel.PROP_SCORE_CRITERION_FORMULA, "TODO");
//
//			}
//
//
//			// profitability and multilevel
//			scoreListFormulationHandler.calculateScore(formulatedProduct);
//		}
//		return true;
//
//	}
////	
//	
//	protected void synchronizeTemplate(ProductData formulatedProduct, 
//	                                   List<ScoreListDataItem> simpleListDataList, 
//	                                   List<ScoreListDataItem> toRemove) {
//	    
//	    // Synchronize with entity template
//	    if (formulatedProduct.getEntityTpl() != null && 
//	        !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {
//	        synchronizeWithEntityTemplate(formulatedProduct, simpleListDataList, toRemove);
//	    }
//
//	    // Synchronize with clients, suppliers, and product specifications
//	    synchronizeWithRelatedEntities(formulatedProduct, simpleListDataList, toRemove);
//	}
//
//	private void synchronizeWithEntityTemplate(ProductData formulatedProduct, 
//	                                           List<ScoreListDataItem> simpleListDataList, 
//	                                           List<ScoreListDataItem> toRemove) {
//	    List<ScoreListDataItem> templateScoreList = formulatedProduct.getEntityTpl().getScoreList();
//	    
//	    templateScoreList.forEach(templateScoreItem -> 
//	        synchronizeScore(formulatedProduct, templateScoreItem, simpleListDataList, true, toRemove)
//	    );
//
//	    updateScoreListSorting(simpleListDataList, templateScoreList);
//	}
//
//	private void updateScoreListSorting(List<ScoreListDataItem> simpleListDataList, 
//	                                    List<ScoreListDataItem> templateScoreList) {
//	    int lastSort = 0;
//	    for (ScoreListDataItem sl : simpleListDataList) {
//	        if (sl.getCharactNodeRef() == null) continue;
//	        
//	        Optional<ScoreListDataItem> matchingTemplateItem = templateScoreList.stream()
//	            .filter(tsl -> sl.getCharactNodeRef().equals(tsl.getCharactNodeRef()))
//	            .findFirst();
//	        
//	        if (matchingTemplateItem.isPresent()) {
//	            lastSort = (matchingTemplateItem.get().getSort() != null ? 
//	                        matchingTemplateItem.get().getSort() : 0) * 100;
//	            sl.setSort(lastSort);
//	        } else {
//	            sl.setSort(++lastSort);
//	        }
//	    }
//	}
//
//	private void synchronizeWithRelatedEntities(ProductData formulatedProduct, 
//	                                            List<ScoreListDataItem> simpleListDataList, 
//	                                            List<ScoreListDataItem> toRemove) {
//	    // Synchronize with clients
//	    Optional.ofNullable(formulatedProduct.getClients())
//	        .ifPresent(clients -> clients.forEach(client -> 
//	            client.getScoreList().forEach(templateScoreList -> 
//	                synchronizeScore(formulatedProduct, templateScoreList, simpleListDataList, false, toRemove)
//	            )
//	        ));
//
//	    // Synchronize with suppliers
//	    Optional.ofNullable(formulatedProduct.getSuppliers())
//	        .ifPresent(suppliers -> suppliers.forEach(supplierNodeRef -> {
//	            SupplierData supplier = (SupplierData) alfrescoRepository.findOne(supplierNodeRef);
//	            supplier.getScoreList().forEach(templateScoreList -> 
//	                synchronizeScore(formulatedProduct, templateScoreList, simpleListDataList, false, toRemove)
//	            );
//	        }));
//
//	    // Synchronize with product specifications
//	    Optional.ofNullable(formulatedProduct.getProductSpecifications())
//	        .ifPresent(specifications -> specifications.forEach(productSpecification -> 
//	            productSpecification.getScoreList().forEach(templateScoreList -> 
//	                synchronizeScore(formulatedProduct, templateScoreList, simpleListDataList, false, toRemove)
//	            )
//	        ));
//	}
//
//	private void synchronizeScore(ProductData formulatedProduct, 
//	                               ScoreListDataItem templateScoreListItem, 
//	                               List<ScoreListDataItem> scoreList,
//	                               boolean isTemplateScore, 
//	                               List<ScoreListDataItem> toRemove) {
//	    boolean[] addScore = {true};
//	    
//	    scoreList.stream()
//	        .filter(scoreListItem -> scoreListItem.getCharactNodeRef() != null && 
//	                 scoreListItem.getCharactNodeRef().equals(templateScoreListItem.getCharactNodeRef()))
//	        .findFirst()
//	        .ifPresentOrElse(
//	            existingScoreItem -> {
//	                if (isTemplateScore) {
//	                    existingScoreItem.setParent(
//	                        templateScoreListItem.getParent() != null 
//	                            ? findParentByCharactName(scoreList, templateScoreListItem.getParent().getCharactNodeRef()) 
//	                            : null
//	                    );
//	                }
//	                toRemove.remove(existingScoreItem);
//	                addScore[0] = false;
//	            },
//	            () -> {
//	                ScoreListDataItem newScoreItem = (ScoreListDataItem) templateScoreListItem.copy();
//	                newScoreItem.setNodeRef(null);
//	                newScoreItem.setParentNodeRef(null);
//	                
//	                if (newScoreItem.getParent() != null) {
//	                    newScoreItem.setParent(
//	                        findParentByCharactName(scoreList, newScoreItem.getParent().getCharactNodeRef())
//	                    );
//	                }
//	                
//	                scoreList.add(newScoreItem);
//	            }
//	        );
//	}
//
//	
//
//	/**
//	 * <p>findParentByCharactName.</p>
//	 *
//	 * @param simpleListDataList a {@link java.util.List} object.
//	 * @param charactNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
//	 * @return a T object.
//	 */
//	@Deprecated
//	protected T findParentByCharactName(List<T> simpleListDataList, NodeRef charactNodeRef) {
//		for (T listItem : simpleListDataList) {
//			if ((listItem.getCharactNodeRef() != null) && listItem.getCharactNodeRef().equals(charactNodeRef)) {
//				return listItem;
//			}
//		}
//		return null;
//	}
	

}
