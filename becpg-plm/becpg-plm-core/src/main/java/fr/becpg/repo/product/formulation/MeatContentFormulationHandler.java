package fr.becpg.repo.product.formulation;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.meat.MeatContentData;
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
 * @version $Id: $Id
 */
public class MeatContentFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static Log logger = LogFactory.getLog(MeatContentFormulationHandler.class);

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) 
				|| (formulatedProduct instanceof ProductSpecificationData)) {
			return true;
		}

		// no compo => no formulation
		if (!formulatedProduct.hasCompoListEl(new VariantFilters<>())) {
			logger.debug("no compo => no formulation");
			return true;
		}

		formulatedProduct.setMeatContentData(null);

		if (formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			for (CompoListDataItem compoItem : formulatedProduct
					.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
				Double weightUsed = FormulationHelper.getQtyInKg(compoItem);
				if ((weightUsed != null) && !DeclarationType.Omit.equals(compoItem.getDeclType())) {
					ProductData partProduct = (ProductData) alfrescoRepository.findOne(compoItem.getComponent());

					if (!(partProduct instanceof LocalSemiFinishedProductData)) {

						boolean formulateInVol = (partProduct.getUnit() != null) && partProduct.getUnit().isVolume();

						Double volUsed = FormulationHelper.getNetVolume(compoItem, partProduct);
						Double netWeight = FormulationHelper.getNetWeight(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);

						// calculate charact from qty or vol ?
						Double qtyUsedPerc = formulateInVol ? volUsed : weightUsed;

						if ((qtyUsedPerc != null) && (qtyUsedPerc != 0d)) {

//	Fix #7157   			Double lossPerc = FormulationHelper.getComponentLossPerc(partProduct, compoItem);
//							if ((lossPerc != null) && (lossPerc != 0)) {
//								qtyUsedPerc = FormulationHelper.getQtyWithLoss(qtyUsedPerc, lossPerc);
//							}

							if ((netWeight != null) && (netWeight != 0d)) {
								qtyUsedPerc = qtyUsedPerc / netWeight;
							}

							if ((partProduct.getMeatType() != null) && !partProduct.getMeatType().isEmpty()) {

								if (logger.isDebugEnabled()) {
									logger.debug("Found meat product : " + partProduct.getName() + " " + partProduct.getMeatType());
								}

								MeatContentData meatContentData = getOrCreateMeatData(formulatedProduct, partProduct.getMeatType());

								meatContentData.addQtyPerc(qtyUsedPerc * 100d);

								for (NutListDataItem nutListDataItem : partProduct.getNutList()) {

									NutDataItem nut = (NutDataItem) alfrescoRepository.findOne(nutListDataItem.getNut());

									if ((nutListDataItem.getValue() != null)) {
										Double value = nutListDataItem.getValue() * qtyUsedPerc;
										// Express all as g
										if (nutListDataItem.getUnit().startsWith("mg")) {
											value = value / 1000d;
										}
										if (nut.getNutCode() != null) {
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

								}

							} else if ((partProduct.getMeatContents() != null) && !partProduct.getMeatContents().isEmpty()) {

								for (Map.Entry<String, MeatContentData> entry : partProduct.getMeatContents().entrySet()) {

									// partMeatData is express for 100Perc of
									// meatType
									MeatContentData partMeatData = entry.getValue();

									getOrCreateMeatData(formulatedProduct, entry.getKey()).merge(partMeatData, qtyUsedPerc * 100d);
								}

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

	private MeatContentData getOrCreateMeatData(ProductData formulatedProduct, String meatType) {
		if (!formulatedProduct.getMeatContents().containsKey(meatType)) {
			formulatedProduct.getMeatContents().put(meatType, new MeatContentData(meatType));
		}
		return formulatedProduct.getMeatContents().get(meatType);
	}

}
