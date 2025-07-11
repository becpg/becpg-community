/*
 * Diff Match and Patch
 * Copyright 2018 The diff-match-patch Authors.
 * https://github.com/google/diff-match-patch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications copyright (C) 2018 Author
 */

package fr.becpg.common.diff;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Class containing the diff, match and patch methods.
 * Also contains the behaviour settings.
 *
 * @author matthieu
 */
public class DiffMatchPatch {

	// Defaults.
	// Set these on your DiffMatchPatch instance to override the defaults.

	/**
	 * Number of seconds to map a diff before giving up (0 for infinity).
	 */
	private static final float DIFFTIMEOUT = 1.0f;

	/**
	 * At what point is no match declared (0.0 = perfection, 1.0 = very loose).
	 */
	private static final float MATCHTRESHOLD = 0.5f;
	/**
	 * How far to search for a match (0 = exact location, 1000+ = broad match).
	 * A match this many characters away from the expected location will add
	 * 1.0 to the score (0.0 is a perfect match).
	 */
	private static final int MATCHDISTANCE = 1000;

	/**
	 * The number of bits in an int.
	 */
	private static final short MATCHMAXBITS = 32;

	//  DIFF FUNCTIONS

	private class LinesToCharsResult {
		String chars1;
		String chars2;
		List<String> lineArray;

		public LinesToCharsResult(String chars1, String chars2, List<String> lineArray) {
			this.chars1 = chars1;
			this.chars2 = chars2;
			this.lineArray = lineArray;
		}
	}

	/**
	 * The data structure representing a diff is a Linked list of Diff objects:
	 * {Diff(Operation.DELETE, "Hello"), Diff(Operation.INSERT, "Goodbye"),
	 * Diff(Operation.EQUAL, " world.")}
	 * which means: delete "Hello", add "Goodbye" and keep " world."
	 */

	/**
	 * Find the differences between two texts.
	 * Run a faster, slightly less optimal diff.
	 * This method allows the 'checklines' of diffmain() to be optional.
	 * Most of the time checklines is wanted, so default to true.
	 *
	 * @param text1 Old string to be diffed.
	 * @param text2 New string to be diffed.
	 * @return Linked List of Diff objects.
	 * @since 23.2.1.26
	 */
	public List<Diff> diffMain(String text1, String text2) {
		return diffMain(text1, text2, true);
	}

	/**
	 * Find the differences between two texts.
	 *
	 * @param text1      Old string to be diffed.
	 * @param text2      New string to be diffed.
	 * @param checklines Speedup flag.  If false, then don't run a
	 *                   line-level diff first to identify the changed areas.
	 *                   If true, then run a faster slightly less optimal diff.
	 * @return Linked List of Diff objects.
	 */
	private List<Diff> diffMain(String text1, String text2, boolean checklines) {
		// Set a deadline by which time the diff must be complete.
		long deadline = System.currentTimeMillis() + (long) (DIFFTIMEOUT * 1000);

		return diffMain(text1, text2, checklines, deadline);
	}

	/**
	 * Find the differences between two texts.  Simplifies the problem by
	 * stripping any common prefix or suffix off the texts before diffing.
	 *
	 * @param text1      Old string to be diffed.
	 * @param text2      New string to be diffed.
	 * @param checklines Speedup flag.  If false, then don't run a
	 *                   line-level diff first to identify the changed areas.
	 *                   If true, then run a faster slightly less optimal diff.
	 * @param deadline   Time when the diff should be complete by.  Used
	 *                   internally for recursive calls.  Users should set DiffTimeout instead.
	 * @return Linked List of Diff objects.
	 */
	private List<Diff> diffMain(String text1, String text2, boolean checklines, long deadline) {
		// Check for null inputs.
		if ((text1 == null) || (text2 == null)) {
			throw new IllegalArgumentException("Null inputs. (diffmain)");
		}

		// Check for equality (speedup).
		List<Diff> diffs;
		if (text1.equals(text2)) {
			diffs = new LinkedList<>();
			if (!text1.isEmpty()) {
				diffs.add(new Diff(Operation.EQUAL, text1));
			}
			return diffs;
		}

		// Trim off common prefix (speedup).
		int commonlength = diffCommonPrefix(text1, text2);
		String commonprefix = text1.substring(0, commonlength);
		text1 = text1.substring(commonlength);
		text2 = text2.substring(commonlength);

		// Trim off common suffix (speedup).
		commonlength = diffCommonSuffix(text1, text2);
		String commonsuffix = text1.substring(text1.length() - commonlength);
		text1 = text1.substring(0, text1.length() - commonlength);
		text2 = text2.substring(0, text2.length() - commonlength);

		// Compute the diff on the middle block.
		diffs = diffCompute(text1, text2, checklines, deadline);

		// Restore the prefix and suffix.
		if (!commonprefix.isEmpty()) {
			((LinkedList<Diff>) diffs).addFirst(new Diff(Operation.EQUAL, commonprefix));
		}
		if (!commonsuffix.isEmpty()) {
			((LinkedList<Diff>) diffs).addLast(new Diff(Operation.EQUAL, commonsuffix));
		}

		diffCleanupMerge(diffs);
		return diffs;
	}

