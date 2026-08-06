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

    function renderLetter(scoreClass) {
        if (isBlank(scoreClass)) {
            return "";
        }
        var upper = scoreClass.toString().toUpperCase();
        for (var i = 0; i < LETTER_CLASSES.length; i++) {
            if (LETTER_CLASSES[i] === upper) {
                return '<span class="score-badge-letter score-badge-letter-' + upper.toLowerCase() + '">' + upper + '</span>';
            }
        }
        return '<span class="score-badge-error">' + Alfresco.util.encodeHTML(scoreClass.toString()) + '</span>';
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
        var html = "";
        for (var i = 0; i < count; i++) {
            var part = details.parts && details.parts[i] ? details.parts[i] : null;
            var label = part && part.label ? part.label : "!";
            html += '<span class="score-badge-warning" title="' + beCPG.util.encodeAttr(label) + '">!</span>';
        }
        return html;
    }

    function trafficLevel(part) {
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
            var label = parts[i].label ? parts[i].label : parts[i].code;
            html += '<span class="score-badge-traffic score-badge-traffic-' + trafficLevel(parts[i]) + '">'
                + Alfresco.util.encodeHTML(label) + "</span>";
        }
        return html;
    }

    function renderNumeric(details) {
        var html = '<span class="score-badge-value">' + formatNumber(details.value) + "</span>";
        if (!isBlank(details.unit)) {
            html += '<span class="score-badge-unit">' + Alfresco.util.encodeHTML(details.unit) + "</span>";
        }
        return html;
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
        } else if (scale === "Grade") {
            fallback = renderGrade(details);
        } else {
            fallback = renderNumeric(details);
        }

        if (scale === "Letter" || scale === "Grade") {
            return '<span class="score-badge">' + renderRepositoryBadge(details.code, details["class"], fallback) + "</span>";
        }

        return '<span class="score-badge">' + fallback + "</span>";
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
        html += "<td>" + Alfresco.util.encodeHTML(partLabel(scope, part)) + "</td>";
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
