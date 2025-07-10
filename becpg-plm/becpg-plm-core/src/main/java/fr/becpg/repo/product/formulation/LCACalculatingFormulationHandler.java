/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.ClientData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.SupplierData;
import fr.becpg.repo.product.data.productList.LCAListDataItem;
import fr.becpg.repo.regulatory.RequirementDataType;

/**
 * <p>LCACalculatingFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class LCACalculatingFormulationHandler extends AbstractCostCalculatingFormulationHandler<LCAListDataItem> {

	private static final String MESSAGE_FORMULATE_LCA_LIST_ERROR = "message.formulate.lcaList.error";
	
	/** {@inheritDoc} */
	@Override
	protected void afterProcess(ProductData formulatedProduct) {
		if (shouldCalculateScore(formulatedProduct)) {
			
			Double singleScore = null;
			
			if (formulatedProduct.getLcaList() != null) {
				for (LCAListDataItem lcaItem : formulatedProduct.getLcaList()) {
					Double normalizationFactor = (Double) nodeService.getProperty(lcaItem.getLca(), PLMModel.PROP_LCA_NORMALIZATION);
					Double ponderationFactor = (Double) nodeService.getProperty(lcaItem.getLca(), PLMModel.PROP_LCA_PONDERATION);
					if (lcaItem.getValue() != null && normalizationFactor != null && ponderationFactor != null) {
						double partScore = lcaItem.getValue() / normalizationFactor * ponderationFactor * 10;
						if (singleScore == null) {
							singleScore = 0d;
						}
						singleScore += partScore;
					}
				}
			}
			
			formulatedProduct.setLcaScore(singleScore);
		}
	}

	private boolean shouldCalculateScore(ProductData formulatedProduct) {
		return formulatedProduct.getLcaScoreMethod() == null || "Formulation".equals(formulatedProduct.getLcaScoreMethod());
	}

	/** {@inheritDoc} */
	@Override
	protected List<LCAListDataItem> getDataListVisited(ClientData client) {
		return client.getLcaList();
	}

	/** {@inheritDoc} */
	@Override
	protected List<LCAListDataItem> getDataListVisited(SupplierData supplier) {
		return supplier.getLcaList();
	}

	/** {@inheritDoc} */
	@Override
	protected Class<LCAListDataItem> getInstanceClass() {
		return LCAListDataItem.class;
	}
	
	/** {@inheritDoc} */
	@Override
	protected LCAListDataItem newSimpleListDataItem(NodeRef charactNodeRef) {
		LCAListDataItem lcaListDataItem = new LCAListDataItem();
		lcaListDataItem.setCharactNodeRef(charactNodeRef);
		return lcaListDataItem;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean accept(ProductData formulatedProduct) {
		return !(formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData)
				|| ((formulatedProduct.getCostList() == null) && !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_LCALIST)));

	}

	/** {@inheritDoc} */
	@Override
	protected List<LCAListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getLcaList();
	}

	/** {@inheritDoc} */
	@Override
	protected RequirementDataType getRequirementDataType() {
		return RequirementDataType.Lca;
	}

	/** {@inheritDoc} */
	@Override
	protected void setDataListVisited(ProductData formulatedProduct) {
		formulatedProduct.setLcaList(new LinkedList<>());
	}

	/** {@inheritDoc} */
	@Override
	protected QName getCostFormulaPropName() {
		return PLMModel.PROP_LCA_FORMULA;
	}

	/** {@inheritDoc} */
	@Override
	protected QName getCostFixedPropName() {
		return PLMModel.PROP_LCAFIXED;
	}

	/** {@inheritDoc} */
	@Override
	protected QName getCostUnitPropName() {
		return PLMModel.PROP_LCAUNIT;
	}
	/** {@inheritDoc} */
	@Override
	protected String getFormulationErrorMessage() {
		return MESSAGE_FORMULATE_LCA_LIST_ERROR;
	}
}