	/**
	 * Find the differences between two texts.  Assumes that the texts do not
	 * have any common prefix or suffix.
	 *
	 * @param text1      Old string to be diffed.
	 * @param text2      New string to be diffed.
	 * @param checklines Speedup flag.  If false, then don't run a
	 *                   line-level diff first to identify the changed areas.
	 *                   If true, then run a faster slightly less optimal diff.
	 * @param deadline   Time when the diff should be complete by.
	 * @return Linked List of Diff objects.
	 */
	private List<Diff> diffCompute(String text1, String text2, boolean checklines, long deadline) {
		List<Diff> diffs = new LinkedList<>();

		if (text1.isEmpty()) {
			// Just add some dmp (speedup).
			diffs.add(new Diff(Operation.INSERT, text2));
			return diffs;
		}

		if (text2.isEmpty()) {
			// Just delete some dmp (speedup).
			diffs.add(new Diff(Operation.DELETE, text1));
			return diffs;
		}

		String longtext = text1.length() > text2.length() ? text1 : text2;
		String shorttext = text1.length() > text2.length() ? text2 : text1;
		int i = longtext.indexOf(shorttext);
		if (i != -1) {
			// Shorter dmp is inside the longer dmp (speedup).
			Operation op = (text1.length() > text2.length()) ? Operation.DELETE : Operation.INSERT;
			diffs.add(new Diff(op, longtext.substring(0, i)));
			diffs.add(new Diff(Operation.EQUAL, shorttext));
			diffs.add(new Diff(op, longtext.substring(i + shorttext.length())));
			return diffs;
		}

		if (shorttext.length() == 1) {
			// Single character string.
			// After the previous speedup, the character can't be an equality.
			diffs.add(new Diff(Operation.DELETE, text1));
			diffs.add(new Diff(Operation.INSERT, text2));
			return diffs;
		}

		// Check to see if the problem can be split in two.
		String[] hm = diffHalfMatch(text1, text2);
		if (hm != null) {
			// A half-match was found, sort out the return data.
			String text1a = hm[0];
			String text1b = hm[1];
			String text2a = hm[2];
			String text2b = hm[3];
			String midcommon = hm[4];
			// Send both pairs off for separate processing.
			List<Diff> diffsa = diffMain(text1a, text2a, checklines, deadline);
			List<Diff> diffsb = diffMain(text1b, text2b, checklines, deadline);
			// Merge the results.
			diffs = diffsa;
			diffs.add(new Diff(Operation.EQUAL, midcommon));
			diffs.addAll(diffsb);
			return diffs;
		}

		if (checklines && (text1.length() > 100) && (text2.length() > 100)) {
			return diffLineMode(text1, text2, deadline);
		}

		return diffBisect(text1, text2, deadline);
	}

	/**
	 * Do a quick line-level diff on both strings, then rediff the parts for
	 * greater accuracy.
	 * This speedup can produce non-minimal diffs.
	 *
	 * @param text1    Old string to be diffed.
	 * @param text2    New string to be diffed.
	 * @param deadline Time when the diff should be complete by.
	 * @return Linked List of Diff objects.
	 */
	private List<Diff> diffLineMode(String text1, String text2, long deadline) {
	    // Scan the dmp on a line-by-line basis first.
	    LinesToCharsResult b = diffLinesToChars(text1, text2);
	    text1 = b.chars1;
	    text2 = b.chars2;
	    List<String> lineArray = b.lineArray;

	    List<Diff> diffs = diffMain(text1, text2, false, deadline);

	    // Convert the diff back to original dmp.
	    diffCharsToLines(diffs, lineArray);
	    // Eliminate freak matches (e.g., blank lines)
	    diffCleanupSemantic(diffs);

	    // Rediff any replacement blocks, this time character-by-character.
	    // Add a dummy entry at the end.
	    diffs.add(new Diff(Operation.EQUAL, ""));
	    
	    int countDelete = 0;
	    int countInsert = 0;
	    StringBuilder textDelete = new StringBuilder();
	    StringBuilder textInsert = new StringBuilder();
	    
	    ListIterator<Diff> pointer = diffs.listIterator();
	    Diff thisDiff = pointer.next();

	    while (thisDiff != null) {
	        switch (thisDiff.getOperation()) {
	            case INSERT:
	                countInsert++;
	                textInsert.append(thisDiff.getText());
	                break;
	            case DELETE:
	                countDelete++;
	                textDelete.append(thisDiff.getText());
	                break;
	            case EQUAL:
	                // Upon reaching an equality, check for prior redundancies.
	                if (countDelete >= 1 && countInsert >= 1) {
	                    // Delete the offending records and add the merged ones.
	                    pointer.previous();
	                    for (int j = 0; j < (countDelete + countInsert); j++) {
	                        pointer.previous();
	                        pointer.remove();
	                    }
	                    // Merge the delete and insert diffs
	                    for (Diff newDiff : diffMain(textDelete.toString(), textInsert.toString(), false, deadline)) {
	                        pointer.add(newDiff);
	                    }
	                }
	                // Reset counters and StringBuilders
	                countInsert = 0;
	                countDelete = 0;
	                textDelete.setLength(0); // Clear the StringBuilder
	                textInsert.setLength(0);  // Clear the StringBuilder
	                break;
	        }
	        thisDiff = pointer.hasNext() ? pointer.next() : null;
	    }

	    ((LinkedList<Diff>) diffs).removeLast(); // Remove the dummy entry at the end.

	    return diffs;
	}


