var USER_CONTENT_LOCALE_PREFIX = "userContentLocale_";

function getBeCPGAuthTocken(user) {
    for (var i in user.capabilities) {
        if (i.indexOf("beCPGAuthTocken_") == 0) {
            return i.substring(16);
        }
    }
    return null;
}

/**
 * Reads the content language the user profile carries, null when the profile has none.
 */
function getUserContentLocale(user) {
    if (user != null) {
        for (var i in user.capabilities) {
            if (i.indexOf(USER_CONTENT_LOCALE_PREFIX) == 0) {
                return i.substring(USER_CONTENT_LOCALE_PREFIX.length);
            }
        }
    }
    return null;
}

function main() {
    model.beCPGAuthTocken = getBeCPGAuthTocken(user);
    model.userContentLocale = getUserContentLocale(user);
    model.isAIEnable = (user != null && user.capabilities["isAIUser"] != null && user.capabilities["isAIUser"] == true) || false;
}

main();

