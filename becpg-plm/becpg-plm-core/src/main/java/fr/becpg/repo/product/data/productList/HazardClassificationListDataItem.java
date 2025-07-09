package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.regulatory.RegulatoryEntityItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.AbstractManualDataItem;

/**
 * <p>HazardClassificationListDataItem class.</p>
 *
 * @author matthieu
 */
@AlfType
@AlfQname(qname = "ghs:hazardClassificationList")
public class HazardClassificationListDataItem extends AbstractManualDataItem implements RegulatoryEntityItem {

	private static final long serialVersionUID = 1L;

	public enum SignalWord {
		Danger, Warning
	}

	private String hazardClassCode; // Example: "Flam. Liq. 2"
	private NodeRef hazardStatement; // Example: "H225"
	private NodeRef pictogram; // Example: "H225"
	private String signalWord; //
	private String regulatoryText;
	private String details;

	private RequirementType regulatoryType;
	private MLText regulatoryMessage;
	private List<NodeRef> regulatoryCountriesRef = new ArrayList<>();
	private List<NodeRef> regulatoryUsagesRef = new ArrayList<>();

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>regulatoryCountriesRef</code>.</p>
	 */
	@Override
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryCountries")
	public List<NodeRef> getRegulatoryCountriesRef() {
		return regulatoryCountriesRef;
	}