	/**
	 * Find the 'middle snake' of a diff, split the problem in two
	 * and return the recursively constructed diff.
	 * See Myers 1986 paper: An O(ND) Difference Algorithm and Its Variations.
	 *
	 * @param text1    Old string to be diffed.
	 * @param text2    New string to be diffed.
	 * @param deadline Time at which to bail if not yet complete.
	 * @return List of Diff objects.
	 * @since 23.2.1.26
	 */
	protected List<Diff> diffBisect(String text1, String text2, long deadline) {
		// Cache the dmp lengths to prevent multiple calls.
		int text1length = text1.length();
		int text2length = text2.length();
		int maxd = (text1length + text2length + 1) / 2;
		int voffset = maxd;
		int vlength = 2 * maxd;
		int[] v1 = new int[vlength];
		int[] v2 = new int[vlength];
		for (int x = 0; x < vlength; x++) {
			v1[x] = -1;
			v2[x] = -1;
		}
		v1[voffset + 1] = 0;
		v2[voffset + 1] = 0;
		int delta = text1length - text2length;
		// If the total number of characters is odd, then the front path will
		// collide with the reverse path.
		boolean front = ((delta % 2) != 0);
		// Offsets for start and end of k loop.
		// Prevents mapping of space beyond the grid.
		int k1start = 0;
		int k1end = 0;
		int k2start = 0;
		int k2end = 0;
		for (int d = 0; d < maxd; d++) {
			// Bail out if deadline is reached.
			if (System.currentTimeMillis() > deadline) {
				break;
			}

			// Walk the front path one step.
			for (int k1 = -d + k1start; k1 <= (d - k1end); k1 += 2) {
				int k1offset = voffset + k1;
				int x1;
				if ((k1 == -d) || ((k1 != d) && (v1[k1offset - 1] < v1[k1offset + 1]))) {
					x1 = v1[k1offset + 1];
				} else {
					x1 = v1[k1offset - 1] + 1;
				}
				int y1 = x1 - k1;
				while ((x1 < text1length) && (y1 < text2length) && (text1.charAt(x1) == text2.charAt(y1))) {
					x1++;
					y1++;
				}
				v1[k1offset] = x1;
				if (x1 > text1length) {
					// Ran off the right of the graph.
					k1end += 2;
				} else if (y1 > text2length) {
					// Ran off the bottom of the graph.
					k1start += 2;
				} else if (front) {
					int k2offset = (voffset + delta) - k1;
					if ((k2offset >= 0) && (k2offset < vlength) && (v2[k2offset] != -1)) {
						// Mirror x2 onto top-left coordinate system.
						int x2 = text1length - v2[k2offset];
						if (x1 >= x2) {
							// Overlap detected.
							return diffBisectSplit(text1, text2, x1, y1, deadline);
						}
					}
				}
			}

			// Walk the reverse path one step.
			for (int k2 = -d + k2start; k2 <= (d - k2end); k2 += 2) {
				int k2offset = voffset + k2;
				int x2;
				if ((k2 == -d) || ((k2 != d) && (v2[k2offset - 1] < v2[k2offset + 1]))) {
					x2 = v2[k2offset + 1];
				} else {
					x2 = v2[k2offset - 1] + 1;
				}
				int y2 = x2 - k2;
				while ((x2 < text1length) && (y2 < text2length) && (text1.charAt(text1length - x2 - 1) == text2.charAt(text2length - y2 - 1))) {
					x2++;
					y2++;
				}
				v2[k2offset] = x2;
				if (x2 > text1length) {
					// Ran off the left of the graph.
					k2end += 2;
				} else if (y2 > text2length) {
					// Ran off the top of the graph.
					k2start += 2;
				} else if (!front) {
					int k1offset = (voffset + delta) - k2;
					if ((k1offset >= 0) && (k1offset < vlength) && (v1[k1offset] != -1)) {
						int x1 = v1[k1offset];
						int y1 = (voffset + x1) - k1offset;
						// Mirror x2 onto top-left coordinate system.
						x2 = text1length - x2;
						if (x1 >= x2) {
							// Overlap detected.
							return diffBisectSplit(text1, text2, x1, y1, deadline);
						}
					}
				}
			}
		}
		// Diff took too long and hit the deadline or
		// number of diffs equals number of characters, no commonality at all.
		List<Diff> diffs = new LinkedList<>();
		diffs.add(new Diff(Operation.DELETE, text1));
		diffs.add(new Diff(Operation.INSERT, text2));
		return diffs;
	}

	/**
	 * Given the location of the 'middle snake', split the diff in two parts
	 * and recurse.
	 *
	 * @param text1    Old string to be diffed.
	 * @param text2    New string to be diffed.
	 * @param x        Index of split point in text1.
	 * @param y        Index of split point in text2.
	 * @param deadline Time at which to bail if not yet complete.
	 * @return List of Diff objects.
	 */
	private List<Diff> diffBisectSplit(String text1, String text2, int x, int y, long deadline) {
		String text1a = text1.substring(0, x);
		String text2a = text2.substring(0, y);
		String text1b = text1.substring(x);
		String text2b = text2.substring(y);

		// Compute both diffs serially.
		List<Diff> diffs = diffMain(text1a, text2a, false, deadline);
		List<Diff> diffsb = diffMain(text1b, text2b, false, deadline);

		diffs.addAll(diffsb);
		return diffs;
	}

	/**
	 * Split two texts into a list of strings.  Reduce the texts to a string of
	 * hashes where each Unicode character represents one line.
	 *
	 * @param text1 First string.
	 * @param text2 Second string.
	 * @return An object containing the encoded text1, the encoded text2 and
	 * the List of unique strings.  The zeroth element of the List of
	 * unique strings is intentionally blank.
	 */
	private LinesToCharsResult diffLinesToChars(String text1, String text2) {
		List<String> lineArray = new LinkedList<>();
		Map<String, Integer> lineHash = new HashMap<>();
		// e.g. linearray[4] == "Hello\n"
		// e.g. linehash.get("Hello\n") == 4

		// "\x00" is a valid character, but various debuggers don't like it.
		// So we'll insert a junk entry to avoid generating a null character.
		lineArray.add("");

		String chars1 = diffLinesToCharsMunge(text1, lineArray, lineHash);
		String chars2 = diffLinesToCharsMunge(text2, lineArray, lineHash);
		return new LinesToCharsResult(chars1, chars2, lineArray);
	}

	/**
	 * Split a dmp into a list of strings.  Reduce the texts to a string of
	 * hashes where each Unicode character represents one line.
	 *
	 * @param text      String to encode.
	 * @param lineArray List of unique strings.
	 * @param lineHash  Map of strings to indices.
	 * @return Encoded string.
	 */
	private String diffLinesToCharsMunge(String text, List<String> lineArray, Map<String, Integer> lineHash) {
		int lineStart = 0;
		int lineEnd = -1;
		String line;
		StringBuilder chars = new StringBuilder();
		// Walk the dmp, pulling out a substring for each line.
		// dmp.split('\n') would would temporarily double our memory footprint.
		// Modifying dmp would create many large strings to garbage collect.
		while (lineEnd < (text.length() - 1)) {
			lineEnd = text.indexOf('\n', lineStart);
			if (lineEnd == -1) {
				lineEnd = text.length() - 1;
			}
			line = text.substring(lineStart, lineEnd + 1);
			lineStart = lineEnd + 1;

			if (lineHash.containsKey(line)) {
				chars.append(String.valueOf((char) (int) lineHash.get(line)));
			} else {
				lineArray.add(line);
				lineHash.put(line, lineArray.size() - 1);
				chars.append(String.valueOf((char) (lineArray.size() - 1)));
			}
		}
		return chars.toString();
	}

