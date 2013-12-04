package fr.becpg.repo.admin.patch;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import com.google.common.collect.Lists;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Add NutParentLevelPatch
 */
public class NutParentLevelPatch extends AbstractBeCPGPatch {

	private static Log logger = LogFactory.getLog(NutParentLevelPatch.class);

	private static final String MSG_SUCCESS = "patch.bcpg.nutParentLevelPatch.result";

	private BeCPGSearchService beCPGSearchService;

	private AssociationService associationService;

	private BehaviourFilter policyBehaviourFilter;


	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	@Override
	protected String applyInternal() throws Exception {

		logger.info("Apply NutParentLevelPatch");

		List<NodeRef> dataListNodeRefs = beCPGSearchService.luceneSearch("+TYPE:\"bcpg:nutList\" NOT ASPECT:\"bcpg:depthLevelAspect\" ");
		logger.info("EntitySortableListPatch add sort in bcpg:entityListItem, size: " + dataListNodeRefs.size());

		int i = 0;
		List<List<NodeRef>> batches = Lists.partition(dataListNodeRefs, 500);
		for (final List<NodeRef> subList : batches) {
			try {
				i++;
				logger.info("Batch " + i + " over " + batches.size());
				transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>() {

					@Override
					public Void execute() throws Throwable {

						policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
						policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);

						try {
							for (NodeRef dataListNodeRef : subList) {
								if (nodeService.exists(dataListNodeRef)) {
									Map<QName, Serializable> properties = new HashMap<>();
									properties.put(BeCPGModel.PROP_DEPTH_LEVEL, 1);
									nodeService.addAspect(dataListNodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL, properties);
								} else {
									logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
								}
							}
							return null;

						} finally {
							policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
							policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
						}

					}

				}, false, true);
			} catch (Throwable e) {
				logger.warn(e, e);
			}
		}

		i = 1;
		updateParents();
		applySort("Energie kJ, étiquetage", i++);
		applySort("Energie kcal, étiquetage", i++);
		applySort("Energie kJ", i++);
		applySort("Energie kcal", i++);
		applySort("Lipides", i++);
		applySort("AG saturés", i++);
		applySort("AG monoinsaturés", i++);
		applySort("AG polyinsaturés", i++);
		applySort("Glucides", i++);
		applySort("Sucres", i++);
		applySort("Amidon", i++);
		applySort("Polyols totaux", i++);
		applySort("Fibres alimentaires", i++);
		applySort("Iode", i++);
		applySort("Protéines", i++);
		applySort("Protéines brutes (N x 6.25)", i++);
		applySort("Eau", i++);
		applySort("Sodium", i++);
		applySort("Sel", i++);
		applySort("Magnésium", i++);
		applySort("Phosphore", i++);
		applySort("Potassium", i++);
		applySort("Calcium", i++);
		applySort("Manganèse", i++);
		applySort("Fer total", i++);
		applySort("Cuivre", i++);
		applySort("Zinc", i++);
		applySort("Sélénium", i++);
		applySort("Rétinol", i++);
		applySort("Bêta-carotène", i++);
		applySort("Vitamine A", i++);
		applySort("Vitamine D", i++);
		applySort("Activité vitaminique E (en équivalents alpha-tocophérol)", i++);
		applySort("Vitamine C totale", i++);
		applySort("Vitamine B1 ou Thiamine", i++);
		applySort("Vitamine B2 ou Riboflavine", i++);
		applySort("Vitamine B3 ou PP ou Niacine", i++);
		applySort("Vitamine B5 ou Acide pantothénique", i++);
		applySort("Vitamine B6 ou Pyridoxine", i++);
		applySort("Vitamine B12 ou Cobalamines", i++);
		applySort("Vitamine B9 ou Folates totaux", i++);
		applySort("Alcool (éthanol)", i++);
		applySort("Acides organiques", i++);
		applySort("Cholestérol", i++);
		applySort("AG 4:0, butyrique", i++);
		applySort("AG 6:0, caproïque", i++);
		applySort("AG 8:0, caprylique", i++);
		applySort("AG 10:0, caprique", i++);
		applySort("AG 12:0, laurique", i++);
		applySort("AG 14:0, myristique", i++);
		applySort("AG 16:0, palmitique", i++);
		applySort("AG 18:0, stéarique", i++);
		applySort("AG 18:1 9c (n-9), oléique", i++);
		applySort("AG 18:2 9c,12c (n-6), linoléique", i++);
		applySort("AG 18:3 c9,c12,c15 (n-3), alpha-linolénique", i++);
		applySort("AG 20:4 5c,8c,11c,14c (n-6), arachidonique", i++);
		applySort("AG 20:5 5c,8c,11c,14c,17c (n-3), EPA", i++);
		applySort("AG 22:6 4c,7c,10c,13c,16c,19c (n-3), DHA", i++);

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	// Glucides
	// Sucres
	// Amidon
	// Polyols totaux

	// Lipides
	// AG saturés
	// AG monoinsaturés
	// AG polyinsaturés

	private void applySort(final String nutName, final int sort) {
		try {
			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>() {

				@Override
				public Void execute() throws Throwable {
					policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
					policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);

					try {

						NodeRef parent = firstOrNull(beCPGSearchService.luceneSearch("+TYPE:\"bcpg:nut\" AND +@cm\\:name:\"" + nutName + "\" "));
						if (parent != null) {
							List<NodeRef> parents = associationService.getSourcesAssocs(parent, BeCPGModel.ASSOC_NUTLIST_NUT);
							for (NodeRef item : parents) {
								nodeService.setProperty(item, BeCPGModel.PROP_SORT, sort);
							}
						}
						return null;
					} finally {
						policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
						policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
					}
				}

			}, false, true);
		} catch (Throwable e) {
			logger.warn(e, e);
		}
	}

	private void updateParents() {
		NodeRef parent = firstOrNull(beCPGSearchService.luceneSearch("+TYPE:\"bcpg:nut\" AND +@cm\\:name:\"Glucides\" "));
		List<NodeRef> childs = beCPGSearchService.luceneSearch("+TYPE:\"bcpg:nut\" AND( @cm\\:name:\"Sucre\" OR @cm\\:name:\"Amidon\" OR @cm\\:name:\"Polyols totaux\") ");

		updateParent(parent, childs);

		parent = firstOrNull(beCPGSearchService.luceneSearch("+TYPE:\"bcpg:nut\" AND +@cm\\:name:\"Lipides\" "));
		childs = beCPGSearchService.luceneSearch("+TYPE:\"bcpg:nut\" AND( @cm\\:name:\"AG saturés\" OR @cm\\:name:\"AG monoinsaturés\" OR @cm\\:name:\"AG polyinsaturés\") ");

		updateParent(parent, childs);

	}

	private NodeRef firstOrNull(List<NodeRef> nodeRefs) {
		return nodeRefs != null && !nodeRefs.isEmpty() ? nodeRefs.get(0) : null;
	}

	private void updateParent(NodeRef parent, List<NodeRef> childs) {
		if (parent != null && childs != null) {
			final List<NodeRef> parents = associationService.getSourcesAssocs(parent, BeCPGModel.ASSOC_NUTLIST_NUT);
			logger.info("Found " + parents.size() + " to check");

			for (NodeRef child : childs) {
				final String nutName = (String) nodeService.getProperty(child, ContentModel.PROP_NAME);
				List<NodeRef> items = associationService.getSourcesAssocs(child, BeCPGModel.ASSOC_NUTLIST_NUT);
				logger.info("Look for nutList: " + nutName + " (" + items.size() + ")");
				for (final List<NodeRef> subList : Lists.partition(items, 100)) {
					try {
						transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>() {

							@Override
							public Void execute() throws Throwable {
								policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
								policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);

								try {
									for (NodeRef check : parents) {
										for (NodeRef item : subList) {
											if (nodeService.getPrimaryParent(item).getParentRef().equals((nodeService.getPrimaryParent(check)).getParentRef())) {
												logger.debug("Updating parent for nut" + nutName + " " + item + " with " + check);
												nodeService.setProperty(item, BeCPGModel.PROP_PARENT_LEVEL, check);
												nodeService.setProperty(item, BeCPGModel.PROP_DEPTH_LEVEL, 2);
											}
										}
									}
									return null;
								} finally {
									policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
									policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
								}
							}

						}, false, true);
					} catch (Throwable e) {
						logger.warn(e, e);
					}
				}
			}
		}
	}
}
