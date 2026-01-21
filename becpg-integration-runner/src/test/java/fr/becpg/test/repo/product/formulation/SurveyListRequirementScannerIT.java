/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.test.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.sample.StandardChocolateEclairTestProduct;
import fr.becpg.repo.sample.CharactTestHelper;
import fr.becpg.repo.survey.data.SurveyListDataItem;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Integration test for SurveyListRequirementScanner
 * 
 * @author matthieu
 */
public class SurveyListRequirementScannerIT extends PLMBaseTestCase {

    private static final Log logger = LogFactory.getLog(SurveyListRequirementScannerIT.class);

    @Autowired
    protected ProductService productService;

    @Autowired
    private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

    @Autowired
    protected Repository repositoryHelper;
    

    @Test
    public void testSurveyRequirementsScanner() {
        logger.info("Starting testSurveyRequirementsScanner");

        // Create a product with surveys
        FinishedProductData product = inWriteTx(() -> {
            // Use the builder from StandardChocolateEclairTestProduct to create a product with surveys
            StandardChocolateEclairTestProduct.Builder builder = new StandardChocolateEclairTestProduct.Builder()
                    .withAlfrescoRepository(alfrescoRepository)
                    .withNodeService(nodeService)
                    .withDestFolder(getTestFolderNodeRef())
                    .withCompo(true)
                    .withSurvey(true)
                    .withSpecification(true);
            
            return builder.build().createTestProduct();
          
            
        });

        // Formulate the product which will trigger the SurveyListRequirementScanner
        inWriteTx(() -> {
            productService.formulate(product);
            return null;
        });
        
        inReadTx(() -> {
            verifySurveyRequirements(product);
            return null;
        });
    }

    @Test
    public void testSurveyRequirementsScannerAllTypes() {
        logger.info("Starting testSurveyRequirementsScannerAllTypes");

        // Create a product with surveys for comprehensive testing
        FinishedProductData product = inWriteTx(() -> {
            StandardChocolateEclairTestProduct.Builder builder = new StandardChocolateEclairTestProduct.Builder()
                    .withAlfrescoRepository(alfrescoRepository)
                    .withNodeService(nodeService)
                    .withDestFolder(getTestFolderNodeRef())
                    .withCompo(true)
                    .withSurvey(true)
                    .withSpecification(false); // We'll create our own specifications
            
            StandardChocolateEclairTestProduct testProduct = builder.build();
            FinishedProductData productData = testProduct.createTestProduct();
            
            // Set our comprehensive specifications
            productData.setProductSpecifications(createComprehensiveSpecifications());
            
            // Save the product with the new specifications
            productData = (FinishedProductData) alfrescoRepository.save(productData);
            
            return productData;
        });

        // Formulate the product which will trigger the SurveyListRequirementScanner
        inWriteTx(() -> {
            productService.formulate(product);
            return null;
        });
        
        inReadTx(() -> {
            verifyComprehensiveSurveyRequirements(product);
            return null;
        });
    }

