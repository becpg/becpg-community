package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "bcpg:reqCtrlList")
public class ReqCtrlListDataItem extends BeCPGDataObject {

	
	RequirementType reqType;
	
	String reqMessage;
	
	private List<NodeRef> sources = new ArrayList<NodeRef>();

	@AlfProp
	@AlfQname(qname="bcpg:rclReqType")
	public RequirementType getReqType() {
		return reqType;
	}

	public void setReqType(RequirementType reqType) {
		this.reqType = reqType;
	}

	@AlfProp
	@AlfQname(qname="bcpg:rclReqMessage")
	public String getReqMessage() {
		return reqMessage;
	}

	public void setReqMessage(String reqMessage) {
		this.reqMessage = reqMessage;
	}

	@AlfMultiAssoc
	@AlfQname(qname="bcpg:rclSources")
	public List<NodeRef> getSources() {
		return sources;
	}

	public void setSources(List<NodeRef> sources) {
		this.sources = sources;
	}
	
	
	
	public ReqCtrlListDataItem() {
		super();
	}

	public ReqCtrlListDataItem(NodeRef nodeRef, RequirementType reqType, String reqMessage, List<NodeRef> sources){
		setNodeRef(nodeRef);
		setReqType(reqType);
		setReqMessage(reqMessage);
		setSources(sources);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((reqMessage == null) ? 0 : reqMessage.hashCode());
		result = prime * result + ((reqType == null) ? 0 : reqType.hashCode());
		result = prime * result + ((sources == null) ? 0 : sources.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReqCtrlListDataItem other = (ReqCtrlListDataItem) obj;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (reqMessage == null) {
			if (other.reqMessage != null)
				return false;
		} else if (!reqMessage.equals(other.reqMessage))
			return false;
		if (reqType != other.reqType)
			return false;
		if (sources == null) {
			if (other.sources != null)
				return false;
		} else if (!sources.equals(other.sources))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ReqCtrlListDataItem [nodeRef=" + nodeRef + ", reqType=" + reqType + ", reqMessage=" + reqMessage + ", sources=" + sources + "]";
	}
	
	
}
