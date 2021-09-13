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

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Score list of project
 *
 * @author quere
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "pjt:scoreList")
public class ScoreListDataItem extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6664147076602707094L;
	private String criterion;
	private Integer weight;
	private Integer score;

	
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
	public Integer getWeight() {
		return weight;
	}

	/**
	 * <p>Setter for the field <code>weight</code>.</p>
	 *
	 * @param weight a {@link java.lang.Integer} object.
	 */
	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	/**
	 * <p>Getter for the field <code>score</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:slScore")
	public Integer getScore() {
		return score;
	}

	/**
	 * <p>Setter for the field <code>score</code>.</p>
	 *
	 * @param score a {@link java.lang.Integer} object.
	 */
	public void setScore(Integer score) {
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
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param name a {@link java.lang.String} object.
	 */
	public ScoreListDataItem(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}

	/**
	 * <p>Constructor for ScoreListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param criterion a {@link java.lang.String} object.
	 * @param weight a {@link java.lang.Integer} object.
	 * @param score a {@link java.lang.Integer} object.
	 */
	public ScoreListDataItem(NodeRef nodeRef, String criterion, Integer weight, Integer score) {
		super();
		this.nodeRef = nodeRef;
		this.criterion = criterion;
		this.weight = weight;
		this.score = score;
	}
	
	/**
	 * <p>Constructor for ScoreListDataItem.</p>
	 *
	 * @param s a {@link fr.becpg.repo.project.data.projectList.ScoreListDataItem} object.
	 */
	public ScoreListDataItem(ScoreListDataItem s) {
		super();
		this.nodeRef = s.getNodeRef();
		this.criterion = s.getCriterion();
		this.weight = s.getWeight();
		this.score = s.getScore();
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
		result = prime * result + ((criterion == null) ? 0 : criterion.hashCode());
		result = prime * result + ((score == null) ? 0 : score.hashCode());
		result = prime * result + ((weight == null) ? 0 : weight.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScoreListDataItem other = (ScoreListDataItem) obj;
		if (criterion == null) {
			if (other.criterion != null)
				return false;
		} else if (!criterion.equals(other.criterion))
			return false;
		if (score == null) {
			if (other.score != null)
				return false;
		} else if (!score.equals(other.score))
			return false;
		if (weight == null) {
			if (other.weight != null)
				return false;
		} else if (!weight.equals(other.weight))
			return false;
		return true;
	}

}
