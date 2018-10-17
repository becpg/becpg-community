package fr.becpg.repo.product.formulation;

import java.util.Arrays;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.meat.MeatContentData;
import fr.becpg.repo.product.data.meat.MeatType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.NutDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * Quantitative Ingredient Declaration For Meat Product
 * https://www.fsai.ie/uploadedFiles/Site/FAQs/meat_content_calculation.pdf
 *
 * @author matthieu
 *
 */

public class MeatContentFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static Log logger = LogFactory.getLog(MeatContentFormulationHandler.class);

	private NodeService nodeService;

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)) {
			return true;
		}

		// no compo => no formulation
		if (!formulatedProduct.hasCompoListEl(new VariantFilters<>())) {
			logger.debug("no compo => no formulation");
			return true;
		}

		formulatedProduct.setMeatContentData(null);

		Double netWeight = FormulationHelper.getNetWeight(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
		FormulationHelper.getNetQtyInLorKg(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);

		if (formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			for (CompoListDataItem compoItem : formulatedProduct
					.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
				Double weightUsed = FormulationHelper.getQtyInKg(compoItem);
				Double volUsed = FormulationHelper.getNetVolume(compoItem, nodeService);

				if ((weightUsed != null) && !DeclarationType.Omit.equals(compoItem.getDeclType())) {
					ProductData partProduct = (ProductData) alfrescoRepository.findOne(compoItem.getComponent());

					if (!(partProduct instanceof LocalSemiFinishedProductData)) {

						boolean formulateInVol = FormulationHelper.isProductUnitLiter(partProduct.getUnit());

						// calculate charact from qty or vol ?
						Double qtyUsed = formulateInVol ? volUsed : weightUsed;

						if (partProduct.getMeatType() != null) {
							
							if (logger.isDebugEnabled()) {
								logger.debug("Found meat product : " + partProduct.getName() + " " + partProduct.getMeatType());
							}

							MeatContentData meatContentData = getOrCreateMeatData(formulatedProduct, partProduct.getMeatType());

							for (NutListDataItem nutListDataItem : partProduct.getNutList()) {

								NutDataItem nut = (NutDataItem) alfrescoRepository.findOne(nutListDataItem.getNut());

								if ((nutListDataItem.getValue() != null) && (qtyUsed != null)) {
									Double value = nutListDataItem.getValue() * qtyUsed;
									// Express all as g
									if (nutListDataItem.getUnit().startsWith("mg")) {
										value = value / 100d;
									}
									if ((netWeight != null) && (netWeight != 0d)) {
										value = value / netWeight;
									}

									
									switch (nut.getNutCode()) {
									case "FAT":
										meatContentData.addFatPerc(value);
										break;
									case "PRO-":
										meatContentData.addProteinPerc(value);
										// HYP hydroxyproline (mg)
										break;
									case "HYP":
										meatContentData.addCollagenPerc(8 * value);
										// COLG collagen (mg)
										break;
									case "COLG":
										meatContentData.addCollagenPerc(value);
										break;
									default:
										break;
									}

								}
								
							}

						} else if ((partProduct.getMeatContents() != null) && !partProduct.getMeatContents().isEmpty()) {

							for (Map.Entry<MeatType, MeatContentData> entry : partProduct.getMeatContents().entrySet()) {
								getOrCreateMeatData(formulatedProduct, entry.getKey()).merge(entry.getValue());
							}

						}

					}

				}
			}

		}

		for (MeatContentData meatContentData : formulatedProduct.getMeatContents().values()) {
			meatContentData.calculateMeatContent();
		}

		return true;

	}

	private MeatContentData getOrCreateMeatData(ProductData formulatedProduct, MeatType meatType) {
		if (!formulatedProduct.getMeatContents().containsKey(meatType)) {

			switch (meatType) {
			case Mammals:
				formulatedProduct.getMeatContents().put(MeatType.Mammals, new MeatContentData(MeatType.Mammals));
				break;
			case Porcines:
				formulatedProduct.getMeatContents().put(MeatType.Porcines, new MeatContentData(MeatType.Porcines));
				break;
			case BirdsAndRabbits:
				formulatedProduct.getMeatContents().put(MeatType.BirdsAndRabbits, new MeatContentData(MeatType.BirdsAndRabbits));
				break;
			}
		}
		return formulatedProduct.getMeatContents().get(meatType);
	}

}
