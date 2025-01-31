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
package fr.becpg.repo.project.data.projectList;

import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.annotation.MultiLevelDataList;
import fr.becpg.repo.repository.model.AbstractManualDataItem;
import fr.becpg.repo.repository.model.CopiableDataItem;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;

/**
 * Score list of project
 *
 * @author quere
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "pjt:scoreList")
@MultiLevelDataList
public class ScoreListDataItem extends AbstractManualDataItem implements  SimpleListDataItem, SimpleCharactDataItem, CompositeDataItem<ScoreListDataItem> {

	/**
	 *
	 */
	private static final long serialVersionUID = 6664147076602707094L;
	private String criterion;
	private String range;
	private Double weight;
	private Double score;
	private NodeRef scoreCriterion;
	private String detail;

	private Integer depthLevel;

	private ScoreListDataItem parent;

	@AlfSingleAssoc
	@DataListIdentifierAttr
	@AlfQname(qname = "pjt:slScoreCriterion")
	@InternalField
	public NodeRef getScoreCriterion() {
		return scoreCriterion;
	}

	public void setScoreCriterion(NodeRef scoreCriterion) {
		this.scoreCriterion = scoreCriterion;
	}

	@AlfProp
	@AlfQname(qname = "pjt:slScoreRange")
	public String getRange() {
		return range;
	}

	@AlfProp
	@AlfQname(qname = "pjt:slScoreDetail")
	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public void setRange(String range) {
		this.range = range;
	}

	/** {@inheritDoc} */
	@Override
	@InternalField
	public NodeRef getCharactNodeRef() {
		return getScoreCriterion();
	}

	/** {@inheritDoc} */
	@Override
	public Double getValue() {
		return getScore();
	}

	/** {@inheritDoc} */
	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		setScoreCriterion(nodeRef);

	}

	/** {@inheritDoc} */
	@Override
	public void setValue(Double value) {
		setScore(value);

	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:depthLevel")
	public Integer getDepthLevel() {
		return depthLevel;
	}

	/**
	 * <p>Setter for the field <code>depthLevel</code>.</p>
	 *
	 * @param depthLevel a {@link java.lang.Integer} object.
	 */
	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:parentLevel")
	public ScoreListDataItem getParent() {
		return this.parent;
	}

	/** {@inheritDoc} */
	@Override
	public void setParent(ScoreListDataItem parent) {
		this.parent = parent;
	}

	/**
	 * <p>Getter for the field <code>criterion</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:slCriterion")
	public String getCriterion() {
		return criterion;
	}

	/**
	 * <p>Setter for the field <code>criterion</code>.</p>
	 *
	 * @param criterion a {@link java.lang.String} object.
	 */
	public void setCriterion(String criterion) {
		this.criterion = criterion;
	}

	/**
	 * <p>Getter for the field <code>weight</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:slWeight")
	public Double getWeight() {
		return weight;
	}

	/**
	 * <p>Setter for the field <code>weight</code>.</p>
	 *
	 * @param weight a {@link java.lang.Integer} object.
	 */
	public void setWeight(Double weight) {
		this.weight = weight;
	}

	/**
	 * <p>Getter for the field <code>score</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:slScore")
	public Double getScore() {
		return score;
	}

	/**
	 * <p>Setter for the field <code>score</code>.</p>
	 *
	 * @param score a {@link java.lang.Integer} object.
	 */
	public void setScore(Double score) {
		this.score = score;
	}

	/**
	 * <p>Constructor for ScoreListDataItem.</p>
	 */
	public ScoreListDataItem() {
		super();
	}

	/**
	 * <p>Constructor for ScoreListDataItem.</p>
	 *
	 * @param s a {@link fr.becpg.repo.project.data.projectList.ScoreListDataItem} object.
	 */
	public ScoreListDataItem(ScoreListDataItem s) {
		super(s);
		this.nodeRef = s.getNodeRef();
		this.criterion = s.getCriterion();
		this.weight = s.getWeight();
		this.score = s.getScore();
		this.scoreCriterion = s.getScoreCriterion();
		this.detail = s.getDetail();
		this.range = s.getRange();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ScoreListDataItem [criterion=" + criterion + ", weight=" + weight + ", score=" + score + "]";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((criterion == null) ? 0 : criterion.hashCode());
		result = (prime * result) + ((score == null) ? 0 : score.hashCode());
		return (prime * result) + ((weight == null) ? 0 : weight.hashCode());
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj) || (getClass() != obj.getClass())) {
			return false;
		}
		ScoreListDataItem other = (ScoreListDataItem) obj;
		if (!Objects.equals(criterion, other.criterion)) {
			return false;
		}
		if (!Objects.equals(score, other.score)) {
			return false;
		}
		if (!Objects.equals(weight, other.weight)) {
			return false;
		}
		return true;
	}

	public static ScoreListDataItem build() {
		return new ScoreListDataItem();
	}

	public ScoreListDataItem withParent(ScoreListDataItem parent) {
		this.parent = parent;
		return this;
	}

	@Override
	public CopiableDataItem copy() {
		return new ScoreListDataItem(this);
	}

	public ScoreListDataItem withScoreCriterion(NodeRef orCreateScoreCriteriom) {
		this.setScoreCriterion(orCreateScoreCriteriom);
		return this;
	}

	public ScoreListDataItem withWeight(Double weight) {
	    this.weight = weight;
		return this;
	}
	
	public ScoreListDataItem withScore(Double score) {
	    this.score = score;
		return this;
	}


}