    /**
     * Verify that requirement controls were properly generated by the SurveyListRequirementScanner
     */
    private void verifySurveyRequirements(ProductData formulatedProduct) {
        logger.info("Verifying survey requirement controls");
        
        // Ensure requirement controls were created
        Assert.assertNotNull("Requirement control list should not be null", formulatedProduct.getReqCtrlList());
        Assert.assertFalse("Requirement control list should not be empty", formulatedProduct.getReqCtrlList().isEmpty());
        
        int forbiddenCount = 0;
        int toleratedCount = 0;
        int authorizedCount = 0;
        int infoCount = 0;
        
        for (RequirementListDataItem reqCtrl : formulatedProduct.getReqCtrlList()) {
            // Check for survey requirement controls
            if (reqCtrl.getReqType() == RequirementType.Forbidden) {
                MLText mlMessage = reqCtrl.getReqMlMessage();
                if (mlMessage != null) {
                    String message = mlMessage.getDefaultValue();
                    if (message != null && 
                        (message.contains("forbidden") || 
                         message.contains("defects") ||
                         message.contains("issues"))) {
                        forbiddenCount++;
                        logger.info("Found forbidden requirement control: " + message);
                    }
                }
            } else if (reqCtrl.getReqType() == RequirementType.Tolerated) {
                MLText mlMessage = reqCtrl.getReqMlMessage();
                if (mlMessage != null) {
                    String message = mlMessage.getDefaultValue();
                    if (message != null && 
                        (message.contains("tolerated") || 
                         message.contains("defects") ||
                         message.contains("issues"))) {
                        toleratedCount++;
                        logger.info("Found tolerated requirement control: " + message);
                    }
                }
            } else if (reqCtrl.getReqType() == RequirementType.Authorized) {
                // Authorized values typically don't generate requirements unless violated
                authorizedCount++;
                logger.info("Found authorized requirement control");
            } else if (reqCtrl.getReqType() == RequirementType.Info) {
                MLText mlMessage = reqCtrl.getReqMlMessage();
                if (mlMessage != null) {
                    String message = mlMessage.getDefaultValue();
                    if (message != null && 
                        (message.contains("information") || 
                         message.contains("noted") ||
                         message.contains("info"))) {
                        infoCount++;
                        logger.info("Found info requirement control: " + message);
                    }
                }
            }
        }
        
        // We expect at least some requirement controls for different scenarios
        Assert.assertTrue("Should have at least 1 forbidden requirement control", forbiddenCount >= 1);
        logger.info("Survey requirement controls verified - Forbidden: " + forbiddenCount + 
                   ", Tolerated: " + toleratedCount + ", Authorized: " + authorizedCount + 
                   ", Info: " + infoCount);
    }
    
    /**
     * Create comprehensive specifications with all requirement types for testing
     */
    private List<ProductSpecificationData> createComprehensiveSpecifications() {
        List<ProductSpecificationData> specifications = new ArrayList<>();
        
        // Get references to survey questions and answers
        NodeRef pastryQuestionRef = CharactTestHelper.getOrCreateSurveyQuestion(nodeService, StandardChocolateEclairTestProduct.SURVEY_PASTRY_QUESTION);
        NodeRef fillingQuestionRef = CharactTestHelper.getOrCreateSurveyQuestion(nodeService, StandardChocolateEclairTestProduct.SURVEY_FILLING_QUESTION);
        NodeRef chocolateQuestionRef = CharactTestHelper.getOrCreateSurveyQuestion(nodeService, StandardChocolateEclairTestProduct.SURVEY_CHOCOLATE_QUESTION);
        
        NodeRef pastryPerfectRef = CharactTestHelper.getOrCreateSurveyQuestion(nodeService, StandardChocolateEclairTestProduct.ANSWER_PASTRY_PERFECT);
        NodeRef pastryMinorRef = CharactTestHelper.getOrCreateSurveyQuestion(nodeService, StandardChocolateEclairTestProduct.ANSWER_PASTRY_MINOR_DEFECTS);
        
        NodeRef fillingPerfectRef = CharactTestHelper.getOrCreateSurveyQuestion(nodeService, StandardChocolateEclairTestProduct.ANSWER_FILLING_PERFECT);
        NodeRef fillingMinorRef = CharactTestHelper.getOrCreateSurveyQuestion(nodeService, StandardChocolateEclairTestProduct.ANSWER_FILLING_MINOR_ISSUES);
        
        NodeRef chocolateCorrectRef = CharactTestHelper.getOrCreateSurveyQuestion(nodeService, StandardChocolateEclairTestProduct.ANSWER_CHOCOLATE_CORRECT);
        NodeRef chocolateDeviationRef = CharactTestHelper.getOrCreateSurveyQuestion(nodeService, StandardChocolateEclairTestProduct.ANSWER_CHOCOLATE_DEVIATION);
        
        // Specification 1: High Quality Standard (Forbidden type)
        ProductSpecificationData spec1 = createHighQualitySpecification(pastryQuestionRef, fillingQuestionRef, chocolateQuestionRef, 
                pastryPerfectRef, fillingPerfectRef, chocolateCorrectRef);
        specifications.add(spec1);
        
        // Specification 2: Tolerant Quality Standard (Tolerated type)
        ProductSpecificationData spec2 = createTolerantQualitySpecification(pastryQuestionRef, fillingQuestionRef, chocolateQuestionRef,
                pastryMinorRef, fillingMinorRef, chocolateDeviationRef);
        specifications.add(spec2);
        
        // Specification 3: Information Standard (Info type)
        ProductSpecificationData spec3 = createInformationSpecification(pastryQuestionRef, fillingQuestionRef, chocolateQuestionRef,
                pastryMinorRef, fillingMinorRef, chocolateDeviationRef);
        specifications.add(spec3);
        
        return specifications;
    }
    
