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
		result = prime * result + score;
		result = prime * result + weight;
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
		if (score != other.score)
			return false;
		if (weight != other.weight)
			return false;
		return true;
	}

}
