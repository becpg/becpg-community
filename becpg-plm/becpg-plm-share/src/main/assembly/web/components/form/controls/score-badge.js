/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * beCPG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

/**
 * Renders a computed score from its normalized detail, whatever produced it.
 *
 * The detail follows the ScoreContext format:
 * { code, version, value, unit, class, range, scale, parts: [...], steps: [...] }
 *
 * The badge comes from the repository when the customer uploaded one under
 * /System/ScoreBadges/<code>/, and falls back on the shipped CSS classes otherwise.
 */
(function() {

    if (typeof beCPG === "undefined" || !beCPG.util) {
        return;
    }

    var LETTER_CLASSES = ["A", "B", "C", "D", "E"];

    /** Wording of the front of pack marks, which name nutrients rather than codes. */
    var NUTRIENT_LABELS = {
        "ENER-KJO": "Energy", "ENER-E14": "Energy", "FAT": "Fat", "FASAT": "Saturates",
        "SUGAR": "Sugars", "NACL": "Salt", "NA": "Sodium", "FATRN": "Trans fat",
        "PRO-": "Protein", "FIBTG": "Fibre"
    };
    var MAX_STARS = 5;

    beCPG.util.score = beCPG.util.score || {};

    /**
     * Parses a serialized score detail, returning null when it is absent or invalid.
     */
    function parseDetails(value) {
        if (!value) {
            return null;
        }
        try {
            return typeof value === "string" ? JSON.parse(value) : value;
        } catch (e) {
            return null;
        }
    }

    function isBlank(value) {
        return value === null || typeof value === "undefined" || value === "";
    }

    function formatNumber(value, decimals) {
        if (isBlank(value)) {
            return "";
        }
        var num = parseFloat(value);
        if (isNaN(num)) {
            return value.toString();
        }
        return num.toFixed(typeof decimals === "number" ? decimals : 2).replace(/\.?0+$/, "");
    }

    /**
     * Text summary of a score, shown on hover: the verdict then one line per part, so the
     * breakdown is reachable without opening the detail panel.
     */
    function buildTooltip(details) {
        var lines = [];
        var header = details.code || "";

        if (!isBlank(details["class"])) {
            header += " : " + details["class"];
        } else if (!isBlank(details.value)) {
            header += " : " + formatNumber(details.value) + (isBlank(details.unit) ? "" : " " + details.unit);
        }
        if (header) {
            lines.push(header);
        }

        var parts = details.parts || [];
        for (var i = 0; i < parts.length; i++) {
            var part = parts[i];
            var line = part.label ? part.label : part.code;
            if (!isBlank(part.value)) {
                line += " : " + formatNumber(part.value) + (isBlank(part.unit) ? "" : " " + part.unit);
            }
            if (!isBlank(part.contribution)) {
                line += " (" + formatNumber(part.contribution) + ")";
            }
            lines.push(line);
        }

        return lines.join("\n");
    }

    function badgeUrl(code, scoreClass) {
        var url = Alfresco.constants.PROXY_URI + "becpg/score/badge/" + encodeURIComponent(code);
        if (!isBlank(scoreClass)) {
            url += "/" + encodeURIComponent(scoreClass);
        }
        return url;
    }

    /**
     * Emits the repository badge with an inline fallback: when the image errors out, it is
     * replaced by the sibling span carrying the shipped CSS classes.
     */
    function renderRepositoryBadge(code, scoreClass, fallbackHtml) {
        if (isBlank(code)) {
            return fallbackHtml;
        }

        var fallbackId = Alfresco.util.generateDomId(null, "scoreBadgeFallback");
        var onError = "this.style.display='none';"
            + "var f=document.getElementById('" + fallbackId + "');if(f){f.style.display='inline-block';}";

        return '<img class="score-badge-img" src="' + badgeUrl(code, scoreClass) + '" alt="'
            + beCPG.util.encodeAttr(isBlank(scoreClass) ? code : scoreClass) + '" onerror="' + onError + '" />'
            + '<span id="' + fallbackId + '" style="display:none;">' + fallbackHtml + '</span>';
    }

    /**
     * The marks are drawn as inline SVG rather than styled spans: a glyph centred by
     * text-anchor and dominant-baseline lands exactly in its shape, whatever the font and
     * the line height of the page, which no amount of padding on a span achieves.
     */
    var LETTER_COLOURS = { A: "#00853f", B: "#64bf21", C: "#ffc800", D: "#ff7600", E: "#ff0100" };

    function svgText(x, y, text, fill, size, weight) {
        return '<text x="' + x + '" y="' + y + '" text-anchor="middle" dominant-baseline="central"'
            + ' font-family="Arial, Helvetica, sans-serif" font-size="' + size + '"'
            + ' font-weight="' + (weight || "bold") + '" fill="' + fill + '">'
            + Alfresco.util.encodeHTML(text) + "</text>";
    }

    function svgOpen(width, height, cssClass, title) {
        return '<svg class="' + cssClass + '" width="' + width + '" height="' + height + '"'
            + ' viewBox="0 0 ' + width + " " + height + '" role="img" aria-label="'
            + beCPG.util.encodeAttr(title || "") + '">'
            + (title ? "<title>" + Alfresco.util.encodeHTML(title) + "</title>" : "");
    }

    /**
     * Draws the whole scale with the reached class emphasised, the way the official marks
     * do, rather than the single chip that told nothing of the range.
     */
    function renderLetter(scoreClass) {
        if (isBlank(scoreClass)) {
            return "";
        }

        var upper = scoreClass.toString().toUpperCase();
        var reached = false;

        for (var c = 0; c < LETTER_CLASSES.length; c++) {
            reached = reached || LETTER_CLASSES[c] === upper;
        }
        if (!reached) {
            return '<span class="score-badge-error">' + Alfresco.util.encodeHTML(scoreClass.toString()) + "</span>";
        }

        var cell = 17;
        var grown = 25;
        var height = grown + 4;
        var width = (LETTER_CLASSES.length * cell) + (grown - cell) + 4;
        var html = svgOpen(width, height, "score-badge-scale", upper);
        var x = 2;

        for (var i = 0; i < LETTER_CLASSES.length; i++) {
            var letter = LETTER_CLASSES[i];
            var current = letter === upper;
            var size = current ? grown : cell;
            var y = (height - size) / 2;

            html += '<rect x="' + x + '" y="' + y + '" width="' + size + '" height="' + size + '" rx="3"'
                + ' fill="' + LETTER_COLOURS[letter] + '"' + (current ? ' stroke="#333" stroke-width="1.5"' : "")
                + (current ? "" : ' opacity="0.45"') + " />";
            html += svgText(x + (size / 2), y + (size / 2), letter, "#fff", current ? 16 : 11);

            x += size;
        }

        return html + "</svg>";
    }

    function renderStars(value) {
        if (isBlank(value)) {
            return "";
        }
        var stars = Math.max(0, Math.min(MAX_STARS, Math.round(parseFloat(value) * 2) / 2));
        var full = Math.floor(stars);
        var html = "";
        for (var i = 0; i < full; i++) {
            html += "★";
        }
        if (stars - full >= 0.5) {
            html += "½";
        }
        for (var j = Math.ceil(stars); j < MAX_STARS; j++) {
            html += "☆";
        }
        return '<span class="score-badge-stars">' + html + "</span>";
    }

    function renderWarnings(details) {
        var count = parseInt(details["class"], 10);
        if (isNaN(count)) {
            count = details.parts ? details.parts.length : 0;
        }

        var shape = warningShape(details.code);
        var html = "";

        for (var i = 0; i < count; i++) {
            var part = details.parts && details.parts[i] ? details.parts[i] : null;
            html += renderWarningMark(shape, part && part.label ? part.label : "!");
        }
        return html;
    }

    /**
     * One warning mark. The wording is stacked over as many lines as it has words, so a
     * three word mention stays inside its shape instead of overflowing it.
     */
    function renderWarningMark(shape, label) {
        var size = 54;
        var fill = shape === "circle" ? "#d0021b" : "#000";
        var html = svgOpen(size, size, "score-badge-warning", label);

        if (shape === "circle") {
            html += '<circle cx="27" cy="27" r="26" fill="' + fill + '" />';
        } else if (shape === "rect") {
            html += '<rect x="1" y="8" width="52" height="38" rx="2" fill="' + fill + '" />';
        } else {
            html += '<polygon points="16,1 38,1 53,16 53,38 38,53 16,53 1,38 1,16" fill="' + fill + '" />';
        }

        var words = label.toString().split(/\s+/);
        var lineHeight = words.length > 3 ? 9 : 11;
        var top = 27 - (((words.length - 1) * lineHeight) / 2);

        for (var i = 0; i < words.length; i++) {
            html += svgText(27, top + (i * lineHeight), words[i], "#fff", words.length > 3 ? 7 : 8);
        }

        return html + "</svg>";
    }

    /**
     * Shape of a warning mark. Most of Latin America stamps a black octagon, Brazil a black
     * rectangle shaped like a magnifying glass and Israel a red circle.
     */
    function warningShape(code) {
        if (code === "WARNINGS_BR") {
            return "rect";
        }
        if (code === "WARNINGS_IL") {
            return "circle";
        }
        return "octagon";
    }

    /**
     * Colour of a traffic light. The threshold engine publishes its verdict as the label of
     * the part, the contribution only serves the schemes that score in points.
     */
    function trafficLevel(part) {
        var label = part && part.label ? part.label.toString().toLowerCase() : "";

        if (label.indexOf("low") === 0 || label.indexOf("faible") === 0) {
            return "low";
        }
        if (label.indexOf("medium") === 0 || label.indexOf("moyen") === 0) {
            return "medium";
        }
        if (label.indexOf("high") === 0 || label.indexOf("élev") === 0) {
            return "high";
        }

        if (part && part.contribution !== null && typeof part.contribution !== "undefined") {
            if (part.contribution <= 1) {
                return "low";
            }
            if (part.contribution <= 2) {
                return "medium";
            }
        }
        return "high";
    }

    var TRAFFIC_COLOURS = { low: "#00853f", medium: "#ffc800", high: "#ff0100" };

    function renderTraffic(details) {
        var parts = details.parts || [];
        var html = "";

        for (var i = 0; i < parts.length; i++) {
            html += renderTrafficLight(parts[i]);
        }
        return html;
    }

    /**
     * One traffic light: the nutrient, the amount it holds, the share of the reference
     * intake it covers and the verdict, on four evenly spaced lines of one pill.
     */
    function renderTrafficLight(part) {
        var level = trafficLevel(part);
        var name = NUTRIENT_LABELS[part.code] || part.code;
        var amount = isBlank(part.value) ? "" : formatNumber(part.value) + (isBlank(part.unit) ? " g" : " " + part.unit);
        var intake = isBlank(part.share) ? "" : formatNumber(part.share, 0) + " %";
        var verdict = part.label ? part.label : "";

        var lines = [name, amount, intake, verdict];
        var width = 62;
        var height = 68;
        var ink = level === "medium" ? "#333" : "#fff";
        var html = svgOpen(width, height, "score-badge-traffic", name + " " + verdict);

        html += '<rect x="1" y="1" width="' + (width - 2) + '" height="' + (height - 2) + '" rx="4" fill="'
            + TRAFFIC_COLOURS[level] + '" />';

        var shown = [];
        for (var i = 0; i < lines.length; i++) {
            if (!isBlank(lines[i])) {
                shown.push(lines[i]);
            }
        }

        var step = (height - 10) / shown.length;
        for (var j = 0; j < shown.length; j++) {
            html += svgText(width / 2, 5 + (step * (j + 0.5)), shown[j], ink, 10, j === 0 ? "bold" : "normal");
        }

        return html + "</svg>";
    }

    /**
     * Numeric scale. A scheme that publishes no aggregated verdict, the reference intake
     * batteries being the case, is drawn as one cell per part instead of an empty span.
     */
    function renderNumeric(details) {
        if (isBlank(details.value) && details.parts && details.parts.length > 0) {
            return renderParts(details);
        }

        var html = '<span class="score-badge-value">' + formatNumber(details.value) + "</span>";
        if (!isBlank(details.unit)) {
            html += '<span class="score-badge-unit">' + Alfresco.util.encodeHTML(details.unit) + "</span>";
        }
        return html;
    }

    /**
     * One cell per part. A part expressed as a share of a reference intake is drawn as a
     * battery filled to that share, which is what the NutrInform mark shows.
     */
    function renderParts(details) {
        var html = "";
        for (var i = 0; i < details.parts.length; i++) {
            var part = details.parts[i];
            var amount = formatNumber(part.value) + (isBlank(part.unit) ? "" : " " + part.unit);
            var name = Alfresco.util.encodeHTML(part.label ? part.label : part.code);

            var intake = part.unit === "%" ? part.value : part.share;
            if (!isBlank(intake)) {
                var fill = Math.max(0, Math.min(100, parseFloat(intake)));
                amount = formatNumber(intake, 1) + " %";
                html += '<span class="score-badge-battery">'
                    + '<span class="score-badge-battery-name">' + name + "</span>"
                    + '<span class="score-badge-battery-body"><span class="score-badge-battery-fill" style="width:'
                    + fill.toFixed(0) + '%"></span></span>'
                    + '<span class="score-badge-battery-amount">' + Alfresco.util.encodeHTML(amount) + "</span></span>";
            } else {
                html += '<span class="score-badge-part">'
                    + '<span class="score-badge-part-name">' + name + "</span>"
                    + '<span class="score-badge-part-amount">' + Alfresco.util.encodeHTML(amount) + "</span></span>";
            }
        }
        return html;
    }


    /**
     * Gauge of the Planet-score axes: the whole A to E range as a colour bar, with a marker
     * on the level the product reaches.
     */
    function renderGauge(scoreClass) {
        if (isBlank(scoreClass)) {
            return "";
        }

        var upper = scoreClass.toString().toUpperCase();
        var reached = LETTER_CLASSES.indexOf(upper);
        if (reached < 0) {
            return '<span class="score-badge-error">' + Alfresco.util.encodeHTML(scoreClass.toString()) + "</span>";
        }

        var html = "";
        for (var i = 0; i < LETTER_CLASSES.length; i++) {
            html += '<span class="score-badge-gauge-step score-badge-gauge-step-' + LETTER_CLASSES[i].toLowerCase() + '">'
                + (i === reached ? '<span class="score-badge-gauge-marker"></span>' : "") + "</span>";
        }

        return '<span class="score-badge-gauge" title="' + beCPG.util.encodeAttr(upper) + '">' + html + "</span>";
    }


    /**
     * A mark states its themes side by side rather than a single verdict: one row per axis,
     * each with the gauge of the level it reaches.
     */
    function renderMark(details) {
        var parts = details.parts || [];
        if (parts.length === 0) {
            return "";
        }

        var html = "";
        for (var i = 0; i < parts.length; i++) {
            var part = parts[i];
            var name = NUTRIENT_LABELS[part.code] || part.code;
            html += '<span class="score-badge-mark-row">'
                + '<span class="score-badge-mark-name">' + Alfresco.util.encodeHTML(name) + "</span>"
                + renderGauge(part.label) + "</span>";
        }

        return '<span class="score-badge-mark">' + html + "</span>";
    }

    function renderGrade(details) {
        if (isBlank(details["class"])) {
            return renderNumeric(details);
        }
        return '<span class="score-badge-grade">' + Alfresco.util.encodeHTML(details["class"].toString()) + "</span>";
    }

    /**
     * Renders the badge of a score, without its detail.
     */
    beCPG.util.score.renderBadge = function(details) {
        if (!details) {
            return "";
        }

        var scale = details.scale || "Numeric";
        var fallback;

        if (scale === "Letter") {
            fallback = renderLetter(details["class"]);
        } else if (scale === "Stars") {
            fallback = renderStars(details.value);
        } else if (scale === "Warnings") {
            fallback = renderWarnings(details);
        } else if (scale === "Traffic") {
            fallback = renderTraffic(details);
        } else if (scale === "Mark") {
            fallback = renderMark(details);
        } else if (scale === "Gauge") {
            fallback = renderGauge(details["class"]);
        } else if (scale === "Grade") {
            fallback = renderGrade(details);
        } else {
            fallback = renderNumeric(details);
        }

        // any score may carry its official artwork, the shipped CSS is only the fallback
        var body = renderRepositoryBadge(details.code, details["class"], fallback);

        return '<span class="score-badge" title="' + beCPG.util.encodeAttr(buildTooltip(details)) + '">' + body + "</span>";
    };

    function partLabel(scope, part) {
        if (part.label) {
            return part.label;
        }
        var key = "score.part." + part.code;
        var translated = scope && scope.msg ? scope.msg(key) : key;
        return translated === key ? part.code : translated;
    }

    function renderDetailRow(scope, part, isStep) {
        var html = '<tr' + (isStep ? ' class="score-details-step"' : "") + ">";
        html += "<td>" + Alfresco.util.encodeHTML(partLabel(scope, part))
            + (isBlank(part["class"]) ? "" : ' <span class="score-details-class">'
                + Alfresco.util.encodeHTML(part["class"]) + "</span>") + "</td>";
        html += '<td class="score-details-number">' + formatNumber(part.value)
            + (isBlank(part.unit) ? "" : " " + Alfresco.util.encodeHTML(part.unit)) + "</td>";
        html += '<td class="score-details-number">' + formatNumber(part.weight, 4) + "</td>";
        html += '<td class="score-details-number">' + formatNumber(part.contribution) + "</td>";
        html += '<td class="score-details-number">' + (isBlank(part.share) ? "" : formatNumber(part.share, 1) + " %") + "</td>";
        html += "</tr>";
        return html;
    }

    function byContributionDesc(left, right) {
        var leftValue = left.contribution === null || typeof left.contribution === "undefined" ? 0 : Math.abs(left.contribution);
        var rightValue = right.contribution === null || typeof right.contribution === "undefined" ? 0 : Math.abs(right.contribution);
        return rightValue - leftValue;
    }

    /**
     * Renders the breakdown of a score as a sortable table, replacing the concatenated
     * title tooltips that were unreadable past a handful of lines.
     */
    beCPG.util.score.renderDetails = function(scope, details) {
        if (!details) {
            return "";
        }

        var parts = (details.parts || []).slice(0).sort(byContributionDesc);
        var steps = details.steps || [];

        if (parts.length === 0 && steps.length === 0) {
            return "";
        }

        var html = '<table class="score-details"><thead><tr>';
        html += "<th>" + scope.msg("score.details.part") + "</th>";
        html += "<th>" + scope.msg("score.details.value") + "</th>";
        html += "<th>" + scope.msg("score.details.weight") + "</th>";
        html += "<th>" + scope.msg("score.details.contribution") + "</th>";
        html += "<th>" + scope.msg("score.details.share") + "</th>";
        html += "</tr></thead><tbody>";

        for (var i = 0; i < parts.length; i++) {
            html += renderDetailRow(scope, parts[i], false);
        }
        for (var j = 0; j < steps.length; j++) {
            html += renderDetailRow(scope, steps[j], true);
        }

        html += "</tbody></table>";
        return html;
    };

    beCPG.util.score.parseDetails = parseDetails;

})();
