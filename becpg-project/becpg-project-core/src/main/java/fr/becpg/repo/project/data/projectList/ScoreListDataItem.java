/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
 * 
 */
@AlfType
@AlfQname(qname = "pjt:scoreList")
public class ScoreListDataItem extends BeCPGDataObject {

	private String criterion;
	private Integer weight;
	private Integer score;

	
	@AlfProp
	@AlfQname(qname = "pjt:slCriterion")
	public String getCriterion() {
		return criterion;
	}

	public void setCriterion(String criterion) {
		this.criterion = criterion;
	}

	@AlfProp
	@AlfQname(qname = "pjt:slWeight")
	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	@AlfProp
	@AlfQname(qname = "pjt:slScore")
	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}
	
	public ScoreListDataItem() {
		super();
	}

	public ScoreListDataItem(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}

	public ScoreListDataItem(NodeRef nodeRef, String criterion, Integer weight, Integer score) {
		super();
		this.nodeRef = nodeRef;
		this.criterion = criterion;
		this.weight = weight;
		this.score = score;
	}
	
	public ScoreListDataItem(ScoreListDataItem s) {
		super();
		this.nodeRef = s.getNodeRef();
		this.criterion = s.getCriterion();
		this.weight = s.getWeight();
		this.score = s.getScore();
	}

	@Override
	public String toString() {
		return "ScoreListDataItem [criterion=" + criterion + ", weight=" + weight + ", score=" + score + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((criterion == null) ? 0 : criterion.hashCode());
		result = prime * result + ((score == null) ? 0 : score.hashCode());
		result = prime * result + ((weight == null) ? 0 : weight.hashCode());
		return result;
	}

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