	/**
	 * Rehydrate the dmp in a diff from a string of line hashes to real lines of
	 * dmp.
	 *
	 * @param diffs     List of Diff objects.
	 * @param lineArray List of unique strings.
	 */
	private void diffCharsToLines(List<Diff> diffs, List<String> lineArray) {
		StringBuilder text;
		for (Diff diff : diffs) {
			text = new StringBuilder();
			for (int y = 0; y < diff.getText().length(); y++) {
				text.append(lineArray.get(diff.getText().charAt(y)));
			}
			diff.setText(text.toString());
		}
	}

	/**
	 * Determine the common prefix of two strings
	 *
	 * @param text1 First string.
	 * @param text2 Second string.
	 * @return The number of characters common to the start of each string.
	 */
	private int diffCommonPrefix(String text1, String text2) {
		// Performance analysis: http://neil.fraser.name/news/2007/10/09/
		int n = Math.min(text1.length(), text2.length());
		for (int i = 0; i < n; i++) {
			if (text1.charAt(i) != text2.charAt(i)) {
				return i;
			}
		}
		return n;
	}

	/**
	 * Determine the common suffix of two strings
	 *
	 * @param text1 First string.
	 * @param text2 Second string.
	 * @return The number of characters common to the end of each string.
	 */
	private int diffCommonSuffix(String text1, String text2) {
		// Performance analysis: http://neil.fraser.name/news/2007/10/09/
		int text1length = text1.length();
		int text2length = text2.length();
		int n = Math.min(text1length, text2length);
		for (int i = 1; i <= n; i++) {
			if (text1.charAt(text1length - i) != text2.charAt(text2length - i)) {
				return i - 1;
			}
		}
		return n;
	}

	/**
	 * Determine if the suffix of one string is the prefix of another.
	 *
	 * @param text1 First string.
	 * @param text2 Second string.
	 * @return The number of characters common to the end of the first
	 * string and the start of the second string.
	 * @since 23.2.1.26
	 */
	protected int diffCommonOverlap(String text1, String text2) {
		// Cache the dmp lengths to prevent multiple calls.
		int text1length = text1.length();
		int text2length = text2.length();
		// Eliminate the null case.
		if ((text1length == 0) || (text2length == 0)) {
			return 0;
		}
		// Truncate the longer string.
		if (text1length > text2length) {
			text1 = text1.substring(text1length - text2length);
		} else if (text1length < text2length) {
			text2 = text2.substring(0, text1length);
		}
		int textlength = Math.min(text1length, text2length);
		// Quick check for the worst case.
		if (text1.equals(text2)) {
			return textlength;
		}

		// Start by looking for a single character match
		// and increase length until no match is found.
		// Performance analysis: http://neil.fraser.name/news/2010/11/04/
		int best = 0;
		int length = 1;
		while (true) {
			String pattern = text1.substring(textlength - length);
			int found = text2.indexOf(pattern);
			if (found == -1) {
				return best;
			}
			length += found;
			if ((found == 0) || text1.substring(textlength - length).equals(text2.substring(0, length))) {
				best = length;
				length++;
			}
		}
	}

	/**
	 * Do the two texts share a substring which is at least half the length of
	 * the longer dmp?
	 * This speedup can produce non-minimal diffs.
	 *
	 * @param text1 First string.
	 * @param text2 Second string.
	 * @return Five element String array, containing the prefix of text1, the
	 * suffix of text1, the prefix of text2, the suffix of text2 and the
	 * common middle.  Or null if there was no match.
	 * @since 23.2.1.26
	 */
	protected String[] diffHalfMatch(String text1, String text2) {
		String longtext = text1.length() > text2.length() ? text1 : text2;
		String shorttext = text1.length() > text2.length() ? text2 : text1;
		if ((longtext.length() < 4) || ((shorttext.length() * 2) < longtext.length())) {
			return null; // Pointless.
		}

		// First check if the second quarter is the seed for a half-match.
		String[] hm1 = diffHalfMatchI(longtext, shorttext, (longtext.length() + 3) / 4);
		// Check again based on the third quarter.
		String[] hm2 = diffHalfMatchI(longtext, shorttext, (longtext.length() + 1) / 2);
		String[] hm;
		if ((hm1 == null) && (hm2 == null)) {
			return null;
		} else if (hm2 == null) {
			hm = hm1;
		} else if (hm1 == null) {
			hm = hm2;
		} else {
			// Both matched.  Select the longest.
			hm = hm1[4].length() > hm2[4].length() ? hm1 : hm2;
		}

		// A half-match was found, sort out the return data.
		if (hm != null) {
			if (text1.length() > text2.length()) {
				return hm;
			} else {
				return new String[] { hm[2], hm[3], hm[0], hm[1], hm[4] };
			}
		}
		return null;
	}

	/**
	 * Does a substring of shorttext exist within longtext such that the
	 * substring is at least half the length of longtext?
	 *
	 * @param longtext  Longer string.
	 * @param shorttext Shorter string.
	 * @param i         Start index of quarter length substring within longtext.
	 * @return Five element String array, containing the prefix of longtext, the
	 * suffix of longtext, the prefix of shorttext, the suffix of shorttext
	 * and the common middle.  Or null if there was no match.
	 */
	private String[] diffHalfMatchI(String longtext, String shorttext, int i) {
		// Start with a 1/4 length substring at position i as a seed.
		String seed = longtext.substring(i, i + (longtext.length() / 4));
		int j = -1;
		String bestcommon = "";
		String bestlongtexta = "";
		String bestlongtextb = "";
		String bestshorttexta = "";
		String bestshorttextb = "";
		while ((j = shorttext.indexOf(seed, j + 1)) != -1) {
			int prefixLength = diffCommonPrefix(longtext.substring(i), shorttext.substring(j));
			int suffixLength = diffCommonSuffix(longtext.substring(0, i), shorttext.substring(0, j));
			if (bestcommon.length() < (suffixLength + prefixLength)) {
				bestcommon = shorttext.substring(j - suffixLength, j) + shorttext.substring(j, j + prefixLength);
				bestlongtexta = longtext.substring(0, i - suffixLength);
				bestlongtextb = longtext.substring(i + prefixLength);
				bestshorttexta = shorttext.substring(0, j - suffixLength);
				bestshorttextb = shorttext.substring(j + prefixLength);
			}
		}
		if ((bestcommon.length() * 2) >= longtext.length()) {
			return new String[] { bestlongtexta, bestlongtextb, bestshorttexta, bestshorttextb, bestcommon };
		} else {
			return null;
		}
	}

