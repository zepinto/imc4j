// document.addEventListener('touchstart', onTouchStart, {passive: true});

// window.setInterval("reloadLogbookFrame();", 5000);
window.setInterval("changeDetect();", 1000);
window.setInterval("isRunning();", 2000);

function httpGetAsync(theUrl, callback) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() { 
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
            callback(xmlHttp.responseText);
    }
    xmlHttp.open("GET", theUrl, true); // true for asynchronous 
    xmlHttp.send(null);
}

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


function isRunning() {
    httpGetAsync(window.location.href + "state", updateStartStopState);
}

function updateStartStopState(state) {
    var isStarted = state === true || state === "true";
    var startStop = document.getElementById("startStop");
    
    var newValue = isStarted ? "Stop" : "Start";
    var isTheSame = startStop.value === newValue;
    startStop.value = newValue;

    // console.log(" '" + startStop.value + " '" + newValue + "'  " + isTheSame);
    if (!isTheSame) {
        console.log("RELOAD: " + window.location.pathname + window.location.hash);

        window.location.assign(window.location.pathname + window.location.hash);
    }
}