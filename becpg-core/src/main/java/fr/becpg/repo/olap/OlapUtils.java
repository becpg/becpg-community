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

	/** Grouping separators Mondrian may emit, depending on the locale. */
	private static final String GROUPING_SPACES = "    ";

	/** A number with no separator at all: `-1148`, `19`. */
	private static final Pattern PLAIN = Pattern.compile("^[-+]?\\d+$");

	/** Anything that can plausibly be a formatted number. */
	private static final Pattern NUMERIC = Pattern.compile("^[-+]?[\\d.,]+$");

	/**
	 * Converts one Saiku cell, <b>preferring its raw value</b>.
	 *
	 * <h3>Why the raw value and not the displayed one</h3>
	 *
	 * A Saiku data cell carries both, and only one of them is a number:
	 *
	 * <pre>
	 * {"value":"1,148","type":"DATA_CELL","properties":{"raw":"1148.0","position":"0:1"}}
	 * </pre>
	 *
	 * {@code value} is what a human reads — grouped for the connection locale — and
	 * parsing it is a lossy guess: {@code "1.148"} is 1 148 in one cell and 19.468
	 * really is a fraction in the next. {@code raw} is the measure itself, in Java's
	 * canonical form, and it removes the ambiguity entirely. Row and column headers
	 * carry no {@code raw}, which is correct: a caption is not a number, and the
	 * fallback returns it untouched.
	 *
	 * A whole {@code raw} ({@code "1148.0"}) is returned as a {@link java.lang.Long}
	 * rather than a {@link java.lang.Double}, so a count keeps reading as a count —
	 * `retrieveDataType` publishes that class as the column type, and turning every
	 * count into a decimal would change what charts are told about their own axes.
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
	 * Converts one Saiku <b>display</b> value — the fallback of
	 * {@link #convertCell(String, String)}, used for the cells that carry no raw
	 * one (headers) and for older payloads.
	 *
	 * <h3>Why this is not a {@code parseDouble}</h3>
	 *
	 * Saiku returns the cell's <b>display</b> value, formatted by Mondrian for the
	 * connection locale — {@code 1148} comes back as {@code "1.148"} on a locale
	 * whose grouping separator is a dot. The previous implementation handed that
	 * straight to {@code Double.parseDouble}, so every count of a thousand or more
	 * was silently divided by a thousand: measured on dev.becpg.fr, 1 148 completed
	 * tasks were reported as {@code 1.148}, and a refusal rate computed from them
	 * read 97 % instead of roughly 3.5 %.
	 *
	 * <h3>What is decided, and what cannot be</h3>
	 *
	 * Most formatted numbers are unambiguous and are now read correctly:
	 * <ul>
	 *   <li>both separators present ({@code "1.234,56"}) — the <b>last</b> one is the
	 *       decimal separator, whatever the locale;</li>
	 *   <li>the same separator more than once ({@code "1.234.567"}) — grouping;</li>
	 *   <li>a separator followed by anything other than exactly three digits
	 *       ({@code "19.4"}, {@code "0,25"}) — decimal;</li>
	 *   <li>spaces, including the non-breaking ones Mondrian uses in French
	 *       ({@code "1 148"}) — grouping.</li>
	 * </ul>
	 *
	 * One shape stays <b>undecidable</b>: a single separator followed by exactly
	 * three digits. It is left as a decimal — the historical behaviour — and this
	 * is deliberate rather than a default. The locale cannot settle it: measured on
	 * dev.becpg.fr, one and the same cellset carries {@code "1.148"} for a count of
	 * 1 148 <i>and</i> {@code "19.468"} for an average of 19.468. Same connection,
	 * same locale, same separator, two readings — Mondrian formatted them with
	 * different format strings and kept only the result. Guessing on the locale
	 * would merely move the error from counts to averages.
	 *
	 * <b>The cure is upstream</b>: Saiku's raw cell value instead of its formatted
	 * one, which means a different result formatter in {@code buildDataUrl} and the
	 * matching cellset parsing in {@code OlapServiceImpl}. That change should be
	 * written against an observed Saiku payload — a direct call answers 401, the
	 * {@code ticket} parameter not being an Alfresco login ticket — and not against
	 * a guessed field name.
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
