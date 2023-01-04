package fr.becpg.test.repo.product;

import java.util.ArrayList;
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

			CompoListDataItem child1 = new CompoListDataItem(null, null, 3d, 0d, ProductUnit.kg, 0d, DeclarationType.Omit, RM);
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
			
			CompoListDataItem child1 = new CompoListDataItem(null, null, 3d, 0d, ProductUnit.kg, 0d, DeclarationType.Omit, RM);
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

			CompoListDataItem child1 = new CompoListDataItem(null, null, 3d, 0d, ProductUnit.kg, 0d, DeclarationType.Omit, SF1);
			compoList.add(child1);
			CompoListDataItem child2 = new CompoListDataItem(null, null, 3d, 0d, ProductUnit.kg, 0d, DeclarationType.Omit, SF2);
			compoList.add(child2);
			CompoListDataItem child3 = new CompoListDataItem(null, null, 3d, 0d, ProductUnit.kg, 0d, DeclarationType.Omit, SF3);
			compoList.add(child3);

			finishedProduct.getCompoListView().setCompoList(compoList);			
			
			return alfrescoRepository.save(finishedProduct).getNodeRef();
			
		});
		
		assertNotNull(FP);
		
		inWriteTx(() -> {
			
			NodeRef destNodeRef = nodeService.getPrimaryParent(RM).getParentRef();
			
			NodeRef branchNodeRef = entityVersionService.createBranch(RM, destNodeRef);
			
			entityVersionService.mergeBranch(branchNodeRef, RM, VersionType.MAJOR, "test merge", true, false);
			
			entityVersionService.impactWUsed(RM, VersionType.MAJOR, "test merge");
			
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
			
			entityVersionService.impactWUsed(RM, VersionType.MINOR, "test merge");
			
			return null;
		});
		
		checkVersion(FP, "2.0", SF1, "2.0");
		checkVersion(FP, "2.0", SF2, "2.0");
		checkVersion(FP, "2.0", SF3, null);
		checkVersion(SF1, "2.0", RM, "2.0");
		checkVersion(SF2, "2.0", RM, "2.0");
			
	}

	private void checkVersion(final NodeRef parentProduct, String parentVersion, final NodeRef componentProduct, String componentVersion) throws InterruptedException {
		
		NodeRef versionNodeRef = null;
		
		int i = 0;
		
		while (versionNodeRef == null && i < 20) {
			
			Thread.sleep(1000);
			logger.debug("waiting for version created...");
			i++;
				
			versionNodeRef = inWriteTx(() -> {
				
				VersionHistory versionHistory = versionService.getVersionHistory(parentProduct);
				
				if (versionHistory != null) {
					
					Version version = versionHistory.getVersion(parentVersion);
					
					if (version != null) {
						return VersionUtil.convertNodeRef(version.getFrozenStateNodeRef());
					}
				}
				
				return null;
				
			});
			
		}
		
		assertNotNull(versionNodeRef);
		
		String entity = entityFormatService.getEntityData(versionNodeRef);
		
		assertNotNull(entity);
		
		JSONObject jsonEntity = new JSONObject(entity);
		
		JSONArray compoList = jsonEntity.getJSONObject("entity").getJSONObject("datalists").getJSONArray("bcpg:compoList");
		
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
		
		i = 0;
		
		boolean aspectsRemoved = false;
		
		while (!aspectsRemoved && i < 20) {
			i++;
			Thread.sleep(1000);
			logger.debug("waiting for unclocking...");
			
			aspectsRemoved = inWriteTx(() -> {
				
				return !lockService.isLocked(parentProduct) &&!lockService.isLocked(componentProduct) && !nodeService.hasAspect(parentProduct, ECMModel.ASPECT_CHANGE_ORDER) && !nodeService.hasAspect(componentProduct, ECMModel.ASPECT_CHANGE_ORDER);
				
			});
		}
		
		assertTrue(aspectsRemoved);
	}
	
}
