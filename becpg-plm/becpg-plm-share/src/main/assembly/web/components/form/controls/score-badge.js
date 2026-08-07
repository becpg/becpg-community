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
     * Draws the whole scale with the reached class emphasised, the way the official marks
     * do, rather than the single chip that told nothing of the range.
     */
    function renderLetter(scoreClass) {
        if (isBlank(scoreClass)) {
            return "";
        }

        var upper = scoreClass.toString().toUpperCase();
        var reached = false;
        var html = "";

        for (var i = 0; i < LETTER_CLASSES.length; i++) {
            var letter = LETTER_CLASSES[i];
            var isCurrent = letter === upper;
            reached = reached || isCurrent;
            html += '<span class="score-badge-letter score-badge-letter-' + letter.toLowerCase()
                + (isCurrent ? " score-badge-letter-current" : "") + '">' + letter + "</span>";
        }

        if (!reached) {
            return '<span class="score-badge-error">' + Alfresco.util.encodeHTML(scoreClass.toString()) + "</span>";
        }
        return '<span class="score-badge-scale">' + html + "</span>";
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
            var label = part && part.label ? part.label : "!";
            html += '<span class="score-badge-warning score-badge-warning-' + shape + '" title="'
                + beCPG.util.encodeAttr(label) + '">' + Alfresco.util.encodeHTML(label) + "</span>";
        }
        return html;
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

    function renderTraffic(details) {
        var parts = details.parts || [];
        var html = "";
        for (var i = 0; i < parts.length; i++) {
            var part = parts[i];
            var level = part.label ? part.label : "";
            var name = NUTRIENT_LABELS[part.code] || part.code;
            var amount = isBlank(part.value) ? "" : formatNumber(part.value) + (isBlank(part.unit) ? " g" : " " + part.unit);
            var intake = isBlank(part.share) ? "" : formatNumber(part.share, 0) + " %";
            html += '<span class="score-badge-traffic score-badge-traffic-' + trafficLevel(part) + '">'
                + '<span class="score-badge-traffic-name">' + Alfresco.util.encodeHTML(name) + "</span>"
                + (amount ? '<span class="score-badge-traffic-amount">' + Alfresco.util.encodeHTML(amount) + "</span>" : "")
                + (intake ? '<span class="score-badge-traffic-intake">' + Alfresco.util.encodeHTML(intake) + "</span>" : "")
                + '<span class="score-badge-traffic-level">' + Alfresco.util.encodeHTML(level) + "</span></span>";
        }
        return html;
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
