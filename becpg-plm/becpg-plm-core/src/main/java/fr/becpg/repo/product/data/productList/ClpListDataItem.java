package fr.becpg.repo.product.data.productList;

import java.util.Map;

import fr.becpg.repo.repository.model.AbstractManualDataItem;

public class ClpListDataItem  extends AbstractManualDataItem  {


    private static final long serialVersionUID = 1L;
    
	private String hazardClassCode; // Example: "Flam. Liq. 2"
    private String hazardStatement; // Example: "H225"
	private String signalWord; 
	private Double qtyPerc;
	private Map<String, Double> details; // Store casNumber / qtyPerc;
	private String regulatoryText;
	
	

    // Constructor that initializes the fields from a code
    public ClpListDataItem() {
        super();
    }

    // Parses the code to set hazardClassCode and hazardStatement
    public static ClpListDataItem fromCode(String code) {
        if ((code == null) || !code.contains(":")) {
            throw new IllegalArgumentException("Invalid code format. Expected format: '<Hazard Class>:<Hazard Statement>'");
        }

        String[] parts = code.split(":", 2);
        ClpListDataItem ret = new ClpListDataItem();
        
        ret.hazardClassCode = parts[0].trim();
        ret.hazardStatement = parts[1].trim();
        
        return ret;
    }

    // Generates a code string from hazardClassCode and hazardStatement
    public String toCode() {
        if ((hazardClassCode == null) || (hazardStatement == null)) {
            throw new IllegalStateException("Both hazardClassCode and hazardStatement must be set.");
        }
        return hazardClassCode + ":" + hazardStatement;
    }

    // Getters and setters
    public String getHazardClassCode() {
        return hazardClassCode;
    }

    public void setHazardClassCode(String hazardClassCode) {
        this.hazardClassCode = hazardClassCode;
    }

    public String getHazardStatement() {
        return hazardStatement;
    }

    public void setHazardStatement(String hazardStatement) {
        this.hazardStatement = hazardStatement;
    }
    
    

    public String getSignalWord() {
		return signalWord;
	}

	public void setSignalWord(String signalWord) {
		this.signalWord = signalWord;
	}

	public Double getQtyPerc() {
		return qtyPerc;
	}

	public void setQtyPerc(Double qtyPerc) {
		this.qtyPerc = qtyPerc;
	}

	public Map<String, Double> getDetails() {
		return details;
	}

	public void setDetails(Map<String, Double> details) {
		this.details = details;
	}

	public String getRegulatoryText() {
		return regulatoryText;
	}

	public void setRegulatoryText(String regulatoryText) {
		this.regulatoryText = regulatoryText;
	}

	// Override toString for better debugging
    @Override
    public String toString() {
        return "ClpClassification{" +
                "hazardClassCode='" + hazardClassCode + '\'' +
                ", hazardStatement='" + hazardStatement + '\'' +
                '}';
    }
	

}
