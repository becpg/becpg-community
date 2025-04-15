package fr.becpg.repo.admin.patch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.GS1Model;
import fr.becpg.model.PLMModel;

/**
 * <p>NutrientTypeCodePatch class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class NutrientTypeCodePatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(NutrientTypeCodePatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.nutrientTypeCode.result";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private BehaviourFilter policyBehaviourFilter;
	private RuleService ruleService;
	private IntegrityChecker integrityChecker;

	Map<String, String> nutrientTypeCode = new HashMap<>();

	{

		nutrientTypeCode.put("Water", "WATER");
		nutrientTypeCode.put("Sodium, Na", "NA");
		nutrientTypeCode.put("Magnesium, Mg", "MG");
		nutrientTypeCode.put("Phosphorus, P", "P");
		nutrientTypeCode.put("Potassium, K", "K");
		nutrientTypeCode.put("Calcium, Ca", "CA");
		nutrientTypeCode.put("Manganese", "MN");
		nutrientTypeCode.put("Iron, Fe", "FE");
		nutrientTypeCode.put("Copper", "CU");
		nutrientTypeCode.put("Zinc, Zn", "ZN");
		nutrientTypeCode.put("Selenium", "SE");
		nutrientTypeCode.put("Iodine", "ID");
		nutrientTypeCode.put("Chromium", "CR");
		nutrientTypeCode.put("Protein", "PRO-");
		nutrientTypeCode.put("Raw proteins (N x 6.25)", "PROCNT");
		nutrientTypeCode.put("Carbohydrate, by difference", "CHOAVL");
		nutrientTypeCode.put("Carbohydrate, by difference (with fiber)", "CHO-");
		nutrientTypeCode.put("Total Carbohydrate", "CHO-");
		nutrientTypeCode.put("Sugars, total", "SUGAR");
		nutrientTypeCode.put("Starch", "STARCH");
		nutrientTypeCode.put("Polyols, total", "POLYL");
		nutrientTypeCode.put("Fiber, total dietary", "FIBTG");
		nutrientTypeCode.put("NSP Fiber", "PSACNS");
		nutrientTypeCode.put("Total lipid (fat)", "FAT");
		nutrientTypeCode.put("Fatty acids, total saturated", "FASAT");
		nutrientTypeCode.put("Fatty acids, total monounsaturated", "FAMSCIS");
		nutrientTypeCode.put("Fatty acids, total polyunsaturated", "FAPUCIS");
		nutrientTypeCode.put("Retinol", "RETOL");
		nutrientTypeCode.put("Beta-caroten", "CARTB");
		nutrientTypeCode.put("Vitamin A", "VITA-");
		nutrientTypeCode.put("Vitamin A, IU", "VITAA");
		nutrientTypeCode.put("Vitamin A retinol equivalents", "VITA");
		nutrientTypeCode.put("Vitamin A, RAE", "VITA");
		nutrientTypeCode.put("Vitamin D", "VITD-");
		nutrientTypeCode.put("Vitamin D (D2 + D3)", "VITDEQ");
		nutrientTypeCode.put("Vitamin E (alpha-tocopherol)", "TOCPHA");
		nutrientTypeCode.put("Vitamin E", "VITE-");
		nutrientTypeCode.put("Vitamin K (phylloquinone)", "VITK1");
		nutrientTypeCode.put("Vitamin K2", "VITK2");
		nutrientTypeCode.put("Vitamin C, total ascorbic acid", "VITC-");
		nutrientTypeCode.put("Thiamin", "THIA");
		nutrientTypeCode.put("Riboflavin", "RIBF");
		nutrientTypeCode.put("Niacin", "NIA");
		nutrientTypeCode.put("Panto acid", "PANTAC");
		nutrientTypeCode.put("Vitamin B-6", "VITB6-");
		nutrientTypeCode.put("Vitamin B7 (Biotin)", "BIOT");
		nutrientTypeCode.put("Vitamin B-12", "VITB12");
		nutrientTypeCode.put("Food Folate", "FOL");
		nutrientTypeCode.put("Folate, DFE", "FOLDFE");
		nutrientTypeCode.put("Folic Acid", "FOLAC");
		nutrientTypeCode.put("Folate, natural", "FOLFD");
		nutrientTypeCode.put("CholineTot", "CHOLN");
		nutrientTypeCode.put("Beta-Crypt", "CRYPX");
		nutrientTypeCode.put("Lycopene", "LYCPN");
		nutrientTypeCode.put("Lut+Zea", "");
		nutrientTypeCode.put("Alcohol (ethanol)", "ALC");
		nutrientTypeCode.put("Organic acids, total", "OA");
		nutrientTypeCode.put("Cholesterol", "CHOL-");
		nutrientTypeCode.put("FA 4:0, butyric", "F4D0");
		nutrientTypeCode.put("FA 6:0, caproic", "F6D0");
		nutrientTypeCode.put("FA 8:0, caprylic", "F8D0");
		nutrientTypeCode.put("FA 10:0, capric", "F10D0");
		nutrientTypeCode.put("FA 12:0, lauric", "F12D0");
		nutrientTypeCode.put("FA 14:0 , myristic", "F14D0");
		nutrientTypeCode.put("FA 16:0, palmitic", "F16D0");
		nutrientTypeCode.put("FA 18:0, stearic", "F18D0");
		nutrientTypeCode.put("FA 18:1 n-9 cis, oleic", "F18D1CN9");
		nutrientTypeCode.put("FA 18:2 9c,12c (n-6), linoleic", "F18D1CN6");
		nutrientTypeCode.put("FA 18:3 c9,c12,c15 (n-3), alpha-linolenic", "F18D3N3");
		nutrientTypeCode.put("FA 20:4 5c,8c,11c,14c (n-6), arachidonic", "F20D4");
		nutrientTypeCode.put("FA 20:5 5c,8c,11c,14c,17c (n-3), EPA", "F20D5N3");
		nutrientTypeCode.put("FA 22:5 7c,10c,13c,16c,19c (n-3), DPA", "F22D5N3");
		nutrientTypeCode.put("FA 22:6 4c,7c,10c,13c,16c,19c (n-3), DHA", "F22D6N3");

		nutrientTypeCode.put("Energy kcal", "ENER-E14");
		nutrientTypeCode.put("Energy kJ", "ENER-KJO");
		nutrientTypeCode.put("Energy kcal Canada, USA", "US_ENER-E14");

		nutrientTypeCode.put("Salt", "NACL");
		nutrientTypeCode.put("Points (SP)", "");
		nutrientTypeCode.put("Points (SP)(Arrondi)", "");
		nutrientTypeCode.put("Ash", "ASH");
		nutrientTypeCode.put("Alpha-Carot", "CARTA");
		nutrientTypeCode.put("Provitamin A (b-carotene equivalents)", "CARTBEQ");
		nutrientTypeCode.put("Niacine (derived equivalents)", "NIAEQ");
		nutrientTypeCode.put("Caffein", "CAFFN");
		nutrientTypeCode.put("Tryptophan", "TRP");
		nutrientTypeCode.put("Total omega 3 fatty acids", "FAPUN3F");
		nutrientTypeCode.put("Total trans fatty acids", "FATRN");
		nutrientTypeCode.put("Added Sugars", "SUGAD");
		nutrientTypeCode.put("Soluble fiber", "FIBSOL");
		nutrientTypeCode.put("Insoluble fiber", "FIBINS");
		nutrientTypeCode.put("Alpha_Carot", "CARTA");
		nutrientTypeCode.put("Beta-carotene", "CARTB");
		nutrientTypeCode.put("Beta_Crypt", "CRYPX");
		nutrientTypeCode.put("Fatty acids, total trans", "FATRN");
		nutrientTypeCode.put("Fatty acids, omega 3", "FAPUN3F");
		nutrientTypeCode.put("Fatty acids, omega 6", "FAPUN6F");

		nutrientTypeCode.put("Eau", "WATER");
		nutrientTypeCode.put("Sodium", "NA");
		nutrientTypeCode.put("Magnésium", "MG");
		nutrientTypeCode.put("Phosphore", "P");
		nutrientTypeCode.put("Potassium", "K");
		nutrientTypeCode.put("Calcium", "CA");
		nutrientTypeCode.put("Manganèse", "MN");
		nutrientTypeCode.put("Fer", "FE");
		nutrientTypeCode.put("Cuivre", "CU");
		nutrientTypeCode.put("Zinc", "ZN");
		nutrientTypeCode.put("Sélénium", "SE");
		nutrientTypeCode.put("Iode", "ID");
		nutrientTypeCode.put("Chrome", "CR");
		nutrientTypeCode.put("Protéines", "PRO-");
		nutrientTypeCode.put("Protéines brutes (N x 6.25)", "PROCNT");
		nutrientTypeCode.put("Glucides", "CHOAVL");
		nutrientTypeCode.put("Glucides (avec fibre)", "CHO-");
		nutrientTypeCode.put("Sucres", "SUGAR");
		nutrientTypeCode.put("Amidon", "STARCH");
		nutrientTypeCode.put("Polyols totaux", "POLYL");
		nutrientTypeCode.put("Fibres alimentaires", "FIBTG");
		nutrientTypeCode.put("Fibres NSP", "PSACNS");
		nutrientTypeCode.put("Lipides", "FAT");
		nutrientTypeCode.put("AG saturés", "FASAT");
		nutrientTypeCode.put("AG monoinsaturés", "FAMSCIS");
		nutrientTypeCode.put("AG polyinsaturés", "FAPUCIS");
		nutrientTypeCode.put("Rétinol", "RETOL");
		nutrientTypeCode.put("Bêtacarotène", "CARTB");
		nutrientTypeCode.put("Vitamine A", "VITA-");
		nutrientTypeCode.put("Vitamine A, IU", "VITAA");
		nutrientTypeCode.put("Vitamine A (équivalent rétinol)", "VITA");
		nutrientTypeCode.put("Vitamine D", "VITD-");
		nutrientTypeCode.put("Vitamine D (D2 + D3)", "VITDEQ");
		nutrientTypeCode.put("Activité vitaminique E (en équivalents alpha-tocophérol)", "TOCPHA");
		nutrientTypeCode.put("Vitamine E", "VITE-");
		nutrientTypeCode.put("Vitamine K1", "VITK1");
		nutrientTypeCode.put("Vitamine K2", "VITK2");
		nutrientTypeCode.put("Vitamine C totale", "VITC-");
		nutrientTypeCode.put("Vitamine B1 ou Thiamine", "THIA");
		nutrientTypeCode.put("Vitamine B2 ou Riboflavine", "RIBF");
		nutrientTypeCode.put("Vitamine B3 ou PP ou Niacine", "NIA");
		nutrientTypeCode.put("Vitamine B5 ou Acide pantothénique", "PANTAC");
		nutrientTypeCode.put("Vitamine B6 ou Pyridoxine", "VITB6-");
		nutrientTypeCode.put("Vitamine B8 ou Biotine", "BIOT");
		nutrientTypeCode.put("Vitamine B12 ou Cobalamines", "VITB12");
		nutrientTypeCode.put("Vitamine B9 ou Folates totaux", "FOL");
		nutrientTypeCode.put("Folates, équivalent fibres alimentaires", "FOLDFE");
		nutrientTypeCode.put("Acide folique", "FOLAC");
		nutrientTypeCode.put("Folates naturels", "FOLFD");
		nutrientTypeCode.put("Choline", "CHOLN");
		nutrientTypeCode.put("Bêta-cryptoxanthine", "CRYPX");
		nutrientTypeCode.put("Lycopène", "LYCPN");
		nutrientTypeCode.put("Lutéine et zéaxanthine", "");
		nutrientTypeCode.put("Alcool (éthanol)", "ALC");
		nutrientTypeCode.put("Acides organiques", "OA");
		nutrientTypeCode.put("Cholestérol", "CHOL-");
		nutrientTypeCode.put("AG 4:0, butyrique", "F4D0");
		nutrientTypeCode.put("AG 6:0, caproïque", "F6D0");
		nutrientTypeCode.put("AG 8:0, caprylique", "F8D0");
		nutrientTypeCode.put("AG 10:0, caprique", "F10D0");
		nutrientTypeCode.put("AG 12:0, laurique", "F12D0");
		nutrientTypeCode.put("AG 14:0, myristique", "F14D0");
		nutrientTypeCode.put("AG 16:0, palmitique", "F16D0");
		nutrientTypeCode.put("AG 18:0, stéarique", "F18D0");
		nutrientTypeCode.put("AG 18:1 9c (n-9), oléique", "F18D1CN9");
		nutrientTypeCode.put("AG 18:2 9c,12c (n-6), linoléique", "F18D1CN6");
		nutrientTypeCode.put("AG 18:3 c9,c12,c15 (n-3), alpha-linolénique", "F18D3N3");
		nutrientTypeCode.put("AG 20:4 5c,8c,11c,14c (n-6), arachidonique", "F20D4");
		nutrientTypeCode.put("AG 20:5 5c,8c,11c,14c,17c (n-3), EPA", "F20D5N3");
		nutrientTypeCode.put("AG 22:5 7c,10c,13c,16c,19c (n-3), DPA", "F22D5N3");
		nutrientTypeCode.put("AG 22:6 4c,7c,10c,13c,16c,19c (n-3), DHA", "F22D6N3");

		nutrientTypeCode.put("Energie kcal", "ENER-E14");
		nutrientTypeCode.put("Energie kJ", "ENER-KJO");
		nutrientTypeCode.put("Energie kcal Canada, USA", "US_ENER-E14");

		nutrientTypeCode.put("Sel", "NACL");
		nutrientTypeCode.put("Résidu à sec", "ASH");
		nutrientTypeCode.put("Alpha-carotene", "CARTA");
		nutrientTypeCode.put("Provitamine A (équivalent b-carotène)", "CARTBEQ");
		nutrientTypeCode.put("Niacine (équivalent dérivés)", "NIAEQ");
		nutrientTypeCode.put("Caféine", "CAFFN");
		nutrientTypeCode.put("Tryptophane", "TRP");
		nutrientTypeCode.put("Acides gras oméga 3 totaux", "FAPUN3F");
		nutrientTypeCode.put("Acides gras trans totaux", "FATRN");
		nutrientTypeCode.put("Sucres ajoutés", "SUGAD");
		nutrientTypeCode.put("Fibres solubles", "FIBSOL");
		nutrientTypeCode.put("Fibres insolubles", "FIBINS");
	}

	/**
	 * <p>Setter for the field <code>ruleService</code>.</p>
	 *
	 * @param ruleService a {@link org.alfresco.service.cmr.rule.RuleService} object.
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	/**
	 * <p>Setter for the field <code>integrityChecker</code>.</p>
	 *
	 * @param integrityChecker a {@link org.alfresco.repo.node.integrity.IntegrityChecker} object.
	 */
	public void setIntegrityChecker(IntegrityChecker integrityChecker) {
		this.integrityChecker = integrityChecker;
	}

	/** {@inheritDoc} */
	@Override
	protected String applyInternal() throws Exception {

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getNodeDAO().getMaxNodeId();

			long minSearchNodeId = 0;

			final Pair<Long, QName> val = getQnameDAO().getQName(PLMModel.TYPE_NUT);

			@Override
			public int getTotalEstimatedWorkSize() {
				return result.size();
			}

			@Override
			public long getTotalEstimatedWorkSizeLong() {
				return getTotalEstimatedWorkSize();
			}

			@Override
			public Collection<NodeRef> getNextWork() {
				if (val != null) {
					Long typeQNameId = val.getFirst();

					result.clear();

					while (result.isEmpty() && (minSearchNodeId < maxNodeId)) {
						List<Long> nodeids = getPatchDAO().getNodesByTypeQNameId(typeQNameId, minSearchNodeId, minSearchNodeId + INC);

						for (Long nodeid : nodeids) {
							NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeid);
							if (!status.isDeleted()) {
								result.add(status.getNodeRef());
							}
						}
						minSearchNodeId = minSearchNodeId + INC;
					}
				}

				return result;
			}
		};

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("NutrientTypeCodePatch", transactionService.getRetryingTransactionHelper(),
				workProvider, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 500);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<>() {

			@Override
			public void afterProcess() throws Throwable {
				//Do nothing

			}

			@Override
			public void beforeProcess() throws Throwable {
				//Do nothing
			}

			@Override
			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			@Override
			public void process(NodeRef dataListNodeRef) throws Throwable {
				ruleService.disableRules();
				if (nodeService.exists(dataListNodeRef)) {
					AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
					policyBehaviourFilter.disableBehaviour();
					String name = (String) nodeService.getProperty(dataListNodeRef, ContentModel.PROP_NAME);
					String charactName = (String) nodeService.getProperty(dataListNodeRef, BeCPGModel.PROP_CHARACT_NAME);
					String nutTypeCode = (String) nodeService.getProperty(dataListNodeRef, GS1Model.PROP_NUTRIENT_TYPE_CODE);
					if ((name != null) && ((charactName != null) && !charactName.isEmpty()) && ((nutTypeCode == null) || nutTypeCode.isEmpty())) {
						if (nutrientTypeCode.containsKey(charactName) && !nutrientTypeCode.get(charactName).isEmpty()) {
							nodeService.setProperty(dataListNodeRef, GS1Model.PROP_NUTRIENT_TYPE_CODE, nutrientTypeCode.get(charactName));
						}
					}

				} else {
					logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
				}
				ruleService.enableRules();
			}

		};
		integrityChecker.setEnabled(false);
		try {
			batchProcessor.processLong(worker, true);
		} finally {
			integrityChecker.setEnabled(true);
		}
		return I18NUtil.getMessage(MSG_SUCCESS);

	}

	/**
	 * <p>Getter for the field <code>nodeDAO</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.domain.node.NodeDAO} object.
	 */
	public NodeDAO getNodeDAO() {
		return nodeDAO;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Setter for the field <code>nodeDAO</code>.</p>
	 */
	public void setNodeDAO(NodeDAO nodeDAO) {
		this.nodeDAO = nodeDAO;
	}

	/**
	 * <p>Getter for the field <code>patchDAO</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.domain.patch.PatchDAO} object.
	 */
	public PatchDAO getPatchDAO() {
		return patchDAO;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Setter for the field <code>patchDAO</code>.</p>
	 */
	public void setPatchDAO(PatchDAO patchDAO) {
		this.patchDAO = patchDAO;
	}

	/**
	 * <p>Getter for the field <code>qnameDAO</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.domain.qname.QNameDAO} object.
	 */
	public QNameDAO getQnameDAO() {
		return qnameDAO;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Setter for the field <code>qnameDAO</code>.</p>
	 */
	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
	}

	/**
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object.
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

}
