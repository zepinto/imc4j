window.setInterval("reloadIFrame();", 5000);
window.setInterval("changeDetect();", 1000);

function reloadIFrame() {
    //document.getElementById("logIframe").location.reload();
}

function changeDetect() {
    var someChange = false;

    var settings = document.getElementById("settings");
    if (settings.value != settings.defaultValue) {
        someChange = true;
        if (!settings.className.includes(" changed"))
            settings.className += " changed";
    }
    else {
        settings.className = settings.className.replace(" changed", "");
    }

    var autoStart = document.getElementById("autoStart");
    if (autoStart.checked != autoStart.defaultChecked) {
        someChange = true;
        if (!autoStart.parentNode.className.includes(" changed"))
            autoStart.parentNode.className += " changed";
    }
    else {
        autoStart.parentNode.className = autoStart.parentNode.className.replace(" changed", "");
    }

    var save = document.getElementById("save");
    if (someChange) {
        if (!save.className.includes(" buttonChanged"))
            save.className += " buttonChanged";
    }
    else {
        save.className = save.className.replace(" buttonChanged", "");
    }
}