	/**
	 * Reduce the number of edits by eliminating semantically trivial equalities.
	 *
	 * @param diffs List of Diff objects.
	 */
	private void diffCleanupSemantic(List<Diff> diffs) {
		if (diffs.isEmpty()) {
			return;
		}
		boolean changes = false;
		Stack<Diff> equalities = new Stack<>(); // Stack of qualities.
		String lastequality = null; // Always equal to equalities.lastElement().dmp
		ListIterator<Diff> pointer = diffs.listIterator();
		// Number of characters that changed prior to the equality.
		int lengthinsertions1 = 0;
		int lengthdeletions1 = 0;
		// Number of characters that changed after the equality.
		int lengthinsertions2 = 0;
		int lengthdeletions2 = 0;
		Diff thisDiff = pointer.next();
		while (thisDiff != null) {
			if (thisDiff.getOperation() == Operation.EQUAL) {
				// Equality found.
				equalities.push(thisDiff);
				lengthinsertions1 = lengthinsertions2;
				lengthdeletions1 = lengthdeletions2;
				lengthinsertions2 = 0;
				lengthdeletions2 = 0;
				lastequality = thisDiff.getText();
			} else {
				// An insertion or deletion.
				if (thisDiff.getOperation() == Operation.INSERT) {
					lengthinsertions2 += thisDiff.getText().length();
				} else {
					lengthdeletions2 += thisDiff.getText().length();
				}
				// Eliminate an equality that is smaller or equal to the edits on both
				// sides of it.
				if ((lastequality != null) && (lastequality.length() <= Math.max(lengthinsertions1, lengthdeletions1))
						&& (lastequality.length() <= Math.max(lengthinsertions2, lengthdeletions2))) {
					// Walk back to offending equality.
					while (thisDiff != equalities.lastElement()) {
						thisDiff = pointer.previous();
					}
					pointer.next();

					// Replace equality with a delete.
					pointer.set(new Diff(Operation.DELETE, lastequality));
					// Insert a corresponding an insert.
					pointer.add(new Diff(Operation.INSERT, lastequality));

					equalities.pop(); // Throw away the equality we just deleted.
					if (!equalities.empty()) {
						// Throw away the previous equality (it needs to be reevaluated).
						equalities.pop();
					}
					if (equalities.empty()) {
						// There are no previous equalities, walk back to the start.
						while (pointer.hasPrevious()) {
							pointer.previous();
						}
					} else {
						// There is a safe equality we can fall back to.
						thisDiff = equalities.lastElement();
						while (thisDiff != pointer.previous()) {
							// Intentionally empty loop.
						}
					}

					lengthinsertions1 = 0; // Reset the counters.
					lengthinsertions2 = 0;
					lengthdeletions1 = 0;
					lengthdeletions2 = 0;
					lastequality = null;
					changes = true;
				}
			}
			thisDiff = pointer.hasNext() ? pointer.next() : null;
		}

		// Normalize the diff.
		if (changes) {
			diffCleanupMerge(diffs);
		}
		diffcleanupSemanticLossless(diffs);

		// Find any overlaps between deletions and insertions.
		// e.g: <del>abcxxx</del><ins>xxxdef</ins>
		//   -> <del>abc</del>xxx<ins>def</ins>
		// e.g: <del>xxxabc</del><ins>defxxx</ins>
		//   -> <ins>def</ins>xxx<del>abc</del>
		// Only extract an overlap if it is as big as the edit ahead or behind it.
		pointer = diffs.listIterator();
		Diff prevDiff = null;
		thisDiff = null;
		if (pointer.hasNext()) {
			prevDiff = pointer.next();
			if (pointer.hasNext()) {
				thisDiff = pointer.next();
			}
		}
		while (thisDiff != null) {
			if (prevDiff!=null && (prevDiff.getOperation() == Operation.DELETE) && (thisDiff.getOperation() == Operation.INSERT)) {
				String deletion = prevDiff.getText();
				String insertion = thisDiff.getText();
				int overlaplength1 = this.diffCommonOverlap(deletion, insertion);
				int overlaplength2 = this.diffCommonOverlap(insertion, deletion);
				if (overlaplength1 >= overlaplength2) {
					if ((overlaplength1 >= (deletion.length() / 2.0)) || (overlaplength1 >= (insertion.length() / 2.0))) {
						// Overlap found. Insert an equality and trim the surrounding edits.
						pointer.previous();
						pointer.add(new Diff(Operation.EQUAL, insertion.substring(0, overlaplength1)));
						prevDiff.setText(deletion.substring(0, deletion.length() - overlaplength1));
						thisDiff.setText(insertion.substring(overlaplength1));
						// pointer.add inserts the element before the cursor, so there is
						// no need to step past the new element.
					}
				} else {
					if ((overlaplength2 >= (deletion.length() / 2.0)) || (overlaplength2 >= (insertion.length() / 2.0))) {
						// Reverse overlap found.
						// Insert an equality and swap and trim the surrounding edits.
						pointer.previous();
						pointer.add(new Diff(Operation.EQUAL, deletion.substring(0, overlaplength2)));
						prevDiff.setOperation(Operation.INSERT);
						prevDiff.setText(insertion.substring(0, insertion.length() - overlaplength2));
						thisDiff.setOperation(Operation.DELETE);
						thisDiff.setText(deletion.substring(overlaplength2));
						// pointer.add inserts the element before the cursor, so there is
						// no need to step past the new element.
					}
				}
				thisDiff = pointer.hasNext() ? pointer.next() : null;
			}
			prevDiff = thisDiff;
			thisDiff = pointer.hasNext() ? pointer.next() : null;
		}
	}

