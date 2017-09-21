// document.addEventListener('touchstart', onTouchStart, {passive: true});

// window.setInterval("reloadLogbookFrame();", 5000);
window.setInterval("changeDetect();", 1000);

function reloadLogbookFrame() {
    logbook.location.reload(1);
    setTimeout(function () {scrollToEnd();}, 1000);
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
        if (!autoStart.parentNode.className.includes(" changedOverBackground"))
            autoStart.parentNode.className += " changedOverBackground";
    }
    else {
        autoStart.parentNode.className = autoStart.parentNode.className.replace(" changedOverBackground", "");
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

function scrollToEnd() { 
    var frame = window.frames.logbook; 
    frame.scrollTo(0, 10000000000000000); 
} 