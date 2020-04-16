package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
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

public class IngRequirementScanner extends AbstractRequirementScanner<ForbiddenIngListDataItem> {

	private boolean ingsCalculatingWithYield = false;

	private static Log logger = LogFactory.getLog(IngRequirementScanner.class);

	private static final String MESSAGE_NOTAUTHORIZED_ING = "message.formulate.notauhorized.ing";

	private static final String MESSAGE_MISSING_INGLIST = "message.formulate.missing.ingList";
	private static final String MESSAGE_INCORRECT_INGLIST_TOTAL = "message.formulate.incorrect.ingList.total";
	private static final String MESSAGE_FORBIDDEN_ING = "message.formulate.ingredient.forbidden";

	AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	public void setIngsCalculatingWithYield(boolean ingsCalculatingWithYield) {
		this.ingsCalculatingWithYield = ingsCalculatingWithYield;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	public List<ReqCtrlListDataItem> checkRequirements(ProductData productData, List<ProductSpecificationData> specifications) {

		Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap = new HashMap<>();

		if ((productData.getIngList() != null) && !productData.getIngList().isEmpty()) {

			boolean checkAutorized = false;

			List<ForbiddenIngListDataItem> requirements = extractRequirements(specifications);

			Set<NodeRef> visited = new HashSet<>();

			if (productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				for (CompoListDataItem compoListDataItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

					if ((compoListDataItem.getQtySubFormula() != null) && (compoListDataItem.getQtySubFormula() > 0)) {
						ProductData componentProductData = (ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct());

						checkILOfPart(compoListDataItem.getProduct(), compoListDataItem.getDeclType(), componentProductData, requirements, reqCtrlMap,
								visited);

					}

				}
			}

			for (ForbiddenIngListDataItem fil : requirements) {

				for (IngListDataItem ingListDataItem : productData.getIngList()) {
					if (!RequirementType.Authorized.equals(fil.getReqType())) {
						// Ings
						if (!fil.getIngs().isEmpty() && (fil.getReqMessage() != null) && !fil.getReqMessage().isEmpty()) {
							if (fil.getIngs().contains(ingListDataItem.getIng())) {
								if ((ingListDataItem.getQtyPerc() == null) || 
										((fil.getQtyPercMaxi() != null) && (fil.getQtyPercMaxi() <= ingListDataItem.getQtyPerc()))) {

									// req not respected
									ReqCtrlListDataItem reqCtrl = reqCtrlMap.get(fil.getNodeRef());
									if (reqCtrl == null) {
										reqCtrl = new ReqCtrlListDataItem(null, fil.getReqType(), fil.getReqMessage(), ingListDataItem.getIng(),
												new ArrayList<NodeRef>(), RequirementDataType.Specification);
										reqCtrlMap.put(fil.getNodeRef(), reqCtrl);
									} else {
										reqCtrl.setReqDataType(RequirementDataType.Specification);
									}
								}
							}
						} else if ((productData.getCompoListView().getCompoList() == null)
								|| productData.getCompoListView().getCompoList().isEmpty()) {
							if (checkRuleMatchIng(ingListDataItem, fil)) {
								MLText curMessage = fil.getReqMessage();
								if ((curMessage == null) || curMessage.values().stream().noneMatch(mes -> (mes != null) && !mes.isEmpty())) {
									curMessage = new MLText(I18NUtil.getMessage(MESSAGE_FORBIDDEN_ING,
											nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME)));
								}

								ReqCtrlListDataItem reqCtrl = reqCtrlMap.get(fil.getNodeRef());
								if (reqCtrl == null) {
									reqCtrl = new ReqCtrlListDataItem(null, fil.getReqType(), curMessage, null, new ArrayList<NodeRef>(),
											RequirementDataType.Specification);
									reqCtrlMap.put(fil.getNodeRef(), reqCtrl);
								} else {
									reqCtrl.setReqDataType(RequirementDataType.Specification);
								}

							}

						}

					} else {
						checkAutorized = true;
					}

				}
			}

			// Check autorized

			if (checkAutorized) {

				for (IngListDataItem ingListDataItem : productData.getIngList()) {
					boolean autorized = false;

					for (ForbiddenIngListDataItem fil : requirements) {
						if (RequirementType.Authorized.equals(fil.getReqType())) {
							if (checkRuleMatchIng(ingListDataItem, fil)) {
								autorized = true;
								if ((fil.getReqMessage() != null) && (fil.getReqMessage().getDefaultValue() != null)
										&& (!fil.getReqMessage().getDefaultValue().isEmpty())) {
									addReqCtrl(reqCtrlMap, fil.getNodeRef(), RequirementType.Authorized, fil.getReqMessage(),
											ingListDataItem.getIng(), RequirementDataType.Specification);
								}
								break;
							}
						}
					}

					if (!autorized) {
						String message = I18NUtil.getMessage(MESSAGE_NOTAUTHORIZED_ING);
						addReqCtrl(reqCtrlMap, new NodeRef(RepoConsts.SPACES_STORE, "ing-notauhtorised"), RequirementType.Forbidden,
								new MLText(message), ingListDataItem.getIng(), RequirementDataType.Specification);
					}

				}

			}

		}

		return new LinkedList<>(reqCtrlMap.values());
	}

