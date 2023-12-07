package fr.becpg.repo.product.data.constraints;

public enum RegulatoryResult {
	    PROHIBITED(2),
	    PERMITTED(1),
	    NOT_LISTED(0);

	    private final int severity;

	    RegulatoryResult(int severity) {
	        this.severity = severity;
	    }

	    public int getSeverity() {
	        return severity;
	    }
	 
	
}
