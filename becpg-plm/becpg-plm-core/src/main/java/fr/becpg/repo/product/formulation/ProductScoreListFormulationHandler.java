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

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.SupplierData;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.project.formulation.ScoreListFormulationHandler;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.repository.model.SimpleListDataItem;

/**
 * <p>LCACalculatingFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ProductScoreListFormulationHandler extends AbstractSimpleListFormulationHandler<ScoreListDataItem> {

	private ScoreListFormulationHandler scoreListFormulationHandler;

	/**
	 * <p>Setter for the field <code>scoreListFormulationHandler</code>.</p>
	 *
	 * @param scoreListFormulationHandler a {@link fr.becpg.repo.project.formulation.ScoreListFormulationHandler} object
	 */
	public void setScoreListFormulationHandler(ScoreListFormulationHandler scoreListFormulationHandler) {
		this.scoreListFormulationHandler = scoreListFormulationHandler;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean accept(ProductData formulatedProduct) {

		boolean hasScoreList = (formulatedProduct.getScoreList() != null) && ((formulatedProduct.getScoreList() instanceof ArrayList)
				|| alfrescoRepository.hasDataList(formulatedProduct, ProjectModel.TYPE_SCORE_LIST));

		return !formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) && !(formulatedProduct instanceof ProductSpecificationData)
				&& hasScoreList;

	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) {

		if (accept(formulatedProduct)) {

			if (formulatedProduct.getSurveyList() == null) {
				formulatedProduct.setScoreList(new ArrayList<>());
			}

			scoreListFormulationHandler.formulateSurveylist(formulatedProduct);

			boolean hasCompoEl = formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
					|| formulatedProduct.hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
					|| formulatedProduct.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE));

			formulateSimpleList(formulatedProduct, getDataListVisited(formulatedProduct), new CostListQtyProvider(formulatedProduct), hasCompoEl);

			scoreListFormulationHandler.calculateScore(formulatedProduct);

			scoreListFormulationHandler.calculateScoreType(formulatedProduct);

		}

		return true;
	}

	/** {@inheritDoc} */
	@Override
	protected void synchronizeTemplate(ProductData formulatedProduct, List<ScoreListDataItem> simpleListDataList, List<ScoreListDataItem> toRemove) {

		// Synchronize with entity template
		if ((formulatedProduct.getEntityTpl() != null) && !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {
			synchronizeWithEntityTemplate(formulatedProduct, simpleListDataList, toRemove);
		}

		// Synchronize with clients, suppliers, and product specifications
		synchronizeWithRelatedEntities(formulatedProduct, simpleListDataList, toRemove);
	}

	private void synchronizeWithEntityTemplate(ProductData formulatedProduct, List<ScoreListDataItem> simpleListDataList,
			List<ScoreListDataItem> toRemove) {
		List<ScoreListDataItem> templateScoreList = formulatedProduct.getEntityTpl().getScoreList();

		if ((templateScoreList != null) && !templateScoreList.isEmpty()) {
			templateScoreList.forEach(templateScoreItem -> synchronizeScore(templateScoreItem, simpleListDataList, true, toRemove));
			updateScoreListSorting(simpleListDataList, templateScoreList);
		}

		if (formulatedProduct.getProductSpecifications() != null) {
			for (ProductSpecificationData productSpecificationData : formulatedProduct.getProductSpecifications()) {
				templateScoreList = productSpecificationData.getScoreList();
				if ((templateScoreList != null) && !templateScoreList.isEmpty()) {
					templateScoreList.forEach(templateScoreItem -> synchronizeScore(templateScoreItem, simpleListDataList, true, toRemove));
					updateScoreListSorting(simpleListDataList, templateScoreList);
				}
			}

		}

	}

	private void updateScoreListSorting(List<ScoreListDataItem> simpleListDataList, List<ScoreListDataItem> templateScoreList) {
		int lastSort = 0;
		for (ScoreListDataItem sl : simpleListDataList) {
			if (sl.getCharactNodeRef() == null) {
				continue;
			}

			Optional<ScoreListDataItem> matchingTemplateItem = templateScoreList.stream()
					.filter(tsl -> sl.getCharactNodeRef().equals(tsl.getCharactNodeRef())).findFirst();

			if (matchingTemplateItem.isPresent()) {
				lastSort = (matchingTemplateItem.get().getSort() != null ? matchingTemplateItem.get().getSort() : 0) * 100;
				sl.setSort(lastSort);
			} else {
				sl.setSort(++lastSort);
			}
		}
	}

	private void synchronizeWithRelatedEntities(ProductData formulatedProduct, List<ScoreListDataItem> simpleListDataList,
			List<ScoreListDataItem> toRemove) {

		// Synchronize with clients
		Optional.ofNullable(formulatedProduct.getClients())
				.ifPresent(clients -> clients.forEach(client -> Optional.ofNullable(client.getScoreList()).ifPresent(scoreList -> scoreList
						.forEach(templateScoreList -> synchronizeScore(templateScoreList, simpleListDataList, false, toRemove)))));

		// Synchronize with suppliers
		Optional.ofNullable(formulatedProduct.getSuppliers()).ifPresent(suppliers -> suppliers.forEach(supplierNodeRef -> {
			SupplierData supplier = (SupplierData) alfrescoRepository.findOne(supplierNodeRef);
			Optional.ofNullable(supplier.getScoreList()).ifPresent(
					scoreList -> scoreList.forEach(templateScoreList -> synchronizeScore(templateScoreList, simpleListDataList, false, toRemove)));
		}));

	}

	private void synchronizeScore(ScoreListDataItem templateScoreListItem, List<ScoreListDataItem> scoreList, boolean isTemplateScore,
			List<ScoreListDataItem> toRemove) {
		boolean[] addScore = { true };

		scoreList.stream()
				.filter(scoreListItem -> (scoreListItem.getCharactNodeRef() != null)
						&& scoreListItem.getCharactNodeRef().equals(templateScoreListItem.getCharactNodeRef()))
				.findFirst().ifPresentOrElse(existingScoreItem -> {

					if (!isTemplateScore && isCharactFormulated(existingScoreItem)) {
						existingScoreItem.setScore(templateScoreListItem.getScore());
					}

					toRemove.remove(existingScoreItem);
					addScore[0] = false;
				}, () -> {
					ScoreListDataItem newScoreItem = (ScoreListDataItem) templateScoreListItem.copy();
					newScoreItem.setNodeRef(null);
					newScoreItem.setParentNodeRef(null);
					scoreList.add(newScoreItem);
				});

		if (isTemplateScore) {
			scoreList.stream()
					.filter(scoreListItem -> (scoreListItem.getCharactNodeRef() != null)
							&& scoreListItem.getCharactNodeRef().equals(templateScoreListItem.getCharactNodeRef()))
					.findFirst().ifPresent(existingScoreItem -> {
						existingScoreItem.setParent(templateScoreListItem.getParent() != null
								? findParentByCharactName(scoreList, templateScoreListItem.getParent().getCharactNodeRef())
								: null);
					});

		}

	}

	/** {@inheritDoc} */
	@Override
	protected Class<ScoreListDataItem> getInstanceClass() {
		return ScoreListDataItem.class;
	}

	/** {@inheritDoc} */
	@Override
	protected List<ScoreListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getScoreList();
	}

	/** {@inheritDoc} */
	@Override
	protected Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType) {
		return new HashMap<>();
	}

	/** {@inheritDoc} */
	@Override
	protected boolean propagateModeEnable(ProductData formulatedProduct) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	protected ScoreListDataItem newSimpleListDataItem(NodeRef charactNodeRef) {
		return ScoreListDataItem.build().withScoreCriterion(charactNodeRef);
	}

	/** {@inheritDoc} */
	@Override
	protected RequirementDataType getRequirementDataType() {
		return RequirementDataType.Formulation;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean isCharactFormulated(SimpleListDataItem sl) {
		if (!super.isCharactFormulated(sl)) {
			return false;
		}
		return Boolean.TRUE.equals(nodeService.getProperty(sl.getCharactNodeRef(), ProjectModel.PROP_SCORE_CRITERION_FORMULATED));
	}

}
