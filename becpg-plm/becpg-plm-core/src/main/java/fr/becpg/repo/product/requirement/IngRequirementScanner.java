package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>
 * IngRequirementScanner class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class IngRequirementScanner extends AbstractRequirementScanner<ForbiddenIngListDataItem> {

	private static Log logger = LogFactory.getLog(IngRequirementScanner.class);

	private static final String MESSAGE_NOTAUTHORIZED_ING = "message.formulate.notauhorized.ing";

	private static final String MESSAGE_FORBIDDEN_ING = "message.formulate.ingredient.forbidden";

	AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	/**
	 * <p>
	 * Setter for the field <code>alfrescoRepository</code>.
	 * </p>
	 *
	 * @param alfrescoRepository
	 *            a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	public List<ReqCtrlListDataItem> checkRequirements(ProductData productData, List<ProductSpecificationData> specifications) {

		List<ReqCtrlListDataItem> reqCtrlMap = new ArrayList<>();

		if ((productData.getIngList() != null) && !productData.getIngList().isEmpty()) {

			for (Map.Entry<ProductSpecificationData, List<ForbiddenIngListDataItem>> entry : extractRequirements(specifications).entrySet()) {
				boolean checkAutorized = false;

				List<ForbiddenIngListDataItem> requirements = entry.getValue();
				ProductSpecificationData specification = entry.getKey();

				Set<NodeRef> visited = new HashSet<>();
				Map<String, List<NodeRef>> sources = new HashMap<>();

				if (productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
					for (CompoListDataItem compoListDataItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
						if ((compoListDataItem.getProduct() != null)
								&& ((compoListDataItem.getQtySubFormula() != null) && (compoListDataItem.getQtySubFormula() > 0))) {
							ProductData componentProductData = (ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct());
							checkILOfPart(compoListDataItem.getProduct(), compoListDataItem.getDeclType(), componentProductData, requirements,
									specification, sources, visited);
						}
					}
				}

				for (ForbiddenIngListDataItem fil : requirements) {
					if (!RequirementType.Authorized.equals(fil.getReqType())) {
						processForbiddenRequirements(specification, productData, fil, reqCtrlMap, sources);
					} else {
						checkAutorized = true;
					}
				}

				if (checkAutorized) {
					processAuthorizedRequirements(productData, requirements, specification, reqCtrlMap);

				}
			}
		}

		return reqCtrlMap;

	}

	/**
	 * check the ingredients of the part according to the specification
	 *
	 * @param specification
	 *
	 * @param compoListDataItem
	 *            the compo list data item
	 * @param ingMap
	 *            the ing map
	 * @param totalQtyIngMap
	 *            the total qty ing map
	 */
	private void checkILOfPart(NodeRef productNodeRef, DeclarationType declType, ProductData componentProductData,
			List<ForbiddenIngListDataItem> forbiddenIngredientsList, ProductSpecificationData specification, Map<String, List<NodeRef>> sources,
			Set<NodeRef> visited) {

		if (!PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.equals(mlNodeService.getType(productNodeRef)) && !visited.contains(productNodeRef)) {

			visited.add(productNodeRef);

			if ((componentProductData.getIngList() != null) && !componentProductData.getIngList().isEmpty()) {

				forbiddenIngredientsList.forEach(fil ->

				componentProductData.getIngList().forEach(ingListDataItem -> {

					if (!RequirementType.Authorized.equals(fil.getReqType()) && !isQtyCheck(fil) && checkRuleMatchIng(ingListDataItem, fil)) {
						// Look for raw material

						if (componentProductData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
							for (CompoListDataItem compoListDataItem : componentProductData
									.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

								if ((compoListDataItem.getQtySubFormula() != null) && (compoListDataItem.getQtySubFormula() > 0)) {
									checkILOfPart(compoListDataItem.getProduct(), declType,
											(ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct()), forbiddenIngredientsList,
											specification, sources, visited);
								}
							}

						} else {
							String key = createReqSourceKey(fil, ingListDataItem);

							List<NodeRef> sourceList = sources.computeIfAbsent(key, f -> new ArrayList<>());
							sourceList.add(productNodeRef);
							sources.put(key, sourceList);
						}

					}

				}));

			}
		}
	}

	private void processForbiddenRequirements(ProductSpecificationData specification, ProductData productData, ForbiddenIngListDataItem fil,
			List<ReqCtrlListDataItem> reqCtrlMap, Map<String, List<NodeRef>> sources) {

		Double totalQtyPerc = calculateQtyPerc(fil, productData);

		for (IngListDataItem ingListDataItem : productData.getIngList()) {
			if (checkRuleMatchIng(ingListDataItem, fil)) {

				ReqCtrlListDataItem reqCtrl = createForbiddenReq(specification, fil, ingListDataItem, sources);

				if (isQtyCheck(fil)) {

					Double filMaxQtyPerc = getFilMaxQtyPerc(productData, fil);
					Double filMinQtyPerc = getFilMinQtyPerc(productData, fil);

					Double qtyPerc = getQtyPerc(ingListDataItem, fil);

					if (!((totalQtyPerc == null) || (qtyPerc == null) || (totalQtyPerc == 0) || (qtyPerc == 0))) {
						boolean dontMatchQty = ((filMaxQtyPerc != null) && (filMaxQtyPerc <= totalQtyPerc))
								|| ((filMinQtyPerc != null) && (filMinQtyPerc >= totalQtyPerc));

						boolean isInfo = !dontMatchQty && Boolean.TRUE.equals(addInfoReqCtrl) && (fil.getReqMessage() != null)
								&& !fil.getReqMessage().isEmpty();

						if (dontMatchQty || isInfo) {

							if (isInfo) {
								reqCtrl.setReqType(RequirementType.Info);
							} else if ((totalQtyPerc != null) && (filMaxQtyPerc != null) && (totalQtyPerc != 0)) {
								reqCtrl.setReqMaxQty((filMaxQtyPerc / totalQtyPerc) * 100d);
							} else if ((totalQtyPerc != null) && (filMinQtyPerc != null) && (totalQtyPerc != 0)) {
								reqCtrl.setReqMaxQty((filMinQtyPerc / totalQtyPerc) * 100d);
							}
							reqCtrlMap.add(reqCtrl);
						}
					}

				} else {
					reqCtrlMap.add(reqCtrl);
				}

			}

		}
	}

	private boolean isQtyCheck(ForbiddenIngListDataItem fil) {
		return (fil.getQtyPercMaxi() != null) || (fil.getQtyPercMini() != null);
	}

	private void processAuthorizedRequirements(ProductData productData, List<ForbiddenIngListDataItem> requirements,
			ProductSpecificationData specification, List<ReqCtrlListDataItem> reqCtrlMap) {
		for (IngListDataItem ingListDataItem : productData.getIngList()) {
			boolean autorized = false;

			for (ForbiddenIngListDataItem fil : requirements) {
				if (RequirementType.Authorized.equals(fil.getReqType()) && checkRuleMatchIng(ingListDataItem, fil)) {
					autorized = true;
					if ((fil.getReqMessage() != null) && (fil.getReqMessage().getDefaultValue() != null)
							&& (!fil.getReqMessage().getDefaultValue().isEmpty())) {

						reqCtrlMap.add(ReqCtrlListDataItem.build().ofType(RequirementType.Authorized).withMessage(fil.getReqMessage())
								.ofDataType(RequirementDataType.Specification)
								.withCharact(ingListDataItem.getNodeRef() != null ? ingListDataItem.getNodeRef() : ingListDataItem.getIng())
								.withSources(List.of(ingListDataItem.getIng())).withRegulatoryCode(extractRegulatoryId(fil, specification)));
					}
					break;
				}
			}

			if (!autorized) {
				reqCtrlMap.add(ReqCtrlListDataItem.build().ofType(RequirementType.Forbidden)
						.withMessage(MLTextHelper.getI18NMessage(MESSAGE_NOTAUTHORIZED_ING)).ofDataType(RequirementDataType.Specification)
						.withCharact(ingListDataItem.getNodeRef() != null ? ingListDataItem.getNodeRef() : ingListDataItem.getIng())
						.withSources(List.of(ingListDataItem.getIng())).withRegulatoryCode(extractRegulatoryId(null, specification)));
			}

		}

	}

	private ReqCtrlListDataItem createForbiddenReq(ProductSpecificationData specification, ForbiddenIngListDataItem fil,
			IngListDataItem ingListDataItem, Map<String, List<NodeRef>> sources) {

		MLText curMessage = fil.getReqMessage();
		if ((curMessage == null) || curMessage.values().stream().noneMatch(mes -> (mes != null) && !mes.isEmpty())) {
			curMessage = MLTextHelper.getI18NMessage(MESSAGE_FORBIDDEN_ING,
					mlNodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME));
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Adding not respected for: " + curMessage);
		}

		String key = createReqSourceKey(fil, ingListDataItem);
		List<NodeRef> sourceList = sources.computeIfAbsent(key, f -> new ArrayList<>());
		if (!fil.getIngs().isEmpty() && sourceList.isEmpty()) {
			sourceList.add(ingListDataItem.getIng());
		}

		return ReqCtrlListDataItem.build().ofType(fil.getReqType()).withMessage(curMessage)
				.withCharact(ingListDataItem.getNodeRef() != null ? ingListDataItem.getNodeRef() : ingListDataItem.getIng())
				.ofDataType(RequirementDataType.Specification).withRegulatoryCode(extractRegulatoryId(fil, specification)).withSources(sourceList);

	}

	private String createReqSourceKey(ForbiddenIngListDataItem fil, IngListDataItem ingListDataItem) {
		return ingListDataItem.getIng() + "" + fil.hashCode();
	}

	private Double calculateQtyPerc(ForbiddenIngListDataItem fil, ProductData productData) {
		Double totalQtyPerc = null;

		for (IngListDataItem ingListDataItem : productData.getIngList()) {
			if (!fil.getIngs().isEmpty() && fil.getIngs().contains(ingListDataItem.getIng())
					&& ((fil.getIngLevel() == null) || fil.getIngLevel().equals(ingListDataItem.getDepthLevel()))) {

				Double ingQtyPerc = getQtyPerc(ingListDataItem, fil);

				if (ingQtyPerc != null) {
					totalQtyPerc = (totalQtyPerc == null) ? ingQtyPerc : totalQtyPerc + ingQtyPerc;
				}
			}
		}

		return totalQtyPerc;
	}

	private Double getQtyPerc(IngListDataItem il, ForbiddenIngListDataItem fil) {
		if (fil.getQtyPercType() != null) {
			return switch (fil.getQtyPercType()) {
			case QtyPercWithYield -> il.getQtyPercWithYield();
			case QtyPercWithSecondaryYield -> il.getQtyPercWithSecondaryYield();
			case QtyPerc1 -> il.getQtyPerc1();
			case QtyPerc2 -> il.getQtyPerc2();
			case QtyPerc3 -> il.getQtyPerc3();
			case QtyPerc4 -> il.getQtyPerc4();
			case Mini -> il.getMini();
			case Maxi -> il.getMaxi();
			default -> il.getQtyPerc();
			};
		}
		return il.getQtyPerc();
	}

	private Double getFilQtyPerc(ProductData product, ForbiddenIngListDataItem fil, boolean isMaxi) {
		String unit = fil.getQtyPercMaxiUnit();
		Double quantity = isMaxi ? fil.getQtyPercMaxi() : fil.getQtyPercMini();

		switch (unit) {
		case "%":
			return quantity;
		case "mg/kg":
			return quantity / 10000;
		case "mg/L": {
			Double density = product.getDensity();
			if ((density == null) || (density == 0d)) {
				density = 1d;
			}
			return quantity / density / 10000;
		}
		default:
			break;
		}
		return quantity;
	}

	private Double getFilMaxQtyPerc(ProductData product, ForbiddenIngListDataItem fil) {
		return getFilQtyPerc(product, fil, true);
	}

	private Double getFilMinQtyPerc(ProductData product, ForbiddenIngListDataItem fil) {
		return getFilQtyPerc(product, fil, false);
	}

	/** {@inheritDoc} */
	@Override
	protected List<ForbiddenIngListDataItem> getDataListVisited(ProductData partProduct) {
		return ((ProductSpecificationData) partProduct).getForbiddenIngList() != null ? ((ProductSpecificationData) partProduct).getForbiddenIngList()
				: new ArrayList<>();
	}

	private boolean checkRuleMatchIng(IngListDataItem ingListDataItem, ForbiddenIngListDataItem fil) {

		if ((fil.getIsGMO() != null) && !fil.getIsGMO().isEmpty() && (!fil.getIsGMO().equals(ingListDataItem.getIsGMO().toString())
				|| (Boolean.FALSE.equals(Boolean.valueOf(fil.getIsGMO())) && Boolean.FALSE.equals(ingListDataItem.getIsGMO())))) {

			return false; // check next rule
		}

		// Ionized
		if ((fil.getIsIonized() != null) && !fil.getIsIonized().isEmpty() && (!fil.getIsIonized().equals(ingListDataItem.getIsIonized().toString())
				|| (Boolean.FALSE.equals(Boolean.valueOf(fil.getIsIonized())) && Boolean.FALSE.equals(ingListDataItem.getIsIonized())))) {
			return false; // check next rule
		}

		//Check same ing
		//Check level
		if ((!fil.getIngs().isEmpty() && !fil.getIngs().contains(ingListDataItem.getIng()))
				|| ((fil.getIngLevel() != null) && !fil.getIngLevel().equals(ingListDataItem.getDepthLevel()))) {
			return false;
		}

		// GeoOrigins
		if (!fil.getGeoOrigins().isEmpty()) {
			boolean hasGeoOrigin = false;
			for (NodeRef n : ingListDataItem.getGeoOrigin()) {
				if (fil.getGeoOrigins().contains(n)) {
					hasGeoOrigin = true;
				}
			}

			if (!hasGeoOrigin) {
				return false; // check next rule
			}
		}

		// Required GeoOrigins
		if (!fil.getRequiredGeoOrigins().isEmpty()) {
			boolean hasGeoOrigin = true;
			for (NodeRef n : ingListDataItem.getGeoOrigin()) {
				if (!fil.getRequiredGeoOrigins().contains(n)) {
					hasGeoOrigin = false;
				}
			}

			if (hasGeoOrigin) {
				return false; // check next rule
			}
		}

		// GeoTransfo
		if (!fil.getGeoTransfo().isEmpty()) {
			boolean hasGeoTransfo = false;
			for (NodeRef n : ingListDataItem.getGeoTransfo()) {
				if (fil.getGeoTransfo().contains(n)) {
					hasGeoTransfo = true;
				}
			}

			if (!hasGeoTransfo) {
				return false; // check next rule
			}
		}

		// BioOrigins
		if (!fil.getBioOrigins().isEmpty()) {
			boolean hasBioOrigin = false;
			for (NodeRef n : ingListDataItem.getBioOrigin()) {
				if (fil.getBioOrigins().contains(n)) {
					hasBioOrigin = true;
				}
			}
			if (!hasBioOrigin) {
				return false; // check next rule
			}

		}

		return true;
	}

	/** {@inheritDoc} */
	@Override
	protected void mergeRequirements(List<ForbiddenIngListDataItem> ret, List<ForbiddenIngListDataItem> toAdd) {
		ret.addAll(toAdd);
	}

}
