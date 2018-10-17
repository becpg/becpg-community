package fr.becpg.repo.product.data.meat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class MeatContentData {

	/**
	 * The limit for the fat content is the percentage of fat permitted in a
	 * ‘meat’ mixture for a particular species as defined in Regulation (EC) o.
	 * 1196/2011. For example, the % LIMIT FAT for pork is the legal limit of
	 * fat allowed in the pork ‘meat’ mixture i.e . 30%.
	 */
	Integer fatLimitPerc;

	/**
	 * The limit for the connective tissue content, expressed as collagen, is
	 * the percentage of connective tissue permitted in a ‘meat’ mixture for a
	 * particular species as defined in Regulation (EC) No.1196/2011. For
	 * example the % LIMIT COL for pork is the legal limit of connective tissue
	 * allowed in the pork ‘meat’ mixture i.e. 25%.
	 */
	Integer colLimitPerc;

	/**
	 * The maximum fat content is the amount of fat tolerated in a ‘meat’
	 * mixture for a particular species in order for the mixture to be defined
	 * as ‘meat’ in the list of ingredients. It is expressed as a proportion of
	 * the ‘meat’ components only.
	 */
	Double maxFat;

	/**
	 * The maximum connective tissue content, expressed as collagen, is the
	 * amount of connective tissue tolerated in a meat mixture for a particular
	 * species in order for the mixture to be defined as ‘meat’ in the list of
	 * ingredients. It is expressed as collagen as a proportion of the ‘meat’
	 * mixture components only.
	 */
	Double maxCol;

	/**
	 * Excess collagen
	 */
	Double exColPerc;

	/**
	 * Corresponding excess connective tissue
	 */
	Double exCTPerc;

	/**
	 * Excess fat
	 */
	Double exFatPerc;

	/**
	 * Meat content covered by the definition of meat
	 */
	Double meatContent;

	Double proteinPerc = 0d;

	Double collagenPerc = 0d;

	Double fatPerc = 0d;

	public MeatContentData(MeatType meatType) {
		super();

		switch (meatType) {
		case Mammals:
			this.fatLimitPerc = 25;
			this.colLimitPerc = 25;
			break;
		case Porcines:
			this.fatLimitPerc = 30;
			this.colLimitPerc = 25;
			break;
		case BirdsAndRabbits:
			this.fatLimitPerc = 15;
			this.colLimitPerc = 10;
			break;
		}

	}

	public void calculateMeatContent() {

		/**
		 * Step 4: Calculate the maximum permitted connective tissue content
		 * expressed as collagen (MAX COL) using equation 1
		 */
		maxCol = (colLimitPerc * (proteinPerc - collagenPerc)) / (100d - colLimitPerc);

		/**
		 * if collagen > maxCol
		 */
		if (collagenPerc > maxCol) {
			// step 5
			exColPerc = (collagenPerc - maxCol);
			
			if(exColPerc > 0) {
				exCTPerc = exColPerc * 4.625; // i.e. 37/8
			} else {
				exCTPerc = 0d;
			}
				// Connective tissue for this species must be labelled
				// as a separate ingredient in the ingredients list (e.g.
				// Beef connective tissue)
				maxFat = (fatLimitPerc * (100 - exCTPerc - fatPerc)) / (100 - fatLimitPerc);
			
		} else {
			exColPerc = 0d;
			exCTPerc = 0d;
			// Step 6(a):
			// Calculate the maximum permitted fat
			// content (MAX FAT using equation 3(a) where no
			// excess connective tissue is present i.e. EXCOL ≤ 0:

			maxFat = (fatLimitPerc * (100 - fatPerc)) / (100 - fatLimitPerc);

		}

		// is there excess fat i.e. is % Fat greater than MAX
		// FAT (%)?

		if (fatPerc > maxFat) {
			exFatPerc = fatPerc - maxFat;
		} else {
			exFatPerc = 0d;
		}

		// Fat for this species must be labelled as a
		// separate ingredient in the ingredients list
		// (e.g. Beef fat)

		meatContent = 100 - exFatPerc - exCTPerc;

	}

	public Integer getFatLimitPerc() {
		return fatLimitPerc;
	}

	public Integer getColLimitPerc() {
		return colLimitPerc;
	}

	public Double getMaxFat() {
		return maxFat;
	}

	public Double getMaxCol() {
		return maxCol;
	}

	public Double getExColPerc() {
		return exColPerc;
	}

	public Double getExCTPerc() {
		return exCTPerc;
	}

	public Double getExFatPerc() {
		return exFatPerc;
	}

	public Double getMeatContent() {
		return meatContent;
	}

	public Double getProteinPerc() {
		return proteinPerc;
	}

	public void setProteinPerc(Double proteinPerc) {
		this.proteinPerc = proteinPerc;
	}
	
	public void addProteinPerc(Double value) {
		if(proteinPerc!=null) {
			proteinPerc+=value;
		} else {
			proteinPerc = value;
		}
	}

	public Double getCollagenPerc() {
		return collagenPerc;
	}
	

	public void addCollagenPerc(Double value) {
		if(collagenPerc!=null) {
			collagenPerc+=value;
		} else {
			collagenPerc = value;
		}
	}


	public void setCollagenPerc(Double collagenPerc) {
		this.collagenPerc = collagenPerc;
	}

	public Double getFatPerc() {
		return fatPerc;
	}

	public void setFatPerc(Double fatPerc) {
		this.fatPerc = fatPerc;
	}
	

	public void addFatPerc(Double value) {
		if(fatPerc!=null) {
			fatPerc+=value;
		} else {
			fatPerc = value;
		}
	}

	public void merge(MeatContentData meatContentData) {
		if (fatPerc == null) {
			fatPerc = 0d;
		}
		if (proteinPerc == null) {
			proteinPerc = 0d;
		}
		if (collagenPerc == null) {
			collagenPerc = 0d;
		}

		if (meatContentData.getFatPerc() != null) {
			fatPerc += meatContentData.getFatPerc();
		}
		if (meatContentData.getProteinPerc() != null) {
			proteinPerc += meatContentData.getProteinPerc();
		}
		if (meatContentData.getCollagenPerc() != null) {
			collagenPerc += meatContentData.getCollagenPerc();
		}

	}

	public static String toJsonString(Map<MeatType, MeatContentData> meatContentData) throws JSONException {
		if(meatContentData ==null || meatContentData.isEmpty()) {
			return null;
		}
		
		JSONObject ret = new JSONObject();
		for (Map.Entry<MeatType, MeatContentData> entry : meatContentData.entrySet()) {
			ret.put(entry.getKey().toString(), entry.getValue().toJson());
		}
		return ret.toString();
	}

	private JSONObject toJson() throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put("fatPerc", fatPerc);
		ret.put("proteinPerc", proteinPerc);
		ret.put("collagenPerc", collagenPerc);
		return ret;
	}

	public static Map<MeatType, MeatContentData> parseJsonString(String meatContentdata) throws JSONException {
		Map<MeatType, MeatContentData> ret = new HashMap<>();
		if (meatContentdata != null) {
			JSONObject obj = new JSONObject(meatContentdata);
			for (Iterator<?> i = obj.keys(); i.hasNext();) {
				String valueKey = (String) i.next();
				ret.put(MeatType.valueOf(valueKey), MeatContentData.parse(MeatType.valueOf(valueKey), (JSONObject) obj.get(valueKey)));
			}
		}
		return ret;
	}

	private static MeatContentData parse(MeatType meatType, JSONObject jsonObject) throws JSONException {
		MeatContentData ret = new MeatContentData(meatType);
		ret.setFatPerc(jsonObject.getDouble("fatPerc"));
		ret.setProteinPerc(jsonObject.getDouble("proteinPerc"));
		ret.setCollagenPerc(jsonObject.getDouble("collagenPerc"));
		ret.calculateMeatContent();
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((colLimitPerc == null) ? 0 : colLimitPerc.hashCode());
		result = (prime * result) + ((collagenPerc == null) ? 0 : collagenPerc.hashCode());
		result = (prime * result) + ((exCTPerc == null) ? 0 : exCTPerc.hashCode());
		result = (prime * result) + ((exColPerc == null) ? 0 : exColPerc.hashCode());
		result = (prime * result) + ((exFatPerc == null) ? 0 : exFatPerc.hashCode());
		result = (prime * result) + ((fatLimitPerc == null) ? 0 : fatLimitPerc.hashCode());
		result = (prime * result) + ((fatPerc == null) ? 0 : fatPerc.hashCode());
		result = (prime * result) + ((maxCol == null) ? 0 : maxCol.hashCode());
		result = (prime * result) + ((maxFat == null) ? 0 : maxFat.hashCode());
		result = (prime * result) + ((meatContent == null) ? 0 : meatContent.hashCode());
		result = (prime * result) + ((proteinPerc == null) ? 0 : proteinPerc.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MeatContentData other = (MeatContentData) obj;
		if (colLimitPerc == null) {
			if (other.colLimitPerc != null) {
				return false;
			}
		} else if (!colLimitPerc.equals(other.colLimitPerc)) {
			return false;
		}
		if (collagenPerc == null) {
			if (other.collagenPerc != null) {
				return false;
			}
		} else if (!collagenPerc.equals(other.collagenPerc)) {
			return false;
		}
		if (exCTPerc == null) {
			if (other.exCTPerc != null) {
				return false;
			}
		} else if (!exCTPerc.equals(other.exCTPerc)) {
			return false;
		}
		if (exColPerc == null) {
			if (other.exColPerc != null) {
				return false;
			}
		} else if (!exColPerc.equals(other.exColPerc)) {
			return false;
		}
		if (exFatPerc == null) {
			if (other.exFatPerc != null) {
				return false;
			}
		} else if (!exFatPerc.equals(other.exFatPerc)) {
			return false;
		}
		if (fatLimitPerc == null) {
			if (other.fatLimitPerc != null) {
				return false;
			}
		} else if (!fatLimitPerc.equals(other.fatLimitPerc)) {
			return false;
		}
		if (fatPerc == null) {
			if (other.fatPerc != null) {
				return false;
			}
		} else if (!fatPerc.equals(other.fatPerc)) {
			return false;
		}
		if (maxCol == null) {
			if (other.maxCol != null) {
				return false;
			}
		} else if (!maxCol.equals(other.maxCol)) {
			return false;
		}
		if (maxFat == null) {
			if (other.maxFat != null) {
				return false;
			}
		} else if (!maxFat.equals(other.maxFat)) {
			return false;
		}
		if (meatContent == null) {
			if (other.meatContent != null) {
				return false;
			}
		} else if (!meatContent.equals(other.meatContent)) {
			return false;
		}
		if (proteinPerc == null) {
			if (other.proteinPerc != null) {
				return false;
			}
		} else if (!proteinPerc.equals(other.proteinPerc)) {
			return false;
		}
		return true;
	}
	
	public boolean isApplied() {
		return meatContent!=null && meatContent < 100;
	}
	

	@Override
	public String toString() {
		return "MAXFat=" + maxFat 
				+ "\nMAX Col.=" + maxCol
				+ "\n%EXCol.=" + exColPerc 
				+ "\n%EXCT=" + exCTPerc 
				+ "\n%EXFat=" + exFatPerc 
				+ "\nMeat=" + meatContent
				+ "\n%Protein=" + proteinPerc 
				+ "\n%Collagen=" + collagenPerc 
				+ "\n%Fat=" + fatPerc;
	}
	
	
	public String toHTMLString() {
		return "<table><th><td>MAX<sub>FAT</sub></td><td>MAX<sub>COL</sub></td><td>MEAT</td></th>"+
				"<tr><td>"+maxFat+"</td><td>"+maxCol+"</td><td>"+meatContent+"</td></tr>"
						+ "</table>";
	}

}
