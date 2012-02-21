package fr.becpg.report.client;


public class ReportException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = 7568852518156544014L;
	
	

	public ReportException(String message) {
		super(message);
	}



	public ReportException(Exception e) {
		super(e);
	}

}
