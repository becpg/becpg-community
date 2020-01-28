package fr.becpg.repo.product.formulation.labeling;

import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import fr.becpg.repo.helper.MLTextHelper;

class ShowRule {
	String format;
	RoundingMode roundingMode = null;
	Double threshold = null;
	Set<Locale> locales = new HashSet<>();

	public ShowRule(String format, List<String> locales) {
		if(format.contains("|")) {
			String[] splitted = format.split("\\|");
			
			this.format = splitted[0];
			if(!splitted[1].isEmpty()) {
				roundingMode = RoundingMode.valueOf(splitted[1]);
			} 
			if(splitted.length>2 && !splitted[2].isEmpty()) {
				threshold = Double.valueOf(splitted[2]);
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

	public boolean matchLocale(Locale locale) {
		return locales.isEmpty() || locales.contains(locale);
	}
	
	public boolean matchQty(Double qtyPerc) {
		return threshold == null || qtyPerc == null || qtyPerc < (threshold/100d);
	}

}
