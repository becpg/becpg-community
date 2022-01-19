package fr.becpg.repo.product.formulation.score;

import org.json.JSONObject;

public class NutriScoreFrame {

	private Double value;

	private Double lowerValue;
	
	private Double upperValue;
	
	private Integer score = 0;
	
	public NutriScoreFrame(Double value) {
		this.value = value;
	}
	
	public static NutriScoreFrame parse(Object frame) {
		
		JSONObject jsonObject = new JSONObject(frame.toString());
		if (jsonObject.has("value") && jsonObject.has("lowerValue") && jsonObject.has("upperValue") && jsonObject.has("score")) {
			NutriScoreFrame nutriScoreFrame = new NutriScoreFrame(Double.parseDouble(jsonObject.get("value").toString()));
			
			//TODO inverser
			nutriScoreFrame.setLowerValue(jsonObject.get("lowerValue").equals("-Inf") ? Double.MIN_VALUE : Double.parseDouble(jsonObject.get("lowerValue").toString()));
			nutriScoreFrame.setUpperValue(jsonObject.get("upperValue").equals("+Inf") ? Double.MAX_VALUE : Double.parseDouble(jsonObject.get("upperValue").toString()));
			nutriScoreFrame.setScore((Integer) jsonObject.get("score"));
			
			return nutriScoreFrame;
		}
		
		return null;
	}

	public Double getLowerValue() {
		return lowerValue;
	}
	
	public Double getUpperValue() {
		return upperValue;
	}
	
	public Integer getScore() {
		return score;
	}

	public Double getValue() {
		return value;
	}

	public void setLowerValue(Double lower) {
		this.lowerValue = lower;
	}
	
	public void setUpperValue(Double upper) {
		this.upperValue = upper;
	}
	
	public void setScore(Integer score) {
		this.score = score;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public JSONObject toJSON() {
		
		JSONObject json = new JSONObject();
		
		json.put("value", value);
		
		if (lowerValue != null) {
			json.put("lowerValue", lowerValue == Double.MIN_VALUE ? "-Inf" : lowerValue);
		}
		
		if (upperValue != null) {
			json.put("upperValue", upperValue == Double.MAX_VALUE ? "+Inf" : upperValue);
		}
		
		if (score != null) {
			json.put("score", score);
		}
		
		return json;
	}
	
}
