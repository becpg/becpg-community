package fr.becpg.repo.product.data.meat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>MeatContentData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class MeatContentData implements Serializable{

	private static final long serialVersionUID = 194540761026268870L;

	Double qtyPerc = 0d;
	
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

	/**
	 * <p>Constructor for MeatContentData.</p>
	 *
	 * @param meatTypeKey a {@link java.lang.String} object.
	 */
	public MeatContentData(String meatTypeKey) {
		super();

		MeatType meatType = MeatType.valueOf(meatTypeKey.split("-")[0]);
		
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

	/**
	 * <p>calculateMeatContent.</p>
	 */
	public void calculateMeatContent() {

		if(qtyPerc!=null && qtyPerc!=0d) {
			fatPerc = fatPerc * (100d /qtyPerc);
			proteinPerc = proteinPerc * (100d /qtyPerc);
			collagenPerc = collagenPerc * (100d /qtyPerc);
		}
		
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

	/**
	 * <p>Getter for the field <code>fatLimitPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getFatLimitPerc() {
		return fatLimitPerc;
	}

	/**
	 * <p>Getter for the field <code>colLimitPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getColLimitPerc() {
		return colLimitPerc;
	}

	/**
	 * <p>Getter for the field <code>maxFat</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getMaxFat() {
		return maxFat;
	}

	/**
	 * <p>Getter for the field <code>maxCol</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getMaxCol() {
		return maxCol;
	}

	/**
	 * <p>Getter for the field <code>exColPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getExColPerc() {
		return exColPerc;
	}

	/**
	 * <p>Getter for the field <code>exCTPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getExCTPerc() {
		return exCTPerc;
	}

	/**
	 * <p>Getter for the field <code>exFatPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getExFatPerc() {
		return exFatPerc;
	}

	/**
	 * <p>Getter for the field <code>meatContent</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getMeatContent() {
		return meatContent;
	}

	/**
	 * <p>Getter for the field <code>proteinPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getProteinPerc() {
		return proteinPerc;
	}

	/**
	 * <p>Setter for the field <code>proteinPerc</code>.</p>
	 *
	 * @param proteinPerc a {@link java.lang.Double} object.
	 */
	public void setProteinPerc(Double proteinPerc) {
		this.proteinPerc = proteinPerc;
	}
	
	/**
	 * <p>addProteinPerc.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 */
	public void addProteinPerc(Double value) {
		if(proteinPerc!=null) {
			proteinPerc+=value;
		} else {
			proteinPerc = value;
		}
	}

	/**
	 * <p>Getter for the field <code>collagenPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getCollagenPerc() {
		return collagenPerc;
	}
	
	/**
	 * <p>addCollagenPerc.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 */
	public void addCollagenPerc(Double value) {
		if(collagenPerc!=null) {
			collagenPerc+=value;
		} else {
			collagenPerc = value;
		}
	}

	/**
	 * <p>Setter for the field <code>collagenPerc</code>.</p>
	 *
	 * @param collagenPerc a {@link java.lang.Double} object.
	 */
	public void setCollagenPerc(Double collagenPerc) {
		this.collagenPerc = collagenPerc;
	}
	
	
	/**
	 * <p>addQtyPerc.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 */
	public void addQtyPerc(Double value) {
		if(qtyPerc!=null) {
			qtyPerc+=value;
		} else {
			qtyPerc = value;
		}
	}

	/**
	 * <p>Getter for the field <code>qtyPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getQtyPerc() {
		return qtyPerc;
	}

	/**
	 * <p>Setter for the field <code>qtyPerc</code>.</p>
	 *
	 * @param qtyPerc a {@link java.lang.Double} object.
	 */
	public void setQtyPerc(Double qtyPerc) {
		this.qtyPerc = qtyPerc;
	}

	/**
	 * <p>Getter for the field <code>fatPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getFatPerc() {
		return fatPerc;
	}

	/**
	 * <p>Setter for the field <code>fatPerc</code>.</p>
	 *
	 * @param fatPerc a {@link java.lang.Double} object.
	 */
	public void setFatPerc(Double fatPerc) {
		this.fatPerc = fatPerc;
	}
	
	/**
	 * <p>addFatPerc.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 */
	public void addFatPerc(Double value) {
		if(fatPerc!=null) {
			fatPerc+=value;
		} else {
			fatPerc = value;
		}
	}

	/**
	 * <p>merge.</p>
	 *
	 * @param meatContentData a {@link fr.becpg.repo.product.data.meat.MeatContentData} object.
	 * @param partQtyPerc a {@link java.lang.Double} object.
	 */
	public void merge(MeatContentData meatContentData, Double partQtyPerc) {
		
		if (fatPerc == null) {
			fatPerc = 0d;
		}
		if (proteinPerc == null) {
			proteinPerc = 0d;
		}
		if (collagenPerc == null) {
			collagenPerc = 0d;
		}
		if(this.qtyPerc == null) {
			this.qtyPerc = 0d;
		}
		
		if(partQtyPerc!=null) {
			if (meatContentData.getQtyPerc() != null) {
				 // 30 % SF  Dans lequels j'avais 10% Viande -> 10% * 30 /100 -> 0.3% viande dans PF
				partQtyPerc = meatContentData.getQtyPerc() * partQtyPerc / 100d;
			}
			
			this.qtyPerc += partQtyPerc;
				
			if (meatContentData.getFatPerc() != null) {
				// meatContentData.getFatPerc() est pour 100% Viande -> 0.3% 				
				fatPerc += (meatContentData.getFatPerc() * partQtyPerc) / 100d;
			}
			if (meatContentData.getProteinPerc() != null) {
				proteinPerc += (meatContentData.getProteinPerc() * partQtyPerc) / 100d;
			}
			if (meatContentData.getCollagenPerc() != null) {
				collagenPerc += (meatContentData.getCollagenPerc() * partQtyPerc) / 100d;
			}
		}

	}

	/**
	 * <p>toJsonString.</p>
	 *
	 * @param meatContentData a {@link java.util.Map} object.
	 * @return a {@link java.lang.String} object.
	 * @throws org.json.JSONException if any.
	 */
	public static String toJsonString(Map<String, MeatContentData> meatContentData) throws JSONException {
		if(meatContentData ==null || meatContentData.isEmpty()) {
			return null;
		}
		
		JSONObject ret = new JSONObject();
		for (Map.Entry<String, MeatContentData> entry : meatContentData.entrySet()) {
			ret.put(entry.getKey().toString(), entry.getValue().toJson());
		}
		return ret.toString();
	}

	private JSONObject toJson() throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put("fatPerc", fatPerc);
		ret.put("proteinPerc", proteinPerc);
		ret.put("collagenPerc", collagenPerc);
		ret.put("qtyPerc", qtyPerc);
		return ret;
	}

	/**
	 * <p>parseJsonString.</p>
	 *
	 * @param meatContentdata a {@link java.lang.String} object.
	 * @return a {@link java.util.Map} object.
	 * @throws org.json.JSONException if any.
	 */
	public static Map<String, MeatContentData> parseJsonString(String meatContentdata) throws JSONException {
		Map<String, MeatContentData> ret = new HashMap<>();
		if (meatContentdata != null) {
			JSONObject obj = new JSONObject(meatContentdata);
			for (Iterator<?> i = obj.keys(); i.hasNext();) {
				String valueKey = (String) i.next();
				ret.put(valueKey, MeatContentData.parse(valueKey, (JSONObject) obj.get(valueKey)));
			}
		}
		return ret;
	}

	private static MeatContentData parse(String meatType, JSONObject jsonObject) throws JSONException {
		MeatContentData ret = new MeatContentData(meatType);
		ret.setFatPerc(jsonObject.getDouble("fatPerc"));
		ret.setProteinPerc(jsonObject.getDouble("proteinPerc"));
		ret.setCollagenPerc(jsonObject.getDouble("collagenPerc"));
		ret.calculateMeatContent();
		//Important keep after
		ret.setQtyPerc(jsonObject.getDouble("qtyPerc"));
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((colLimitPerc == null) ? 0 : colLimitPerc.hashCode());
		result = prime * result + ((collagenPerc == null) ? 0 : collagenPerc.hashCode());
		result = prime * result + ((exCTPerc == null) ? 0 : exCTPerc.hashCode());
		result = prime * result + ((exColPerc == null) ? 0 : exColPerc.hashCode());
		result = prime * result + ((exFatPerc == null) ? 0 : exFatPerc.hashCode());
		result = prime * result + ((fatLimitPerc == null) ? 0 : fatLimitPerc.hashCode());
		result = prime * result + ((fatPerc == null) ? 0 : fatPerc.hashCode());
		result = prime * result + ((maxCol == null) ? 0 : maxCol.hashCode());
		result = prime * result + ((maxFat == null) ? 0 : maxFat.hashCode());
		result = prime * result + ((meatContent == null) ? 0 : meatContent.hashCode());
		result = prime * result + ((proteinPerc == null) ? 0 : proteinPerc.hashCode());
		result = prime * result + ((qtyPerc == null) ? 0 : qtyPerc.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MeatContentData other = (MeatContentData) obj;
		if (colLimitPerc == null) {
			if (other.colLimitPerc != null)
				return false;
		} else if (!colLimitPerc.equals(other.colLimitPerc))
			return false;
		if (collagenPerc == null) {
			if (other.collagenPerc != null)
				return false;
		} else if (!collagenPerc.equals(other.collagenPerc))
			return false;
		if (exCTPerc == null) {
			if (other.exCTPerc != null)
				return false;
		} else if (!exCTPerc.equals(other.exCTPerc))
			return false;
		if (exColPerc == null) {
			if (other.exColPerc != null)
				return false;
		} else if (!exColPerc.equals(other.exColPerc))
			return false;
		if (exFatPerc == null) {
			if (other.exFatPerc != null)
				return false;
		} else if (!exFatPerc.equals(other.exFatPerc))
			return false;
		if (fatLimitPerc == null) {
			if (other.fatLimitPerc != null)
				return false;
		} else if (!fatLimitPerc.equals(other.fatLimitPerc))
			return false;
		if (fatPerc == null) {
			if (other.fatPerc != null)
				return false;
		} else if (!fatPerc.equals(other.fatPerc))
			return false;
		if (maxCol == null) {
			if (other.maxCol != null)
				return false;
		} else if (!maxCol.equals(other.maxCol))
			return false;
		if (maxFat == null) {
			if (other.maxFat != null)
				return false;
		} else if (!maxFat.equals(other.maxFat))
			return false;
		if (meatContent == null) {
			if (other.meatContent != null)
				return false;
		} else if (!meatContent.equals(other.meatContent))
			return false;
		if (proteinPerc == null) {
			if (other.proteinPerc != null)
				return false;
		} else if (!proteinPerc.equals(other.proteinPerc))
			return false;
		if (qtyPerc == null) {
			if (other.qtyPerc != null)
				return false;
		} else if (!qtyPerc.equals(other.qtyPerc))
			return false;
		return true;
	}
	
	/**
	 * <p>isApplied.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isApplied() {
		return meatContent!=null && meatContent < 100;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "MAXFat=" + maxFat 
				+ "\nMAXCol.=" + maxCol
				+ "\n%EXCol.=" + exColPerc 
				+ "\n%EXCT=" + exCTPerc 
				+ "\n%EXFat=" + exFatPerc 
				+ "\nMeat=" + meatContent
				+ "\n%Protein=" + proteinPerc 
				+ "\n%Collagen=" + collagenPerc 
				+ "\n%Fat=" + fatPerc
				+ "\n%Qty=" + qtyPerc;
	}
	
	
	/**
	 * <p>toHTMLString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toHTMLString() {
		return "<table><th><td>MAX<sub>FAT</sub></td><td>MAX<sub>COL</sub></td><td>MEAT</td></th>"+
				"<tr><td>"+maxFat+"</td><td>"+maxCol+"</td><td>"+meatContent+"</td></tr>"
						+ "</table>";
	}

}