    private ProductSpecificationData createHighQualitySpecification(NodeRef pastryQuestionRef, NodeRef fillingQuestionRef, 
            NodeRef chocolateQuestionRef, NodeRef pastryPerfectRef, NodeRef fillingPerfectRef, NodeRef chocolateCorrectRef) {
        
        ProductSpecificationData specification = new ProductSpecificationData();
        specification.setName("High Quality Standard - Forbidden Values");
        
        List<SurveyListDataItem> specSurveyList = new ArrayList<>();
        
        // Forbidden: Only perfect answers allowed
        SurveyListDataItem specQ1 = new SurveyListDataItem(pastryQuestionRef, true);
        specQ1.setChoices(List.of(pastryPerfectRef));
        specQ1.setRegulatoryType(RequirementType.Forbidden);
        MLText message1 = new MLText();
        message1.addValue(Locale.ENGLISH, "Only perfect pastry quality is allowed for this standard");
        specQ1.setRegulatoryMessage(message1);
        specSurveyList.add(specQ1);
        
        SurveyListDataItem specQ2 = new SurveyListDataItem(fillingQuestionRef, true);
        specQ2.setChoices(List.of(fillingPerfectRef));
        specQ2.setRegulatoryType(RequirementType.Forbidden);
        MLText message2 = new MLText();
        message2.addValue(Locale.ENGLISH, "Only perfect filling quality is allowed for this standard");
        specQ2.setRegulatoryMessage(message2);
        specSurveyList.add(specQ2);
        
        SurveyListDataItem specQ3 = new SurveyListDataItem(chocolateQuestionRef, true);
        specQ3.setChoices(List.of(chocolateCorrectRef));
        specQ3.setRegulatoryType(RequirementType.Forbidden);
        MLText message3 = new MLText();
        message3.addValue(Locale.ENGLISH, "Only correct chocolate glaze is allowed for this standard");
        specQ3.setRegulatoryMessage(message3);
        specSurveyList.add(specQ3);
        
        specification.setSurveyList(specSurveyList);
        return (ProductSpecificationData) alfrescoRepository.create(getTestFolderNodeRef(), specification);
    }
    
    private ProductSpecificationData createTolerantQualitySpecification(NodeRef pastryQuestionRef, NodeRef fillingQuestionRef, 
            NodeRef chocolateQuestionRef, NodeRef pastryMinorRef, NodeRef fillingMinorRef, NodeRef chocolateDeviationRef) {
        
        ProductSpecificationData specification = new ProductSpecificationData();
        specification.setName("Tolerant Quality Standard - Tolerated Values");
        
        List<SurveyListDataItem> specSurveyList = new ArrayList<>();
        
        // Tolerated: Minor defects are tolerated
        SurveyListDataItem specQ1 = new SurveyListDataItem(pastryQuestionRef, true);
        specQ1.setChoices(List.of(pastryMinorRef));
        specQ1.setRegulatoryType(RequirementType.Tolerated);
        MLText message1 = new MLText();
        message1.addValue(Locale.ENGLISH, "Minor pastry defects are tolerated for this standard");
        specQ1.setRegulatoryMessage(message1);
        specSurveyList.add(specQ1);
        
        SurveyListDataItem specQ2 = new SurveyListDataItem(fillingQuestionRef, true);
        specQ2.setChoices(List.of(fillingMinorRef));
        specQ2.setRegulatoryType(RequirementType.Tolerated);
        MLText message2 = new MLText();
        message2.addValue(Locale.ENGLISH, "Minor filling issues are tolerated for this standard");
        specQ2.setRegulatoryMessage(message2);
        specSurveyList.add(specQ2);
        
        SurveyListDataItem specQ3 = new SurveyListDataItem(chocolateQuestionRef, true);
        specQ3.setChoices(List.of(chocolateDeviationRef));
        specQ3.setRegulatoryType(RequirementType.Tolerated);
        MLText message3 = new MLText();
        message3.addValue(Locale.ENGLISH, "Minor chocolate deviations are tolerated for this standard");
        specQ3.setRegulatoryMessage(message3);
        specSurveyList.add(specQ3);
        
        specification.setSurveyList(specSurveyList);
        return (ProductSpecificationData) alfrescoRepository.create(getTestFolderNodeRef(), specification);
    }
    