	/**
	 * Look for single edits surrounded on both sides by equalities
	 * which can be shifted sideways to align the edit to a word boundary.
	 * e.g: The c&lt;ins&gt;at c&lt;/ins&gt;ame. -&gt; The &lt;ins&gt;cat &lt;/ins&gt;came.
	 *
	 * @param diffs List of Diff objects.
	 */
	private void diffcleanupSemanticLossless(List<Diff> diffs) {
	    StringBuilder equality1 = new StringBuilder();
	    StringBuilder edit = new StringBuilder();
	    StringBuilder equality2 = new StringBuilder();
	    String commonString;
	    int commonOffset;
	    int score;
	    int bestScore;

	    // Create a new iterator at the start.
	    ListIterator<Diff> pointer = diffs.listIterator();
	    Diff prevDiff = pointer.hasNext() ? pointer.next() : null;
	    Diff thisDiff = pointer.hasNext() ? pointer.next() : null;
	    Diff nextDiff = pointer.hasNext() ? pointer.next() : null;

	    // Intentionally ignore the first and last element (don't need checking).
	    while (nextDiff != null) {
	        if (prevDiff != null && prevDiff.getOperation() == Operation.EQUAL && nextDiff.getOperation() == Operation.EQUAL) {
	            // This is a single edit surrounded by equalities.
	            equality1.append(prevDiff.getText());
	            edit.append(thisDiff.getText());
	            equality2.append(nextDiff.getText());

	            // First, shift the edit as far left as possible.
	            commonOffset = diffCommonSuffix(equality1.toString(), edit.toString());
	            if (commonOffset != 0) {
	                commonString = edit.substring(edit.length() - commonOffset);
	                equality1.setLength(equality1.length() - commonOffset);
	                edit = new StringBuilder(commonString + edit.substring(0, edit.length() - commonOffset));
	                equality2.insert(0, commonString);
	            }

	            // Second, step character by character right, looking for the best fit.
	            String bestEquality1 = equality1.toString();
	            String bestEdit = edit.toString();
	            String bestEquality2 = equality2.toString();
	            bestScore = diffCleanupSemanticScore(equality1.toString(), edit.toString()) + 
	                         diffCleanupSemanticScore(edit.toString(), equality2.toString());
	            while (edit.length() != 0 && equality2.length() != 0 && edit.charAt(0) == equality2.charAt(0)) {
	                equality1.append(edit.charAt(0));
	                edit = new StringBuilder(edit.substring(1)).append(equality2.charAt(0));
	                equality2.setLength(equality2.length() - 1);
	                score = diffCleanupSemanticScore(equality1.toString(), edit.toString()) + 
	                        diffCleanupSemanticScore(edit.toString(), equality2.toString());
	                // The >= encourages trailing rather than leading whitespace on edits.
	                if (score >= bestScore) {
	                    bestScore = score;
	                    bestEquality1 = equality1.toString();
	                    bestEdit = edit.toString();
	                    bestEquality2 = equality2.toString();
	                }
	            }

	            // If there is an improvement, save it back to the diff.
	            if (!prevDiff.getText().equals(bestEquality1)) {
	                if (!bestEquality1.isEmpty()) {
	                    prevDiff.setText(bestEquality1);
	                } else {
	                    pointer.previous(); // Walk past nextDiff.
	                    pointer.previous(); // Walk past thisDiff.
	                    pointer.previous(); // Walk past prevDiff.
	                    pointer.remove(); // Delete prevDiff.
	                    pointer.next(); // Walk past thisDiff.
	                    pointer.next(); // Walk past nextDiff.
	                }
	                thisDiff.setText(bestEdit);
	                if (!bestEquality2.isEmpty()) {
	                    nextDiff.setText(bestEquality2);
	                } else {
	                    pointer.remove(); // Delete nextDiff.
	                    nextDiff = thisDiff;
	                    thisDiff = prevDiff;
	                }
	            }
	        }
	        prevDiff = thisDiff;
	        thisDiff = nextDiff;
	        nextDiff = pointer.hasNext() ? pointer.next() : null;
	    }
	}


	/**
	 * Given two strings, compute a score representing whether the internal
	 * boundary falls on logical boundaries.
	 * Scores range from 6 (best) to 0 (worst).
	 *
	 * @param one First string.
	 * @param two Second string.
	 * @return The score.
	 */
	private int diffCleanupSemanticScore(String one, String two) {
		if ((one.isEmpty()) || (two.isEmpty())) {
			// Edges are the best.
			return 6;
		}

		// Each port of this function behaves slightly differently due to
		// subtle differences in each language's definition of things like
		// 'whitespace'.  Since this function's purpose is largely cosmetic,
		// the choice has been made to use each language's native features
		// rather than force total conformity.
		char char1 = one.charAt(one.length() - 1);
		char char2 = two.charAt(0);
		boolean nonAlphaNumeric1 = !Character.isLetterOrDigit(char1);
		boolean nonAlphaNumeric2 = !Character.isLetterOrDigit(char2);
		boolean whitespace1 = nonAlphaNumeric1 && Character.isWhitespace(char1);
		boolean whitespace2 = nonAlphaNumeric2 && Character.isWhitespace(char2);
		boolean lineBreak1 = whitespace1 && (Character.getType(char1) == Character.CONTROL);
		boolean lineBreak2 = whitespace2 && (Character.getType(char2) == Character.CONTROL);
		boolean blankLine1 = lineBreak1 && BLANKLINEEND.matcher(one).find();
		boolean blankLine2 = lineBreak2 && BLANKLINESTART.matcher(two).find();

		if (blankLine1 || blankLine2) {
			// Five points for blank lines.
			return 5;
		} else if (lineBreak1 || lineBreak2) {
			// Four points for line breaks.
			return 4;
		} else if (nonAlphaNumeric1 && !whitespace1 && whitespace2) {
			// Three points for end of sentences.
			return 3;
		} else if (whitespace1 || whitespace2) {
			// Two points for whitespace.
			return 2;
		} else if (nonAlphaNumeric1 || nonAlphaNumeric2) {
			// One point for non-alphanumeric.
			return 1;
		}
		return 0;
	}

