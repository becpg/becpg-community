package fr.becpg.repo.product.data.productList;

import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.AbstractManualDataItem;

@AlfType
@AlfQname(qname = "ghs:hazardClassificationList")
public class HazardClassificationListDataItem extends AbstractManualDataItem {

	private static final long serialVersionUID = 1L;

	private String hazardClassCode; // Example: "Flam. Liq. 2"
	private NodeRef hazardStatement; // Example: "H225"
	private NodeRef pictogram; // Example: "H225"
	private String signalWord; //
	private String regulatoryText;
	private String details;


	@AlfProp
	@AlfQname(qname = "ghs:hazardClassCode")
	public String getHazardClassCode() {
		return hazardClassCode;
	}

	public void setHazardClassCode(String hazardClassCode) {
		this.hazardClassCode = hazardClassCode;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "ghs:hazardStatementRef")
	public NodeRef getHazardStatement() {
		return hazardStatement;
	}

	public void setHazardStatement(NodeRef hazardStatement) {
		this.hazardStatement = hazardStatement;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "ghs:pictogramRef")
	public NodeRef getPictogram() {
		return pictogram;
	}

	public void setPictogram(NodeRef pictogram) {
		this.pictogram = pictogram;
	}

	@AlfProp
	@AlfQname(qname = "ghs:signalWord")
	public String getSignalWord() {
		return signalWord;
	}

	public void setSignalWord(String signalWord) {
		this.signalWord = signalWord;
	}

	@AlfProp
	@AlfQname(qname = "ghs:hclRegulatoryText")
	public String getRegulatoryText() {
		return regulatoryText;
	}

	public void setRegulatoryText(String regulatoryText) {
		this.regulatoryText = regulatoryText;
	}

	@AlfProp
	@AlfQname(qname = "ghs:hclDetail")
	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public static String toCode(String hazardStatement, String hazardClassCode) {
		return hazardClassCode + ":" + hazardStatement;
	}

	@Override
	public String toString() {
		return "HazardClassificationListDataItem [hazardClassCode=" + hazardClassCode + ", hazardStatement=" + hazardStatement + ", pictogram="
				+ pictogram + ", signalWord=" + signalWord + ", regulatoryText=" + regulatoryText + ", details=" + details + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		return (prime * result) + Objects.hash(details, hazardClassCode, hazardStatement, pictogram, regulatoryText, signalWord);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj) || (getClass() != obj.getClass())) {
			return false;
		}
		HazardClassificationListDataItem other = (HazardClassificationListDataItem) obj;
		return Objects.equals(details, other.details) && Objects.equals(hazardClassCode, other.hazardClassCode)
				&& Objects.equals(hazardStatement, other.hazardStatement) && Objects.equals(pictogram, other.pictogram)
				&& Objects.equals(regulatoryText, other.regulatoryText) && Objects.equals(signalWord, other.signalWord);
	}

	public static HazardClassificationListDataItem build() {
		return new HazardClassificationListDataItem();
	}

	public HazardClassificationListDataItem withHazardStatement(NodeRef hazardStatement) {
		this.hazardStatement = hazardStatement;
		return this;
	}
	
	public HazardClassificationListDataItem withPictogram(NodeRef pictogram) {
		this.pictogram = pictogram;
		return this;
	}

	public HazardClassificationListDataItem withHazardClassCode(String hazardClassCode) {
		this.hazardClassCode = hazardClassCode;
		return this;
	}

	public HazardClassificationListDataItem withSignalWord(String signalWord) {
		this.signalWord = signalWord;
		return this;
	}

	public HazardClassificationListDataItem withPictogramCode(NodeRef pictogram) {
		this.pictogram = pictogram;
		return this;
	}

	public HazardClassificationListDataItem withRegulatoryText(String regulatoryText) {
		this.regulatoryText = regulatoryText;
		return this;
	}

	public HazardClassificationListDataItem withDetail(String details) {
		this.details = details;
		return this;
	}

}