	/**
	 * check the ingredients of the part according to the specification
	 *
	 * @param compoListDataItem
	 *            the compo list data item
	 * @param ingMap
	 *            the ing map
	 * @param totalQtyIngMap
	 *            the total qty ing map
	 */
	private void checkILOfPart(NodeRef productNodeRef, DeclarationType declType, ProductData componentProductData,
			List<ForbiddenIngListDataItem> forbiddenIngredientsList, Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap, Set<NodeRef> visited) {

		if (!PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.equals(nodeService.getType(productNodeRef)) && !visited.contains(productNodeRef)) {

			visited.add(productNodeRef);

			// datalist ingList is null or empty
			if ((componentProductData.getIngList() == null) || componentProductData.getIngList().isEmpty()) {

				if ((declType == null) || (!declType.equals(DeclarationType.DoNotDetails) && !declType.equals(DeclarationType.Omit))) {
					// req not respected
					String message = I18NUtil.getMessage(MESSAGE_MISSING_INGLIST);
					addReqCtrl(reqCtrlMap, new NodeRef(RepoConsts.SPACES_STORE, "missing-inglist"), RequirementType.Tolerated, new MLText(message),
							productNodeRef, RequirementDataType.Ingredient);
				}
			} else {

				if ((declType == null) || (!declType.equals(DeclarationType.DoNotDetails) && !declType.equals(DeclarationType.Omit))) {
					Double total = 0d;
					for (IngListDataItem ingListDataItem : componentProductData.getIngList()) {
						if ((ingListDataItem.getQtyPerc() != null)
								&& ((ingListDataItem.getDepthLevel() == null) || (ingListDataItem.getDepthLevel() == 1))) {
							total += ingListDataItem.getQtyPerc();
						}

					}

					if (ingsCalculatingWithYield && (componentProductData.getYield() != null)) {
						total = (componentProductData.getYield() * total) / 100d;
					}
					// Due to double precision
					if (Math.abs(total - 100d) > 0.00001) {
						String message = I18NUtil.getMessage(MESSAGE_INCORRECT_INGLIST_TOTAL);
						addReqCtrl(reqCtrlMap, new NodeRef(RepoConsts.SPACES_STORE, "incorrect-inglist-total"), RequirementType.Tolerated,
								new MLText(message), productNodeRef, RequirementDataType.Ingredient);
					}

				}

				forbiddenIngredientsList.forEach(fil -> {

					componentProductData.getIngList().forEach(ingListDataItem -> {

						if (!RequirementType.Authorized.equals(fil.getReqType()) && (fil.getReqMessage() != null) && !fil.getReqMessage().isEmpty()) {

							if (checkRuleMatchIng(ingListDataItem, fil)) {
								// Look for raw material
								if ((componentProductData.getCompoListView().getCompoList() != null)
										&& !componentProductData.getCompoListView().getCompoList().isEmpty()) {
									for (CompoListDataItem c : componentProductData.getCompoListView().getCompoList()) {
										checkILOfPart(c.getProduct(), declType, (ProductData) alfrescoRepository.findOne(c.getProduct()),
												forbiddenIngredientsList, reqCtrlMap, visited);
									}
								} else {
									MLText curMessage = fil.getReqMessage();
									if ((curMessage == null) || curMessage.values().stream().noneMatch(mes -> (mes != null) && !mes.isEmpty())) {
										curMessage = new MLText(I18NUtil.getMessage(MESSAGE_FORBIDDEN_ING,
												nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME)));
									}
									;

									logger.debug("Adding not respected for: " + curMessage);

									ReqCtrlListDataItem reqCtrl = reqCtrlMap.get(fil.getNodeRef());
									if (reqCtrl == null) {
										reqCtrl = new ReqCtrlListDataItem(null, fil.getReqType(), curMessage, null, new ArrayList<NodeRef>(),
												RequirementDataType.Specification);
										reqCtrlMap.put(fil.getNodeRef(), reqCtrl);
									} else {
										reqCtrl.setReqDataType(RequirementDataType.Specification);
									}

									if (!reqCtrl.getSources().contains(productNodeRef)) {
										reqCtrl.getSources().add(productNodeRef);
									}
								}
							}
						}

					});
				});

			}
		}
	}

	@Override
	protected List<ForbiddenIngListDataItem> getDataListVisited(ProductData partProduct) {
		return ((ProductSpecificationData) partProduct).getForbiddenIngList();
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

		// Ings
		if (!fil.getIngs().isEmpty()) {
			if (!fil.getIngs().contains(ingListDataItem.getIng())) {
				return false; // check next rule
			} else if (fil.getQtyPercMaxi() != null) {
				return false; // check next rule (we will
				// check
				// in
				// checkILOfFormulatedProduct)
			}
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

	private void addReqCtrl(Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap, NodeRef reqNodeRef, RequirementType requirementType, MLText message,
			NodeRef sourceNodeRef, RequirementDataType requirementDataType) {

		ReqCtrlListDataItem reqCtrl = reqCtrlMap.get(reqNodeRef);
		if (reqCtrl == null) {
			reqCtrl = new ReqCtrlListDataItem(null, requirementType, message, null, new ArrayList<NodeRef>(), requirementDataType);
			reqCtrlMap.put(reqNodeRef, reqCtrl);
		} else {
			reqCtrl.setReqDataType(requirementDataType);
		}

		if (!reqCtrl.getSources().contains(sourceNodeRef)) {
			reqCtrl.getSources().add(sourceNodeRef);
		}
	}

	@Override
	protected void mergeRequirements(List<ForbiddenIngListDataItem> ret, List<ForbiddenIngListDataItem> toAdd) {
		ret.addAll(toAdd);
	}

}