	// Define some regex patterns for matching boundaries.
	private static final Pattern BLANKLINEEND = Pattern.compile("\\n\\r?\\n\\Z", Pattern.DOTALL);
	private static final  Pattern BLANKLINESTART = Pattern.compile("\\A\\r?\\n\\r?\\n", Pattern.DOTALL);

	/**
	 * Reorder and merge like edit sections.  Merge equalities.
	 * Any edit section can move as long as it doesn't cross an equality.
	 *
	 * @param diffs List of Diff objects.
	 */
	private void diffCleanupMerge(List<Diff> diffs) {
		diffs.add(new Diff(Operation.EQUAL, "")); // Add a dummy entry at the end.
		ListIterator<Diff> pointer = diffs.listIterator();
		int countdelete = 0;
		int countinsert = 0;
		String textdelete = "";
		String textinsert = "";
		Diff thisDiff = pointer.next();
		Diff prevEqual = null;
		int commonlength;
		while (thisDiff != null) {
			switch (thisDiff.getOperation()) {
			case INSERT:
				countinsert++;
				textinsert += thisDiff.getText();
				prevEqual = null;
				break;
			case DELETE:
				countdelete++;
				textdelete += thisDiff.getText();
				prevEqual = null;
				break;
			case EQUAL:
				if ((countdelete + countinsert) > 1) {
					boolean bothtypes = (countdelete != 0) && (countinsert != 0);
					// Delete the offending records.
					pointer.previous(); // Reverse direction.
					while (countdelete-- > 0) {
						pointer.previous();
						pointer.remove();
					}
					while (countinsert-- > 0) {
						pointer.previous();
						pointer.remove();
					}
					if (bothtypes) {
						// Factor out any common prefixies.
						commonlength = diffCommonPrefix(textinsert, textdelete);
						if (commonlength != 0) {
							if (pointer.hasPrevious()) {
								thisDiff = pointer.previous();
								assert thisDiff.getOperation() == Operation.EQUAL : "Previous diff should have been an equality.";
								thisDiff.setText(thisDiff.getText() + textinsert.substring(0, commonlength));
								pointer.next();
							} else {
								pointer.add(new Diff(Operation.EQUAL, textinsert.substring(0, commonlength)));
							}
							textinsert = textinsert.substring(commonlength);
							textdelete = textdelete.substring(commonlength);
						}
						// Factor out any common suffixies.
						commonlength = diffCommonSuffix(textinsert, textdelete);
						if (commonlength != 0) {
							thisDiff = pointer.next();
							thisDiff.setText(textinsert.substring(textinsert.length() - commonlength) + thisDiff.getText());
							textinsert = textinsert.substring(0, textinsert.length() - commonlength);
							textdelete = textdelete.substring(0, textdelete.length() - commonlength);
							pointer.previous();
						}
					}
					// Insert the merged records.
					if (!textdelete.isEmpty()) {
						pointer.add(new Diff(Operation.DELETE, textdelete));
					}
					if (!textinsert.isEmpty()) {
						pointer.add(new Diff(Operation.INSERT, textinsert));
					}
					// Step forward to the equality.
					thisDiff = pointer.hasNext() ? pointer.next() : null;
				} else if (prevEqual != null) {
					// Merge this equality with the previous one.
					prevEqual.setText(prevEqual.getText() + thisDiff.getText());
					pointer.remove();
					thisDiff = pointer.previous();
					pointer.next(); // Forward direction
				}
				countinsert = 0;
				countdelete = 0;
				textdelete = "";
				textinsert = "";
				prevEqual = thisDiff;
				break;
			}
			thisDiff = pointer.hasNext() ? pointer.next() : null;
		}
		if (((LinkedList<Diff>) diffs).getLast().getText().isEmpty()) {
			((LinkedList<Diff>) diffs).removeLast(); // Remove the dummy entry at the end.
		}

		/*
		 * Second pass: look for single edits surrounded on both sides by equalities which can be shifted sideways to eliminate an equality. e.g: A<ins>BA</ins>C -> <ins>AB</ins>AC
		 */
		boolean changes = false;
		// Create a new iterator at the start.
		// (As opposed to walking the current one back.)
		pointer = diffs.listIterator();
		Diff prevDiff = pointer.hasNext() ? pointer.next() : null;
		thisDiff = pointer.hasNext() ? pointer.next() : null;
		Diff nextDiff = pointer.hasNext() ? pointer.next() : null;
		// Intentionally ignore the first and last element (don't need checking).
		while (nextDiff != null) {
			if (prevDiff != null && thisDiff != null && (prevDiff.getOperation() == Operation.EQUAL)
					&& (nextDiff.getOperation() == Operation.EQUAL)) {
				// This is a single edit surrounded by equalities.
				if (thisDiff.getText().endsWith(prevDiff.getText())) {
					// Shift the edit over the previous equality.
					thisDiff.setText(prevDiff.getText() + thisDiff.getText().substring(0, thisDiff.getText().length() - prevDiff.getText().length()));
					nextDiff.setText(prevDiff.getText() + nextDiff.getText());
					pointer.previous(); // Walk past nextDiff.
					pointer.previous(); // Walk past thisDiff.
					pointer.previous(); // Walk past prevDiff.
					pointer.remove(); // Delete prevDiff.
					pointer.next(); // Walk past thisDiff.
					thisDiff = pointer.next(); // Walk past nextDiff.
					nextDiff = pointer.hasNext() ? pointer.next() : null;
					changes = true;
				} else if (thisDiff.getText().startsWith(nextDiff.getText())) {
					// Shift the edit over the next equality.
					prevDiff.setText(prevDiff.getText() + nextDiff.getText());
					thisDiff.setText(thisDiff.getText().substring(nextDiff.getText().length()) + nextDiff.getText());
					pointer.remove(); // Delete nextDiff.
					nextDiff = pointer.hasNext() ? pointer.next() : null;
					changes = true;
				}
			}
			prevDiff = thisDiff;
			thisDiff = nextDiff;
			nextDiff = pointer.hasNext() ? pointer.next() : null;
		}
		// If shifts were made, the diff needs reordering and another shift sweep.
		if (changes) {
			diffCleanupMerge(diffs);
		}
	}

