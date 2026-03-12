package fr.becpg.test.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.variant.model.VariantData;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class LabelClaimVariantIT extends AbstractFinishedProductTest {

    private NodeRef halalClaim;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        initParts();
        initClaims();
    }

    private void initClaims() {
        inWriteTx(() -> {
            Map<QName, Serializable> properties = new HashMap<>();
            properties.put(BeCPGModel.PROP_CHARACT_NAME, "Halal");
            halalClaim = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Halal"),
                    QName.createQName(BeCPGModel.BECPG_URI, "labelClaim"), properties).getChildRef();
            return null;
        });
    }

    @Test
    public void testLabelClaimWithVariants() {
        // 1. Create 2 Raw Materials with Halal claim
        final NodeRef rm1NodeRef = inWriteTx(() -> {
            RawMaterialData rm = new RawMaterialData();
            rm.setName("RM1 Halal");
            rm = (RawMaterialData) alfrescoRepository.create(getTestFolderNodeRef(), rm);
            
            List<LabelClaimListDataItem> claimList = new ArrayList<>();
            claimList.add(LabelClaimListDataItem.build().withLabelClaim(halalClaim).withIsClaimed(true).withPercentApplicable(100d).withPercentClaim(100d));
            rm.setLabelClaimList(claimList);
            alfrescoRepository.save(rm);
            return rm.getNodeRef();
        });

        final NodeRef rm2NodeRef = inWriteTx(() -> {
            RawMaterialData rm = new RawMaterialData();
            rm.setName("RM2 Halal (Variant)");
            rm = (RawMaterialData) alfrescoRepository.create(getTestFolderNodeRef(), rm);
            
            List<LabelClaimListDataItem> claimList = new ArrayList<>();
            claimList.add(LabelClaimListDataItem.build().withLabelClaim(halalClaim).withIsClaimed(true).withPercentApplicable(100d).withPercentClaim(100d));
            rm.setLabelClaimList(claimList);
            alfrescoRepository.save(rm);
            return rm.getNodeRef();
        });

        // 2. Create Finished Product with variants
        final NodeRef fpNodeRef = inWriteTx(() -> {
            FinishedProductData fp = new FinishedProductData();
            fp.setName("FP with Variants");
            fp.setUnit(ProductUnit.kg);
            fp.setQty(1d);
            fp = (FinishedProductData) alfrescoRepository.create(getTestFolderNodeRef(), fp);
            
            // Add a variant
            Map<QName, Serializable> props = new HashMap<>();
            props.put(ContentModel.PROP_NAME, "variant1");
            props.put(BeCPGModel.PROP_IS_DEFAULT_VARIANT, false);
            nodeService.createNode(fp.getNodeRef(), BeCPGModel.ASSOC_VARIANTS, BeCPGModel.ASSOC_VARIANTS, BeCPGModel.TYPE_VARIANT, props);
            
            return fp.getNodeRef();
        });

        // 3. Add composition with 2 alternative lines
        inWriteTx(() -> {
            FinishedProductData fp = (FinishedProductData) alfrescoRepository.findOne(fpNodeRef);
            List<VariantData> variants = fp.getVariants();
            NodeRef v1NodeRef = variants.get(0).getNodeRef();

            List<CompoListDataItem> compoList = new ArrayList<>();
            // Line 1: Default (no variant assigned)
            compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withProduct(rm1NodeRef).withDeclarationType(DeclarationType.Declare));
            CompoListDataItem variantLine = CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withProduct(rm2NodeRef).withDeclarationType(DeclarationType.Declare);
            variantLine.setVariants(Collections.singletonList(v1NodeRef));
            compoList.add(variantLine);
            
            fp.getCompoListView().setCompoList(compoList);
            
            // Add the claim to formulate
            List<LabelClaimListDataItem> claimList = new ArrayList<>();
            claimList.add(LabelClaimListDataItem.build().withLabelClaim(halalClaim));
            fp.setLabelClaimList(claimList);
            
            alfrescoRepository.save(fp);
            return null;
        });

        // 4. Formulate
        inWriteTx(() -> {
            productService.formulate(fpNodeRef);
            return null;
        });

        // 5. Check result
        inReadTx(() -> {
            FinishedProductData fp = (FinishedProductData) alfrescoRepository.findOne(fpNodeRef);
            LabelClaimListDataItem halal = fp.getLabelClaimList().stream().filter(c -> c.getLabelClaim().equals(halalClaim)).findFirst().orElse(null);
            
            Assert.assertNotNull("Halal claim should be present", halal);
            // Current bug: it should be 200.0 instead of 100.0 if the bug exists
            logger.info("Halal % applicable: " + halal.getPercentApplicable());
            logger.info("Halal % revendiqué: " + halal.getPercentClaim());
            
            // We expect 100.0 if fixed, or if we only take the default variant
            // If it's 200.0, then the bug is reproduced.
            Assert.assertEquals("Percent applicable should be 100.0", 100.0, halal.getPercentApplicable(), 0.01);
            Assert.assertEquals("Percent claim should be 100.0", 100.0, halal.getPercentClaim(), 0.01);
            
            return null;
        });
    }
}
