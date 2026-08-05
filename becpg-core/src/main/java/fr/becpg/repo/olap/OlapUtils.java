/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.olap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StopWatch;

import fr.becpg.repo.olap.data.OlapContext;

/**
 * <p>OlapUtils class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class OlapUtils {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(OlapUtils.class);

	/** Grouping separators Mondrian may emit, depending on the locale. */
	private static final String GROUPING_SPACES = "    ";

	/** A number with no separator at all: `-1148`, `19`. */
	private static final Pattern PLAIN = Pattern.compile("^[-+]?\\d+$");

	/** Anything that can plausibly be a formatted number. */
	private static final Pattern NUMERIC = Pattern.compile("^[-+]?[\\d.,]+$");

	
	/**
	 * <p>Constructor for OlapUtils.</p>
	 */
	private OlapUtils() {
		//DO Nothing
	}
	/**
	 * <p>readJsonFromUrl.</p>
	 *
	 * @param url a {@link java.lang.String} object.
	 * @param olapContext a {@link fr.becpg.repo.olap.data.OlapContext} object.
	 * @return a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	public static String readJsonFromUrl(String url, OlapContext olapContext) throws IOException {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		try {
			URIBuilder builder = new URIBuilder(url);
			builder.setParameter("ticket", olapContext.getAuthToken());

			HttpGet httpget = new HttpGet(builder.build());

			try (CloseableHttpResponse response = olapContext.getSession().execute(httpget)) {
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity, "UTF-8");
			}
		} catch (URISyntaxException e) {
			logger.error(e, e);
			return null;
		} finally {
			if (logger.isDebugEnabled() && watch!=null) {
				watch.stop();
				logger.debug("Retrivied JSON String from :" + url + " in " + watch.getTotalTimeSeconds() + " seconds");
			}

		}
	}

	/**
	 * <p>sendCreateQueryPostRequest.</p>
	 *
	 * @param olapContext a {@link fr.becpg.repo.olap.data.OlapContext} object.
	 * @param postUrl a {@link java.lang.String} object.
	 * @param xml a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	public static void sendCreateQueryPostRequest(OlapContext olapContext, String postUrl, String xml) throws IOException {

		if (logger.isDebugEnabled()) {
			logger.debug("Send POST request:\n" + xml + "\n to " + postUrl);
		}
		try {
			URIBuilder builder = new URIBuilder(postUrl);
			builder.setParameter("ticket", olapContext.getAuthToken());

			HttpPost httpPost = new HttpPost(builder.build());
			HttpEntity entity = new StringEntity("xml=" + xml, "UTF-8");
			
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

			httpPost.setEntity(entity);
			try (CloseableHttpResponse response = olapContext.getSession().execute(httpPost)) {
				// keep that as we should read the response
				entity = response.getEntity();

				String ret = EntityUtils.toString(entity);

				if (logger.isDebugEnabled()) {
					logger.debug("Ret: " + ret);
				}
			}
		} catch (URISyntaxException e) {
			logger.error(e, e);
		}

	}

	/**
	 * Converts one Saiku cell, preferring its raw value: {@code value} is formatted for
	 * the connection locale, {@code properties.raw} is the measure itself. Headers carry
	 * no raw value and fall back on {@link #convert(String)}.
	 *
	 * A whole raw value is returned as a {@link java.lang.Long} so that a count keeps
	 * being published as a count by {@code retrieveDataType}.
	 *
	 * @param rawValue       {@code properties.raw}, or {@code null} when absent
	 * @param formattedValue {@code value}, the displayed one
	 * @return a {@link java.lang.Long}, a {@link java.lang.Double}, or the formatted
	 *         value unchanged when neither is a number
	 */
	public static Object convertCell(String rawValue, String formattedValue) {
		if (rawValue == null || rawValue.isEmpty()) {
			return convert(formattedValue);
		}
		try {
			double parsed = Double.parseDouble(rawValue.trim());
			if ((parsed == Math.rint(parsed)) && !Double.isInfinite(parsed) && (Math.abs(parsed) <= Long.MAX_VALUE)) {
				return Long.valueOf((long) parsed);
			}
			return Double.valueOf(parsed);
		} catch (NumberFormatException e) {
			// A raw value that is not a number is not something to guess about.
			return convert(formattedValue);
		}
	}

	/**
	 * Converts one Saiku display value — the fallback of
	 * {@link #convertCell(String, String)}, for the cells carrying no raw one.
	 *
	 * The value is formatted by Mondrian for the connection locale, so it is read
	 * rather than parsed: both separators present means the last one is the decimal
	 * one, the same separator twice means grouping, spaces always mean grouping. A
	 * single separator followed by exactly three digits stays undecidable — one and
	 * the same cellset carries {@code "1.148"} for a count and {@code "19.468"} for
	 * an average — and is read as a decimal, the historical behaviour.
	 *
	 * @param value a {@link java.lang.String} object.
	 * @return a {@link java.lang.Long}, a {@link java.lang.Double}, or the value
	 *         unchanged when it is not a number at all.
	 */
	public static Object convert(String value) {
		if (value == null || value.isEmpty()) {
			return 0L;
		}

		String trimmed = value.trim();
		// Spaces are grouping separators, never decimal ones: dropping them first
		// turns "1 148" and "1 234,56" into cases the rules below already cover.
		for (int i = 0; i < GROUPING_SPACES.length(); i++) {
			trimmed = trimmed.replace(String.valueOf(GROUPING_SPACES.charAt(i)), "");
		}

		if (trimmed.isEmpty() || !NUMERIC.matcher(trimmed).matches()) {
			return value;
		}
		if (PLAIN.matcher(trimmed).matches()) {
			try {
				return Long.parseLong(trimmed);
			} catch (NumberFormatException e) {
				// Beyond a long: keep the precision a double can offer rather than
				// handing the caller a string it will not know how to plot.
				return Double.parseDouble(trimmed);
			}
		}

		Character decimalSeparator = decimalSeparatorOf(trimmed);
		String canonical = decimalSeparator == null
				? trimmed.replace(".", "").replace(",", "")
				: stripGrouping(trimmed, decimalSeparator.charValue());

		try {
			return decimalSeparator == null ? (Object) Long.valueOf(canonical) : (Object) Double.valueOf(canonical);
		} catch (NumberFormatException e) {
			logger.debug("Unparseable OLAP cell value: " + value);
			return value;
		}
	}

	/**
	 * Which character carries the decimal point, or {@code null} when the value is
	 * a grouped integer. See {@link #convert(String)} for the rules.
	 *
	 * @param value already stripped of spaces and known to be numeric
	 * @return the decimal separator, or {@code null}
	 */
	private static Character decimalSeparatorOf(String value) {
		int lastDot = value.lastIndexOf('.');
		int lastComma = value.lastIndexOf(',');

		// Both present: the rightmost separates the decimals, the other groups.
		if ((lastDot >= 0) && (lastComma >= 0)) {
			return lastDot > lastComma ? Character.valueOf('.') : Character.valueOf(',');
		}

		char separator = lastDot >= 0 ? '.' : ',';
		int last = Math.max(lastDot, lastComma);
		if (last < 0) {
			return null;
		}
		// The same separator twice or more can only be grouping.
		if (value.indexOf(separator) != last) {
			return null;
		}

		// Exactly three digits is undecidable — see convert(). Reading it as a
		// decimal is what this code has always done, so a caller that had learnt to
		// live with it is not handed a different wrong answer today.
		return Character.valueOf(separator);
	}

	/**
	 * Removes the grouping separators and normalises the decimal one to a dot, so
	 * the result can be handed to {@link java.lang.Double#valueOf(String)}.
	 *
	 * @param value  already stripped of spaces
	 * @param decimal the decimal separator
	 * @return a canonical numeric string
	 */
	private static String stripGrouping(String value, char decimal) {
		char grouping = decimal == '.' ? ',' : '.';
		return value.replace(String.valueOf(grouping), "").replace(decimal, '.');
	}

}