	/**
	 * Convert a Diff list into a pretty HTML report.
	 *
	 * @param diffs List of Diff objects.
	 * @return HTML representation.
	 */
	public String diffPrettyHtml(List<Diff> diffs) {
		StringBuilder html = new StringBuilder();
		for (Diff aDiff : diffs) {
			String text = aDiff.getText().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "&para;<br>");
			switch (aDiff.getOperation()) {
			case INSERT:
				html.append("<ins style=\"background:#e6ffe6;\">").append(text).append("</ins>");
				break;
			case DELETE:
				html.append("<del style=\"background:#ffe6e6;\">").append(text).append("</del>");
				break;
			case EQUAL:
				html.append("<span>").append(text).append("</span>");
				break;
			}
		}
		return html.toString();
	}

	/**
	 * Locate the best instance of 'pattern' in 'dmp' near 'loc' using the
	 * Bitap algorithm.  Returns -1 if no match found.
	 *
	 * @param text    The dmp to search.
	 * @param pattern The pattern to search for.
	 * @param loc     The location to search around.
	 * @return Best match index or -1.
	 * @since 23.2.1.26
	 */
	protected int matchbitap(String text, String pattern, int loc) {
		assert ((MATCHMAXBITS == 0) || (pattern.length() <= MATCHMAXBITS)) : "Pattern too long for this application.";

		// Initialise the alphabet.
		Map<Character, Integer> s = matchAlphabet(pattern);

		// Highest score beyond which we give up.
		double scorethreshold = MATCHTRESHOLD;
		// Is there a nearby exact match? (speedup)
		int bestloc = text.indexOf(pattern, loc);
		if (bestloc != -1) {
			scorethreshold = Math.min(matchBitapScore(0, bestloc, loc, pattern), scorethreshold);
			// What about in the other direction? (speedup)
			bestloc = text.lastIndexOf(pattern, loc + pattern.length());
			if (bestloc != -1) {
				scorethreshold = Math.min(matchBitapScore(0, bestloc, loc, pattern), scorethreshold);
			}
		}

		// Initialise the bit arrays.
		int matchmask = 1 << (pattern.length() - 1);
		bestloc = -1;

		int binmin;
		int binmid;
		int binmax = pattern.length() + text.length();
		// Empty initialization added to appease Java compiler.
		int[] lastrd = new int[0];
		for (int d = 0; d < pattern.length(); d++) {
			// Scan for the best match; each iteration allows for one more error.
			// Run a binary search to determine how far from 'loc' we can stray at
			// this error level.
			binmin = 0;
			binmid = binmax;
			while (binmin < binmid) {
				if (matchBitapScore(d, loc + binmid, loc, pattern) <= scorethreshold) {
					binmin = binmid;
				} else {
					binmax = binmid;
				}
				binmid = ((binmax - binmin) / 2) + binmin;
			}
			// Use the result from this iteration as the maximum for the next.
			binmax = binmid;
			int start = Math.max(1, (loc - binmid) + 1);
			int finish = Math.min(loc + binmid, text.length()) + pattern.length();

			int[] rd = new int[finish + 2];
			rd[finish + 1] = (1 << d) - 1;
			for (int j = finish; j >= start; j--) {
				int charMatch;
				if ((text.length() <= (j - 1)) || !s.containsKey(text.charAt(j - 1))) {
					// Out of range.
					charMatch = 0;
				} else {
					charMatch = s.get(text.charAt(j - 1));
				}
				if (d == 0) {
					// First pass: exact match.
					rd[j] = ((rd[j + 1] << 1) | 1) & charMatch;
				} else {
					// Subsequent passes: fuzzy match.
					rd[j] = (((rd[j + 1] << 1) | 1) & charMatch) | (((lastrd[j + 1] | lastrd[j]) << 1) | 1) | lastrd[j + 1];
				}
				if ((rd[j] & matchmask) != 0) {
					double score = matchBitapScore(d, j - 1, loc, pattern);
					// This match will almost certainly be better than any existing
					// match.  But check anyway.
					if (score <= scorethreshold) {
						// Told you so.
						scorethreshold = score;
						bestloc = j - 1;
						if (bestloc > loc) {
							// When passing loc, don't exceed our current distance from loc.
							start = Math.max(1, (2 * loc) - bestloc);
						} else {
							// Already passed loc, downhill from here on in.
							break;
						}
					}
				}
			}
			if (matchBitapScore(d + 1, loc, loc, pattern) > scorethreshold) {
				// No hope for a (better) match at greater error levels.
				break;
			}
			lastrd = rd;
		}
		return bestloc;
	}

	/**
	 * Compute and return the score for a match with e errors and x location.
	 *
	 * @param e       Number of errors in match.
	 * @param x       Location of match.
	 * @param loc     Expected location of match.
	 * @param pattern Pattern being sought.
	 * @return Overall score for match (0.0 = good, 1.0 = bad).
	 */
	private double matchBitapScore(int e, int x, int loc, String pattern) {
		float accuracy = (float) e / pattern.length();
		int proximity = Math.abs(loc - x);
		return accuracy + (proximity / (float) MATCHDISTANCE);
	}

	/**
	 * Initialise the alphabet for the Bitap algorithm.
	 *
	 * @param pattern The dmp to encode.
	 * @return Hash of character locations.
	 * @since 23.2.1.26
	 */
	protected Map<Character, Integer> matchAlphabet(String pattern) {
		Map<Character, Integer> s = new HashMap<>();
		char[] charpattern = pattern.toCharArray();
		for (char c : charpattern) {
			s.put(c, 0);
		}
		int i = 0;
		for (char c : charpattern) {
			s.put(c, s.get(c) | (1 << (pattern.length() - i - 1)));
			i++;
		}
		return s;
	}

}
