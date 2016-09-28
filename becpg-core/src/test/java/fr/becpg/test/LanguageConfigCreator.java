package fr.becpg.test;

import java.util.Arrays;
import java.util.Locale;

import org.springframework.util.StringUtils;

public class LanguageConfigCreator {

	public static void main(String[] args) {

		Arrays.asList(Locale.getAvailableLocales()).stream().sorted((l1, l2) -> l1.getLanguage().compareTo(l2.getLanguage())).forEach(locale ->

		{
			if ((locale.getCountry() != null) && !locale.getCountry().isEmpty()) {
				String localeCode = locale.getLanguage();

				if ((locale.getVariant() == null) || locale.getVariant().isEmpty()) {

					System.out.println("<language locale=\"" + localeCode + "\">" + locale.getDisplayLanguage(Locale.ENGLISH) + " - "
							+ locale.getDisplayCountry(Locale.ENGLISH));

				}

			}

		});

		System.out.println("");

		Arrays.asList(Locale.getAvailableLocales()).stream().sorted((l1, l2) -> l1.getLanguage().compareTo(l2.getLanguage())).forEach(locale ->

		{
			if ((locale.getCountry() == null) || locale.getCountry().isEmpty()) {
				String localeCode = locale.getLanguage();

				if ((locale.getVariant() == null) || locale.getVariant().isEmpty()) {

					System.out.println("locale.name." + localeCode + "=" + locale.getDisplayLanguage(Locale.ENGLISH) );
				}
			}

		});

		System.out.println("");

		Arrays.asList(Locale.getAvailableLocales()).stream().sorted((l1, l2) -> l1.getLanguage().compareTo(l2.getLanguage())).forEach(locale ->

		{
			if ((locale.getCountry() == null) || locale.getCountry().isEmpty()) {
				String localeCode = locale.getLanguage() ;

				if ((locale.getVariant() == null) || locale.getVariant().isEmpty()) {

					System.out.println("locale.name." + localeCode + "=" + StringUtils.capitalize(locale.getDisplayLanguage(Locale.FRENCH)) );
				}
			}

		});

	}

}
