/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.data;

import java.util.List;

import org.alfresco.service.cmr.repository.MLText;

import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.survey.data.SurveyListDataItem;

/**
 * <p>FinishedProductData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:finishedProduct")
public class FinishedProductData extends ProductData {

	private static final long serialVersionUID = -6530714937830741832L;

	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public static FinishedProductData build() {
		return new FinishedProductData();
	}

	/**
	 * <p>withName.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public FinishedProductData withName(String name) {
		setName(name);
		return this;
	}

	/**
	 * <p>withLegalName.</p>
	 *
	 * @param legalName a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public FinishedProductData withLegalName(String legalName) {
		setLegalName(legalName);
		return this;
	}

	/**
	 * <p>withLegalName.</p>
	 *
	 * @param legalName a {@link org.alfresco.service.cmr.repository.MLText} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public FinishedProductData withLegalName(MLText legalName) {
		setLegalName(legalName);
		return this;
	}

	/**
	 * <p>withUnit.</p>
	 *
	 * @param unit a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public FinishedProductData withUnit(ProductUnit unit) {
		setUnit(unit);
		return this;
	}
	
	
	/**
	 * <p>withUnitPrice.</p>
	 *
	 * @param unitPrice a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public FinishedProductData withUnitPrice(Double unitPrice) {
		setUnitPrice(unitPrice);
		return this;
	}


	/**
	 * <p>withQty.</p>
	 *
	 * @param qty a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public FinishedProductData withQty(Double qty) {
		setQty(qty);
		return this;
	}

	/**
	 * <p>withDensity.</p>
	 *
	 * @param density a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public FinishedProductData withDensity(Double density) {
		setDensity(density);
		return this;
	}
	

	/**
	 * <p>withServingSize.</p>
	 *
	 * @param servingSize a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public FinishedProductData withServingSize(Double servingSize) {
		setServingSize(servingSize);
		return this;
	}
	

	/**
	 * <p>withProjectedQty.</p>
	 *
	 * @param projectedQty a {@link java.lang.Long} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public FinishedProductData withProjectedQty(Long projectedQty) {
		setProjectedQty(projectedQty);
		return this;
	}


	/**
	 * <p>withCompoList.</p>
	 *
	 * @param compoList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public FinishedProductData withCompoList(List<CompoListDataItem> compoList) {
		getCompoListView().setCompoList(compoList);
		return this;
	}
	

	/**
	 * <p>withNutList.</p>
	 *
	 * @param nutList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public FinishedProductData withNutList(List<NutListDataItem> nutList) {
		setNutList(nutList);
		return this;
	}

	/**
	 * <p>withPackagingList.</p>
	 *
	 * @param packagingList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public FinishedProductData withPackagingList(List<PackagingListDataItem> packagingList) {
		getPackagingListView().setPackagingList(packagingList);
		return this;
	}

	/**
	 * <p>withSurveyList.</p>
	 *
	 * @param surveyList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public FinishedProductData withSurveyList(List<SurveyListDataItem> surveyList) {

		setSurveyList(surveyList);
		return this;
	}

	/**
	 * <p>withScoreList.</p>
	 *
	 * @param scoreList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public FinishedProductData withScoreList(List<ScoreListDataItem> scoreList) {
		setScoreList(scoreList);
		return this;
	}
	
	public FinishedProductData withLabelClaimList(List<LabelClaimListDataItem> labelClaimList) {
		setLabelClaimList(labelClaimList);
		return this;
	}

	/**
	 * <p>withLabelingRuleList.</p>
	 *
	 * @param labelingRuleList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public FinishedProductData withLabelingRuleList(List<LabelingRuleListDataItem> labelingRuleList) {
		getLabelingListView().setLabelingRuleList(labelingRuleList);
		return this;
	}

	/**
	 * <p>withClient.</p>
	 *
	 * @param clientData a {@link fr.becpg.repo.product.data.ClientData} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public FinishedProductData withClient(ClientData clientData) {
		setClients(List.of(clientData));
		return this;
	}


}
