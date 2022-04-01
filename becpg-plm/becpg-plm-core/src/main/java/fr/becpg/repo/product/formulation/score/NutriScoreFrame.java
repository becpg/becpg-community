package fr.becpg.repo.product.formulation.score;

import java.util.Objects;

import org.json.JSONObject;

public class NutriScoreFrame {

	public static final String SCORE_STRING = "score";

	public static final String UPPER_VALUE = "upperValue";

	public static final String LOWER_VALUE = "lowerValue";

	public static final String VALUE_STRING = "value";

	private Double value = 0d;

	private Double lowerValue = 0d;
	
	private Double upperValue = 0d;
	
	private Integer score = 0;
	
	public NutriScoreFrame(Double value) {
		this.value = value;
	}
	 
	public NutriScoreFrame() {
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

	public static NutriScoreFrame parse(Object frame) {
		
		JSONObject jsonObject = new JSONObject(frame.toString());
		if (jsonObject.has(VALUE_STRING) && jsonObject.has(LOWER_VALUE) && jsonObject.has(UPPER_VALUE) && jsonObject.has(SCORE_STRING)) {
			NutriScoreFrame nutriScoreFrame = new NutriScoreFrame(Double.parseDouble(jsonObject.get(VALUE_STRING).toString()));
			
			nutriScoreFrame.setLowerValue("-Inf".equals(jsonObject.get(LOWER_VALUE)) ? Double.MIN_VALUE : Double.parseDouble(jsonObject.get(LOWER_VALUE).toString()));
			nutriScoreFrame.setUpperValue("+Inf".equals(jsonObject.get(UPPER_VALUE)) ? Double.MAX_VALUE : Double.parseDouble(jsonObject.get(UPPER_VALUE).toString()));
			nutriScoreFrame.setScore((Integer) jsonObject.get(SCORE_STRING));
			
			return nutriScoreFrame;
		}
		
		return null;
	}

	public JSONObject toJSON() {
		
		JSONObject json = new JSONObject();
		
		json.put(VALUE_STRING, value);
		json.put(LOWER_VALUE, lowerValue == Double.MIN_VALUE ? "-Inf" : lowerValue);
		json.put(UPPER_VALUE, upperValue == Double.MAX_VALUE ? "+Inf" : upperValue);
		json.put(SCORE_STRING, score);
		
		return json;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, lowerValue, upperValue, score);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		NutriScoreFrame other = (NutriScoreFrame) obj;
		return Objects.equals(value, other.value) && Objects.equals(lowerValue, other.lowerValue)
				&& Objects.equals(upperValue, other.upperValue) && Objects.equals(score, other.score);
	}

	@Override
	public String toString() {
		return "NutriScoreFrame [value=" + value + ", lowerValue=" + lowerValue + ", upperValue=" + upperValue
				+ ", score=" + score + "]";
	}
}
