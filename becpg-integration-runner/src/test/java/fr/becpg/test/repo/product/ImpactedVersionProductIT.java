package fr.becpg.test.repo.product;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ECMModel;
import fr.becpg.repo.entity.EntityFormatService;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.test.PLMBaseTestCase;

public class ImpactedVersionProductIT extends PLMBaseTestCase {

	@Autowired
	private EntityVersionService entityVersionService;

	@Autowired
	private VersionService versionService;

	@Autowired
	private EntityFormatService entityFormatService;

	@Autowired
	private LockService lockService;

	private static final Log logger = LogFactory.getLog(ImpactedVersionProductIT.class);

	@Test
	public void testInitialVersionLabels() throws InterruptedException {

		final NodeRef RM = inWriteTx(() -> {

			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("MP version test");

			rawMaterial.setParentNodeRef(getTestFolderNodeRef());

			return alfrescoRepository.save(rawMaterial).getNodeRef();

		});

		assertNotNull(RM);

		final NodeRef SF1 = inWriteTx(() -> {

			SemiFinishedProductData semiFinished = new SemiFinishedProductData();
			semiFinished.setName("SF1 version test");

			semiFinished.setParentNodeRef(getTestFolderNodeRef());

			List<CompoListDataItem> compoList = new ArrayList<>();

			/*
			 * CompoListDataItem child1 = new CompoListDataItem(null, null, 3d, 0d,
			 * ProductUnit.kg, 0d, DeclarationType.Omit, RM);
			 */
			CompoListDataItem child1 = CompoListDataItem.build().withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(RM);
			compoList.add(child1);

			semiFinished.getCompoListView().setCompoList(compoList);

			return alfrescoRepository.save(semiFinished).getNodeRef();

		});

		assertNotNull(SF1);

		final NodeRef SF2 = inWriteTx(() -> {

			SemiFinishedProductData semiFinished = new SemiFinishedProductData();
			semiFinished.setName("SF2 version test");

			semiFinished.setParentNodeRef(getTestFolderNodeRef());

			List<CompoListDataItem> compoList = new ArrayList<>();

			/*
			 * CompoListDataItem child1 = new CompoListDataItem(null, null, 3d, 0d,
			 * ProductUnit.kg, 0d, DeclarationType.Omit, RM);
			 */
			CompoListDataItem child1 = CompoListDataItem.build().withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(RM);
			compoList.add(child1);

			semiFinished.getCompoListView().setCompoList(compoList);

			return alfrescoRepository.save(semiFinished).getNodeRef();

		});

		assertNotNull(SF2);

		final NodeRef SF3 = inWriteTx(() -> {

			SemiFinishedProductData semiFinished = new SemiFinishedProductData();
			semiFinished.setName("SF3 version test");

			semiFinished.setParentNodeRef(getTestFolderNodeRef());

			return alfrescoRepository.save(semiFinished).getNodeRef();

		});

		assertNotNull(SF3);

		final NodeRef FP = inWriteTx(() -> {

			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("FP version test");

			finishedProduct.setParentNodeRef(getTestFolderNodeRef());

			List<CompoListDataItem> compoList = new ArrayList<>();

			/*
			 * CompoListDataItem child1 = new CompoListDataItem(null, null, 3d, 0d,
			 * ProductUnit.kg, 0d, DeclarationType.Omit, SF1);
			 */
			CompoListDataItem child1 = CompoListDataItem.build().withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(SF1);
			compoList.add(child1);
			/*
			 * CompoListDataItem child2 = new CompoListDataItem(null, null, 3d, 0d,
			 * ProductUnit.kg, 0d, DeclarationType.Omit, SF2);
			 */
			CompoListDataItem child2 = CompoListDataItem.build().withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(SF2);
			compoList.add(child2);
			/*
			 * CompoListDataItem child3 = new CompoListDataItem(null, null, 3d, 0d,
			 * ProductUnit.kg, 0d, DeclarationType.Omit, SF3);
			 */
			CompoListDataItem child3 = CompoListDataItem.build().withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(SF3);
			compoList.add(child3);

			finishedProduct.getCompoListView().setCompoList(compoList);

			return alfrescoRepository.save(finishedProduct).getNodeRef();

		});

		assertNotNull(FP);

		inWriteTx(() -> {
			NodeRef destNodeRef = nodeService.getPrimaryParent(RM).getParentRef();
			NodeRef branchNodeRef = entityVersionService.createBranch(RM, destNodeRef);
			entityVersionService.mergeBranch(branchNodeRef, RM, VersionType.MAJOR, "test merge", true, false);
			entityVersionService.impactWUsed(RM, VersionType.MAJOR, "test merge", null);
			return null;
		});

		checkVersion(FP, "1.0", SF1, "1.0");
		checkVersion(FP, "1.0", SF2, "1.0");
		checkVersion(SF1, "1.0", RM, "1.0");
		checkVersion(SF2, "1.0", RM, "1.0");

		inWriteTx(() -> {

			NodeRef destNodeRef = nodeService.getPrimaryParent(RM).getParentRef();

			NodeRef branchNodeRef = entityVersionService.createBranch(RM, destNodeRef);

			entityVersionService.mergeBranch(branchNodeRef, RM, VersionType.MINOR, "test merge", true, false);

			entityVersionService.impactWUsed(RM, VersionType.MINOR, "test merge", null);

			return null;
		});

		checkVersion(FP, "2.0", SF1, "2.0");
		checkVersion(FP, "2.0", SF2, "2.0");
		checkVersion(FP, "2.0", SF3, null);
		checkVersion(SF1, "2.0", RM, "2.0");
		checkVersion(SF2, "2.0", RM, "2.0");
	}

