/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.data;

import java.util.List;

import org.alfresco.service.cmr.repository.MLText;

import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.survey.data.SurveyList;

/**
 * <p>
 * FinishedProductData class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:finishedProduct")
public class FinishedProductData extends ProductData {

	private static final long serialVersionUID = -6530714937830741832L;

	public static FinishedProductData build() {
		return new FinishedProductData();
	}

	public FinishedProductData withName(String name) {
		setName(name);
		return this;
	}

	public FinishedProductData withLegalName(String legalName) {
		setLegalName(legalName);
		return this;
	}

	public FinishedProductData withLegalName(MLText legalName) {
		setLegalName(legalName);
		return this;
	}

	public FinishedProductData withUnit(ProductUnit unit) {
		setUnit(unit);
		return this;
	}

	public FinishedProductData withQty(Double qty) {
		setQty(qty);
		return this;
	}

	public FinishedProductData withDensity(Double density) {
		setDensity(density);
		return this;
	}

	public FinishedProductData withCompoList(List<CompoListDataItem> compoList) {
		getCompoListView().setCompoList(compoList);
		return this;
	}

	public FinishedProductData withPackagingList(List<PackagingListDataItem> packagingList) {
		getPackagingListView().setPackagingList(packagingList);
		return this;
	}

	public FinishedProductData withSurveyList(List<SurveyList> surveyList) {
		setSurveyList(surveyList);
		return this;
	}

	public FinishedProductData withScoreList(List<ScoreListDataItem> scoreList) {
		setScoreList(scoreList);
		return this;
	}
	
	public FinishedProductData withLabelingRuleList(List<LabelingRuleListDataItem> labelingRuleList) {
		getLabelingListView().setLabelingRuleList(labelingRuleList);
		return this;
	}
	
	public FinishedProductData addCostList(List<CostListDataItem> costList) {
		setCostList(costList);
		return this;
	}

}
