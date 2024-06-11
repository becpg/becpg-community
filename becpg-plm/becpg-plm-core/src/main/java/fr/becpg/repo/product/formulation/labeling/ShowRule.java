package fr.becpg.repo.product.formulation.labeling;

import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;

import fr.becpg.repo.helper.MLTextHelper;

class ShowRule {
	String format;
	RoundingMode roundingMode = null;
	Double threshold = null;
	String qtyFormula = null;
	Set<Locale> locales = new HashSet<>();

	/**
	 * <p>Constructor for ShowRule.</p>
	 *
	 * @param format a {@link java.lang.String} object.
	 * @param locales a {@link java.util.List} object.
	 * @param roundingMode a {@link java.math.RoundingMode} object
	 */
	public ShowRule(@Nonnull String format, RoundingMode roundingMode, List<String> locales) {
		if(format.contains("|")) {
			String[] splitted = format.split("\\|");
			this.roundingMode = roundingMode;
			this.format = splitted[0];
			if(!splitted[1].isEmpty()) {
				this.roundingMode = RoundingMode.valueOf(splitted[1]);
			} 
			if(splitted.length>2 && !splitted[2].isEmpty()) {
				threshold = Double.valueOf(splitted[2]);
			}
			if(splitted.length>3 && !splitted[3].isEmpty()) {
				qtyFormula = splitted[3];
			}
			
		} else {
			this.format = format;
		}
		

		if (locales != null) {
			for (String tmp : locales) {
				this.locales.add(MLTextHelper.parseLocale(tmp));
			}
		}
	}

	/**
	 * <p>matchLocale.</p>
	 *
	 * @param locale a {@link java.util.Locale} object.
	 * @return a boolean.
	 */
	public boolean matchLocale(Locale locale) {
		return locales.isEmpty() || locales.contains(locale);
	}
	
	/**
	 * <p>matchQty.</p>
	 *
	 * @param qtyPerc a {@link java.lang.Double} object.
	 * @return a boolean.
	 */
	public boolean matchQty(Double qtyPerc) {
		return threshold == null || qtyPerc == null || qtyPerc <= (threshold/100d);
	}

	/**
	 * <p>Getter for the field <code>threshold</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getThreshold() {
		return threshold;
	}
	
	

}
