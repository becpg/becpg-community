package fr.becpg.repo.olap.data;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Provide Metadata rapport
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class OlapChartMetadata {

	private int colIndex;
	private String colType;
	private String colName;
	

	
	public OlapChartMetadata(int colIndex, String colType, String colName) {
		super();
		this.colIndex = colIndex;
		this.colType = colType;
		this.colName = colName;
	}
	public int getColIndex() {
		return colIndex;
	}
	public String getColType() {
		return colType;
	}
	public String getColName() {
		return colName;
	}
	
	public JSONObject toJSONObject() throws JSONException {
		JSONObject obj = new JSONObject();
		
		obj.put("colIndex", colIndex);
		obj.put("colType", colType);
		obj.put("colName", colName);
		
		return  obj;
	}
	
	

}