    private ProductSpecificationData createInformationSpecification(NodeRef pastryQuestionRef, NodeRef fillingQuestionRef, 
            NodeRef chocolateQuestionRef, NodeRef pastryMinorRef, NodeRef fillingMinorRef, NodeRef chocolateDeviationRef) {
        
        ProductSpecificationData specification = new ProductSpecificationData();
        specification.setName("Information Standard - Info Values");
        
        List<SurveyListDataItem> specSurveyList = new ArrayList<>();
        
        // Info: Minor defects trigger informational alerts
        SurveyListDataItem specQ1 = new SurveyListDataItem(pastryQuestionRef, true);
        specQ1.setChoices(List.of(pastryMinorRef));
        specQ1.setRegulatoryType(RequirementType.Info);
        MLText message1 = new MLText();
        message1.addValue(Locale.ENGLISH, "Minor pastry defects noted for information");
        specQ1.setRegulatoryMessage(message1);
        specSurveyList.add(specQ1);
        
        SurveyListDataItem specQ2 = new SurveyListDataItem(fillingQuestionRef, true);
        specQ2.setChoices(List.of(fillingMinorRef));
        specQ2.setRegulatoryType(RequirementType.Info);
        MLText message2 = new MLText();
        message2.addValue(Locale.ENGLISH, "Minor filling issues noted for information");
        specQ2.setRegulatoryMessage(message2);
        specSurveyList.add(specQ2);
        
        SurveyListDataItem specQ3 = new SurveyListDataItem(chocolateQuestionRef, true);
        specQ3.setChoices(List.of(chocolateDeviationRef));
        specQ3.setRegulatoryType(RequirementType.Info);
        MLText message3 = new MLText();
        message3.addValue(Locale.ENGLISH, "Minor chocolate deviations noted for information");
        specQ3.setRegulatoryMessage(message3);
        specSurveyList.add(specQ3);
        
        specification.setSurveyList(specSurveyList);
        return (ProductSpecificationData) alfrescoRepository.create(getTestFolderNodeRef(), specification);
    }
    
    /**
     * Verify comprehensive survey requirements with all requirement types
     */
    private void verifyComprehensiveSurveyRequirements(ProductData formulatedProduct) {
        logger.info("Verifying comprehensive survey requirement controls");
        
        // Ensure requirement controls were created
        Assert.assertNotNull("Requirement control list should not be null", formulatedProduct.getReqCtrlList());
        Assert.assertFalse("Requirement control list should not be empty", formulatedProduct.getReqCtrlList().isEmpty());
        
        int forbiddenCount = 0;
        int toleratedCount = 0;
        int authorizedCount = 0;
        int infoCount = 0;
        
        for (RequirementListDataItem reqCtrl : formulatedProduct.getReqCtrlList()) {
            if (reqCtrl.getReqType() == RequirementType.Forbidden) {
                forbiddenCount++;
                logger.info("Found forbidden requirement: " + reqCtrl.getReqMlMessage().getDefaultValue());
            } else if (reqCtrl.getReqType() == RequirementType.Tolerated) {
                toleratedCount++;
                logger.info("Found tolerated requirement: " + reqCtrl.getReqMlMessage().getDefaultValue());
            } else if (reqCtrl.getReqType() == RequirementType.Authorized) {
                authorizedCount++;
                logger.info("Found authorized requirement: " + reqCtrl.getReqMlMessage().getDefaultValue());
            } else if (reqCtrl.getReqType() == RequirementType.Info) {
                infoCount++;
                logger.info("Found info requirement: " + reqCtrl.getReqMlMessage().getDefaultValue());
            }
        }
        
        // Verify we have requirements from all types
        Assert.assertTrue("Should have at least 1 forbidden requirement", forbiddenCount >= 1);
        Assert.assertTrue("Should have at least 1 tolerated requirement", toleratedCount >= 1);
        Assert.assertTrue("Should have at least 1 info requirement", infoCount >= 1);
        
        logger.info("Comprehensive survey requirements verified - Forbidden: " + forbiddenCount + 
                   ", Tolerated: " + toleratedCount + ", Authorized: " + authorizedCount + 
                   ", Info: " + infoCount);
    }
}
