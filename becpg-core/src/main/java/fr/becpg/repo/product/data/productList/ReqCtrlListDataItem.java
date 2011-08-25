package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public class ReqCtrlListDataItem {

	NodeRef nodeRef;
	
	String reqType;
	
	String reqMessage;
	
	private List<NodeRef> sources = new ArrayList<NodeRef>();

	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	public String getReqMessage() {
		return reqMessage;
	}

	public void setReqMessage(String reqMessage) {
		this.reqMessage = reqMessage;
	}

	public List<NodeRef> getSources() {
		return sources;
	}

	public void setSources(List<NodeRef> sources) {
		this.sources = sources;
	}
	
	public ReqCtrlListDataItem(NodeRef nodeRef, String reqType, String reqMessage, List<NodeRef> sources){
		setNodeRef(nodeRef);
		setReqType(reqType);
		setReqMessage(reqMessage);
		setSources(sources);
	}
}
