package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.helper.TranslateHelper;

public class ForbiddenIngListDataItem {

	NodeRef nodeRef;
	
	RequirementType reqType;
	
	String reqMessage;
	
	Double qtyPercMaxi;
	
	NullableBoolean isGMO;
	
	NullableBoolean isIonized;
	
	private List<NodeRef> ings = new ArrayList<NodeRef>();
		
	private List<NodeRef> geoOrigins = new ArrayList<NodeRef>();
		
	private List<NodeRef> bioOrigins = new ArrayList<NodeRef>();
	
	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public RequirementType getReqType() {
		return reqType;
	}

	public void setReqType(RequirementType reqType) {
		this.reqType = reqType;
	}

	public String getReqMessage() {
		return reqMessage;
	}

	public void setReqMessage(String reqMessage) {
		this.reqMessage = reqMessage;
	}

	public Double getQtyPercMaxi() {
		return qtyPercMaxi;
	}

	public void setQtyPercMaxi(Double qtyPercMaxi) {
		this.qtyPercMaxi = qtyPercMaxi;
	}

	public NullableBoolean isGMO() {
		return isGMO;
	}

	public void setIsGMO(NullableBoolean isGMO) {
		this.isGMO = isGMO;
	}

	public NullableBoolean isIonized() {
		return isIonized;
	}

	public void setIsIonized(NullableBoolean isIonized) {
		this.isIonized = isIonized;
	}

	public List<NodeRef> getIngs() {
		return ings;
	}

	public void setIngs(List<NodeRef> ings) {
		this.ings = ings;
	}

	public List<NodeRef> getGeoOrigins() {
		return geoOrigins;
	}

	public void setGeoOrigins(List<NodeRef> geoOrigins) {
		this.geoOrigins = geoOrigins;
	}

	public List<NodeRef> getBioOrigins() {
		return bioOrigins;
	}

	public void setBioOrigins(List<NodeRef> bioOrigins) {
		this.bioOrigins = bioOrigins;
	}
	
	public ForbiddenIngListDataItem(NodeRef nodeRef, RequirementType reqType, String reqMessage, Double qtyPercMaxi, NullableBoolean isGMO, NullableBoolean isIonized, List<NodeRef> ings, List<NodeRef> geoOrigins, List<NodeRef> bioOrigins)
	{
		setNodeRef(nodeRef);
		setReqType(reqType);
		setReqMessage(reqMessage);
		setQtyPercMaxi(qtyPercMaxi);
		setGeoOrigins(geoOrigins);
		setBioOrigins(bioOrigins);
		setIsGMO(isGMO);
		setIsIonized(isIonized);		
		setIngs(ings);
	}
	
	public enum NullableBoolean{
		Null,
		True,
		False;
		
		public static NullableBoolean valueOf(String value, boolean isTranslated){
			
			NullableBoolean nullableBoolean = NullableBoolean.Null;
			
			if(value!=null && !value.isEmpty()){
				if(isTranslated){
					
					if(value.equals(TranslateHelper.getTranslatedNullableBoolean(NullableBoolean.True))){
						nullableBoolean = NullableBoolean.True;
					}
					else{
						nullableBoolean = NullableBoolean.False;
					}
				}
				else{
					nullableBoolean = NullableBoolean.valueOf(value);
				}
			}
			
			return nullableBoolean;
		}
	}		
}
