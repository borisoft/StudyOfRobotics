var realWidth = 300;
var realHeight = 300;

var mapView = new MapView('c', realWidth, realHeight, 90);
var map = new Map(realWidth, realHeight, mapView);

function getXmlHttp(){
	var xmlhttp;
	try {
		xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
	} catch (e) {
		try {
			xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
		} catch (E) {
			xmlhttp = false;
		}
	}
	if (!xmlhttp && typeof XMLHttpRequest != 'undefined') {
		xmlhttp = new XMLHttpRequest();
	}
	return xmlhttp;
}

function httpGet(theUrl)
{
	xmlHttp.open( "GET", theUrl, false );
	xmlHttp.send( null );
	if(xmlHttp.status == 200) {
		alert(xmlHttp.responseText);
	}
}

function httpGetASync(theUrl, callBack) {
	var xmlhttp = getXmlHttp()
	xmlhttp.open('GET', theUrl, true);
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4) {
		    if (callBack != null && xmlhttp.status == 200) {
		    	callBack(xmlhttp.responseText);
	    	}
		}
	};
	xmlhttp.send(null);
}


function fakeData() {
	function getRandomInt(min, max) {
    	return Math.floor(Math.random() * (max - min + 1)) + min;
	}

	setInterval(function(){
		map.updateRoboLocationJSON(JSON.stringify({x: getRandomInt(100, 500), y:getRandomInt(100, 500), angle: getRandomInt(-45, 45)}));
	}, 1000);
}

function run() {
    setInterval(function(){
        httpGetASync("/command/?cmd=coordinates", function(responseText) {
            var that = map;
            return that.updateRoboLocationJSON(responseText);
        });
    }, 1000);
}

function execCmd(cmdName) {
	var angle = document.getElementById('angle').value;
	var distance = document.getElementById('distance').value;
	httpGetASync("/command/?cmd=" + cmdName + "&angle=" + angle + "&distance=" + distance, null);
}

run();