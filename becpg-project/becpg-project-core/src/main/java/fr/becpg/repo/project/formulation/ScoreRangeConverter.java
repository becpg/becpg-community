package fr.becpg.repo.project.formulation;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreRangeConverter {
    private final NavigableMap<Double, String> scoreMap = new TreeMap<>(Collections.reverseOrder());

    public ScoreRangeConverter(String intervals) {
        parseIntervals(intervals);
    }

    private void parseIntervals(String intervals) {
        // Adjust regex to allow any word or phrase for labels
        String regex = "(\\w+): ?([\\[\\(])(-?\\d+(?:\\.\\d+)?);(-?\\d+(?:\\.\\d+)?)([\\]\\)])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(intervals);
        double lastUpperBound = Double.POSITIVE_INFINITY;

        while (matcher.find()) {
            String label = matcher.group(1); // Capture the full label (e.g., "Crit1", "BC")
            boolean lowerInclusive = matcher.group(2).equals("[");
            double lowerBound = Double.parseDouble(matcher.group(3));
            double upperBound = Double.parseDouble(matcher.group(4));
            boolean upperInclusive = matcher.group(5).equals("]");

            if (upperBound > lastUpperBound) {
                throw new IllegalArgumentException("Intervals must be in descending order and contiguous.");
            }
            lastUpperBound = lowerBound;

            if (upperInclusive) {
                scoreMap.put(upperBound, label);
            } else {
                scoreMap.put(upperBound - 0.0001, label);
            }

            if (lowerInclusive) {
                scoreMap.put(lowerBound, label);
            } else {
                scoreMap.put(lowerBound + 0.0001, label);
            }
        }
    }

    public String getScoreLetter(double score) {
        Map.Entry<Double, String> entry = scoreMap.ceilingEntry(score);
        return entry != null ? entry.getValue() : "N/A";
    }

 
}