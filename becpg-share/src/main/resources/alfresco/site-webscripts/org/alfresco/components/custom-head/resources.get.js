
function getBeCPGAuthTocken(user) {
    for (var i in user.capabilities) {
        if (i.indexOf("beCPGAuthTocken_") == 0) {
            return i.substring(16);
        }
    }
    return null;
}

function main() {
    model.beCPGAuthTocken = getBeCPGAuthTocken(user);
    model.isAIEnable = (user != null && user.capabilities["isAIUser"] != null && user.capabilities["isAIUser"] == true) || false;
}

main();