	private void checkVersion(final NodeRef parentProduct, String parentVersion, final NodeRef componentProduct,
			String componentVersion) throws InterruptedException {

		NodeRef versionNodeRef = waitForVersion(parentProduct, parentVersion);

		assertNotNull(versionNodeRef);
		String entity = entityFormatService.getEntityData(versionNodeRef);
		assertNotNull(entity);
		JSONObject jsonEntity = new JSONObject(entity);
		JSONArray compoList = jsonEntity.getJSONObject("entity").getJSONObject("datalists")
				.getJSONArray("bcpg:compoList");

		boolean check = false;

		for (int j = 0; j < compoList.length(); j++) {
			JSONObject compoElt = compoList.getJSONObject(j);

			JSONObject compoListProduct = compoElt.getJSONObject("attributes").getJSONObject("bcpg:compoListProduct");

			if (compoListProduct.getString("id").equals(componentProduct.getId())) {
				check = true;
				if (componentVersion != null) {
					assertEquals(componentVersion, compoListProduct.getString("version"));
				} else {
					assertFalse(compoListProduct.has("vesion"));
				}
				break;
			}
		}

		assertTrue(check);

		int i = 0;

		boolean aspectsRemoved = false;

		while (!aspectsRemoved && i < 60) {
			i++;
			Thread.sleep(1000);
			logger.debug("waiting for unclocking...");

			aspectsRemoved = inWriteTx(() -> {

				return !lockService.isLocked(parentProduct) && !lockService.isLocked(componentProduct)
						&& !nodeService.hasAspect(parentProduct, ECMModel.ASPECT_CHANGE_ORDER)
						&& !nodeService.hasAspect(componentProduct, ECMModel.ASPECT_CHANGE_ORDER);

			});
		}

		assertTrue(aspectsRemoved);
	}

	private NodeRef waitForVersion(final NodeRef product, String versionLabel) throws InterruptedException {
		NodeRef versionNodeRef = null;
		int i = 0;
		while (versionNodeRef == null && i < 50) {
			Thread.sleep(1000);
			logger.debug("waiting for version created...");
			i++;
			versionNodeRef = inWriteTx(() -> {
				VersionHistory versionHistory = versionService.getVersionHistory(product);
				if (versionHistory != null) {
					Version version = versionHistory.getVersion(versionLabel);
					if (version != null) {
						return VersionUtil.convertNodeRef(version.getFrozenStateNodeRef());
					}
				}
				return null;
			});

		}
		return versionNodeRef;
	}

	@Test
	public void testEffectivityDate() throws InterruptedException {

		final NodeRef RM = inWriteTx(() -> {
			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("MP effectivity version test");
			rawMaterial.setParentNodeRef(getTestFolderNodeRef());
			return alfrescoRepository.save(rawMaterial).getNodeRef();
		});

		assertNotNull(RM);

		final NodeRef FP = inWriteTx(() -> {
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("FP effectivity version test");
			finishedProduct.setParentNodeRef(getTestFolderNodeRef());
			List<CompoListDataItem> compoList = new ArrayList<>();
			/*
			 * CompoListDataItem child1 = new CompoListDataItem(null, null, 3d, 0d,
			 * ProductUnit.kg, 0d, DeclarationType.Omit, RM);
			 */
			CompoListDataItem child1 = CompoListDataItem.build().withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(RM);
			compoList.add(child1);
			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.save(finishedProduct).getNodeRef();
		});

		assertNotNull(FP);

		Calendar calendarPlusTwoDays = Calendar.getInstance();
		calendarPlusTwoDays.add(Calendar.DATE, 2);

		inWriteTx(() -> {
			NodeRef destNodeRef = nodeService.getPrimaryParent(RM).getParentRef();
			NodeRef branchNodeRef = entityVersionService.createBranch(RM, destNodeRef);
			entityVersionService.mergeBranch(branchNodeRef, RM, VersionType.MAJOR, "test merge", true, false);
			entityVersionService.impactWUsed(RM, VersionType.MAJOR, "test merge", calendarPlusTwoDays.getTime());
			return null;
		});

		NodeRef initialVersionFP = waitForVersion(FP, "1.0");

		inReadTx(() -> {
			Date initialEffectiveDate = (Date) nodeService.getProperty(initialVersionFP,
					BeCPGModel.PROP_END_EFFECTIVITY);
			Calendar cal = Calendar.getInstance();
			cal.setTime(initialEffectiveDate);
			assertEquals(cal.get(Calendar.DAY_OF_MONTH), calendarPlusTwoDays.get(Calendar.DAY_OF_MONTH));
			return null;
		});

		waitForVersion(FP, "2.0");

		inReadTx(() -> {
			Date effectiveDate = (Date) nodeService.getProperty(FP, BeCPGModel.PROP_START_EFFECTIVITY);
			Calendar cal = Calendar.getInstance();
			cal.setTime(effectiveDate);
			assertEquals(cal.get(Calendar.DAY_OF_MONTH), calendarPlusTwoDays.get(Calendar.DAY_OF_MONTH));
			return null;
		});
	}

}
