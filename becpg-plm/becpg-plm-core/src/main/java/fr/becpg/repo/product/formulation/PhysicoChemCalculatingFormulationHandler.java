/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.SimpleListDataItem;

/**
 * <p>PhysicoChemCalculatingFormulationHandler class.</p>
 *
 * @author querephi
 * @version $Id: $Id
 */
public class PhysicoChemCalculatingFormulationHandler extends AbstractSimpleListFormulationHandler<PhysicoChemListDataItem> {

	private static final Log logger = LogFactory.getLog(PhysicoChemCalculatingFormulationHandler.class);

	private EntityTplService entityTplService;

	private AlfrescoRepository<ProductData> alfrescoRepositoryProductData;
	
	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}
	
	public void setAlfrescoRepositoryProductData(AlfrescoRepository<ProductData> alfrescoRepositoryProductData) {
		this.alfrescoRepositoryProductData = alfrescoRepositoryProductData;
	}

	/** {@inheritDoc} */
	@Override
	protected Class<PhysicoChemListDataItem> getInstanceClass() {
		return PhysicoChemListDataItem.class;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) {

		boolean accept = accept(formulatedProduct);

		if (accept) {
			logger.debug("Physico chemical calculating visitor");

			if (formulatedProduct.getPhysicoChemList() == null) {
				formulatedProduct.setPhysicoChemList(new LinkedList<>());
			}

			formulateSimpleList(formulatedProduct, formulatedProduct.getPhysicoChemList(), new SimpleListQtyProvider() {

				Double netQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
				Double netWeight = FormulationHelper.getNetWeight(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
				
				@Override
				public Double getQty(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct) {
					return FormulationHelper.getQtyInKg(compoListDataItem);
				}
				
				@Override
				public Double getVolume(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct) {
					return FormulationHelper.getNetVolume(compoListDataItem, componentProduct);
				}

				@Override
				public Double getNetWeight() {
					return netWeight;
				}

				@Override
				public Double getNetQty() {
					return  netQty;
				}

				@Override
				public Boolean omitElement(CompoListDataItem compoListDataItem) {
					return DeclarationType.Omit.equals(compoListDataItem.getDeclType());
				}

				@Override
				public Double getQty(PackagingListDataItem packagingListDataItem, ProductData componentProduct) {
					return  FormulationHelper.getQtyForCostByPackagingLevel(formulatedProduct, packagingListDataItem, componentProduct);
				}

				@Override
				public Double getQty(ProcessListDataItem processListDataItem) {
					return FormulationHelper.getQty(formulatedProduct, processListDataItem);
				}

				
			},
					formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)));

			computeFormulatedList(formulatedProduct, formulatedProduct.getPhysicoChemList(), PLMModel.PROP_PHYSICO_CHEM_FORMULA,
					"message.formulate.physicoChemList.error");

		}

		if (accept || formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)
				|| (formulatedProduct instanceof ProductSpecificationData)) {
			formulatedProduct.getPhysicoChemList().forEach(n -> {

				n.setUnit((String) nodeService.getProperty(n.getPhysicoChem(), PLMModel.PROP_PHYSICO_CHEM_UNIT));
				n.setType((String) nodeService.getProperty(n.getPhysicoChem(), PLMModel.PROP_PHYSICO_CHEM_TYPE));

			});

		}

		return true;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean accept(ProductData formulatedProduct) {
		return !(formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData)
				|| ((formulatedProduct.getPhysicoChemList() == null)
						&& !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_PHYSICOCHEMLIST)));
	}

	/** {@inheritDoc} */
	@Override
	protected List<PhysicoChemListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getPhysicoChemList();
	}

	/** {@inheritDoc} */
	@Override
	protected boolean isCharactFormulated(SimpleListDataItem sl) {
		if (!super.isCharactFormulated(sl)) {
			return false;
		}
		return Boolean.TRUE.equals(nodeService.getProperty(sl.getCharactNodeRef(), PLMModel.PROP_PHYSICO_CHEM_FORMULATED));
	}

	/** {@inheritDoc} */
	@Override
	protected Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType) {

		Map<NodeRef, List<NodeRef>> mandatoryCharacts = new HashMap<>();

		NodeRef entityTplNodeRef = entityTplService.getEntityTpl(componentType);

		if (entityTplNodeRef != null) {

			List<PhysicoChemListDataItem> physicoChemList = alfrescoRepositoryProductData.findOne(entityTplNodeRef).getPhysicoChemList();

			for (PhysicoChemListDataItem physicoChemListDataItem : formulatedProduct.getPhysicoChemList()) {
				for (PhysicoChemListDataItem pC : physicoChemList) {
					if ((pC.getPhysicoChem() != null) && pC.getPhysicoChem().equals(physicoChemListDataItem.getPhysicoChem()) && isCharactFormulated(physicoChemListDataItem)) {
						mandatoryCharacts.put(pC.getPhysicoChem(), new ArrayList<>());
						break;
					}
				}
			}
		}
		return mandatoryCharacts;
	}

	/** {@inheritDoc} */
	@Override
	protected RequirementDataType getRequirementDataType() {
		return RequirementDataType.Physicochem;
	}

}