	/** {@inheritDoc} */
	@Override
	public void setRegulatoryCountriesRef(List<NodeRef> regulatoryCountries) {
		this.regulatoryCountriesRef = regulatoryCountries;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>regulatoryUsagesRef</code>.</p>
	 */
	@Override
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryUsageRef")
	public List<NodeRef> getRegulatoryUsagesRef() {
		return regulatoryUsagesRef;
	}

	/** {@inheritDoc} */
	@Override
	public void setRegulatoryUsagesRef(List<NodeRef> regulatoryUsages) {
		this.regulatoryUsagesRef = regulatoryUsages;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>regulatoryType</code>.</p>
	 */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryType")
	public RequirementType getRegulatoryType() {
		return regulatoryType;
	}

	/** {@inheritDoc} */
	@Override
	public void setRegulatoryType(RequirementType regulatoryType) {
		this.regulatoryType = regulatoryType;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>regulatoryMessage</code>.</p>
	 */
	@Override
	@AlfProp
	@AlfMlText
	@AlfQname(qname = "bcpg:regulatoryText")
	public MLText getRegulatoryMessage() {
		return regulatoryMessage;
	}

	/** {@inheritDoc} */
	@Override
	public void setRegulatoryMessage(MLText regulatoryMessage) {
		this.regulatoryMessage = regulatoryMessage;
	}

	/**
	 * <p>Getter for the field <code>hazardClassCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "ghs:hazardClassCode")
	public String getHazardClassCode() {
		return hazardClassCode;
	}

	/**
	 * <p>Setter for the field <code>hazardClassCode</code>.</p>
	 *
	 * @param hazardClassCode a {@link java.lang.String} object
	 */
	public void setHazardClassCode(String hazardClassCode) {
		this.hazardClassCode = hazardClassCode;
	}

	/**
	 * <p>Getter for the field <code>hazardStatement</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "ghs:hazardStatementRef")
	public NodeRef getHazardStatement() {
		return hazardStatement;
	}

	/**
	 * <p>Setter for the field <code>hazardStatement</code>.</p>
	 *
	 * @param hazardStatement a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setHazardStatement(NodeRef hazardStatement) {
		this.hazardStatement = hazardStatement;
	}

	/**
	 * <p>Getter for the field <code>pictogram</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "ghs:pictogramRef")
	public NodeRef getPictogram() {
		return pictogram;
	}

	/**
	 * <p>Setter for the field <code>pictogram</code>.</p>
	 *
	 * @param pictogram a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setPictogram(NodeRef pictogram) {
		this.pictogram = pictogram;
	}

	/**
	 * <p>Getter for the field <code>signalWord</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "ghs:signalWord")
	public String getSignalWord() {
		return signalWord;
	}

	/**
	 * <p>Setter for the field <code>signalWord</code>.</p>
	 *
	 * @param signalWord a {@link java.lang.String} object
	 */
	public void setSignalWord(String signalWord) {
		this.signalWord = signalWord;
	}

	/**
	 * <p>Getter for the field <code>regulatoryText</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "ghs:hclRegulatoryText")
	public String getRegulatoryText() {
		return regulatoryText;
	}

	/**
	 * <p>Setter for the field <code>regulatoryText</code>.</p>
	 *
	 * @param regulatoryText a {@link java.lang.String} object
	 */
	public void setRegulatoryText(String regulatoryText) {
		this.regulatoryText = regulatoryText;
	}

	/**
	 * <p>Getter for the field <code>details</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "ghs:hclDetail")
	public String getDetails() {
		return details;
	}

	/**
	 * <p>Setter for the field <code>details</code>.</p>
	 *
	 * @param details a {@link java.lang.String} object
	 */
	public void setDetails(String details) {
		this.details = details;
	}

	/**
	 * <p>toCode.</p>
	 *
	 * @param hazardStatement a {@link java.lang.String} object
	 * @param hazardClassCode a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public static String toCode(String hazardStatement, String hazardClassCode) {
		return hazardClassCode + ":" + hazardStatement;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "HazardClassificationListDataItem [hazardClassCode=" + hazardClassCode + ", hazardStatement=" + hazardStatement + ", pictogram="
				+ pictogram + ", signalWord=" + signalWord + ", regulatoryText=" + regulatoryText + ", details=" + details + "]";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		return (prime * result) + Objects.hash(details, hazardClassCode, hazardStatement, pictogram, regulatoryText, signalWord);
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
		HazardClassificationListDataItem other = (HazardClassificationListDataItem) obj;
		return Objects.equals(details, other.details) && Objects.equals(hazardClassCode, other.hazardClassCode)
				&& Objects.equals(hazardStatement, other.hazardStatement) && Objects.equals(pictogram, other.pictogram)
				&& Objects.equals(regulatoryText, other.regulatoryText) && Objects.equals(signalWord, other.signalWord);
	}

	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.HazardClassificationListDataItem} object
	 */
	public static HazardClassificationListDataItem build() {
		return new HazardClassificationListDataItem();
	}

	/**
	 * <p>withHazardStatement.</p>
	 *
	 * @param hazardStatement a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.product.data.productList.HazardClassificationListDataItem} object
	 */
	public HazardClassificationListDataItem withHazardStatement(NodeRef hazardStatement) {
		this.hazardStatement = hazardStatement;
		return this;
	}

	/**
	 * <p>withPictogram.</p>
	 *
	 * @param pictogram a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.product.data.productList.HazardClassificationListDataItem} object
	 */
	public HazardClassificationListDataItem withPictogram(NodeRef pictogram) {
		this.pictogram = pictogram;
		return this;
	}

	/**
	 * <p>withHazardClassCode.</p>
	 *
	 * @param hazardClassCode a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.productList.HazardClassificationListDataItem} object
	 */
	public HazardClassificationListDataItem withHazardClassCode(String hazardClassCode) {
		this.hazardClassCode = hazardClassCode;
		return this;
	}

	/**
	 * <p>withSignalWord.</p>
	 *
	 * @param signalWord a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.productList.HazardClassificationListDataItem} object
	 */
	public HazardClassificationListDataItem withSignalWord(String signalWord) {
		this.signalWord = signalWord;
		return this;
	}

	/**
	 * <p>withRegulatoryText.</p>
	 *
	 * @param regulatoryText a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.productList.HazardClassificationListDataItem} object
	 */
	public HazardClassificationListDataItem withRegulatoryText(String regulatoryText) {
		this.regulatoryText = regulatoryText;
		return this;
	}

	/**
	 * <p>withRegulatoryMessage.</p>
	 *
	 * @param regulatoryMessage a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.productList.HazardClassificationListDataItem} object
	 */
	public HazardClassificationListDataItem withRegulatoryMessage(String regulatoryMessage) {
		this.regulatoryMessage = new MLText();
		this.regulatoryMessage.addValue(MLTextHelper.getNearestLocale(Locale.getDefault()), regulatoryMessage);
		return this;
	}

	/**
	 * <p>withDetail.</p>
	 *
	 * @param details a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.productList.HazardClassificationListDataItem} object
	 */
	public HazardClassificationListDataItem withDetail(String details) {
		this.details = details;
		return this;
	}

	/**
	 * <p>merge.</p>
	 *
	 * @param toMerge a {@link fr.becpg.repo.product.data.productList.HazardClassificationListDataItem} object
	 * @return a {@link fr.becpg.repo.product.data.productList.HazardClassificationListDataItem} object
	 */
	public HazardClassificationListDataItem merge(HazardClassificationListDataItem toMerge) {
		this.pictogram = toMerge.pictogram;
		this.hazardClassCode = toMerge.hazardClassCode;
		this.details = toMerge.details;
		this.signalWord = toMerge.signalWord;
		this.regulatoryText = toMerge.regulatoryText;
		return this;
	}

}
