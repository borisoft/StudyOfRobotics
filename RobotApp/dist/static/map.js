var Map = function(realWidth, realHeight, mapView) {
	this.roboX = 0;
	this.roboY = 0;
	this.roboRotate = 0; // в градусах по часовой стрелке. 0 - на север
	this.realWidth = realWidth; //в сантиметрах
	this.realHeight = realHeight;

	if (mapView) {
		this.mapView = mapView;
	} else {
		throw "mapView object is null!";
	}

	this.updateRoboLocation(this.roboX, this.roboY, this.roboRotate);
}

Map.prototype.updateRoboLocation = function(newX, newY, newRotate) {
	if (newX < this.realWidth / 2 && newX > -this.realWidth / 2) {
		this.roboX = newX;
	}
	if (newY < this.realHeight / 2 && newY > -this.realHeight / 2) {
		this.roboY = newY;
	}
	this.roboRotate = newRotate;
	var tempLocation = {x: this.roboX, y: this.roboY, angle: this.roboRotate};
	this.mapView.redraw(tempLocation);
}

Map.prototype.shiftRobo = function(xDiff, yDiff, rotateDiff) {
	this.updateRoboLocation(this.roboX + xDiff, this.roboY + yDiff, this.roboRotate + rotateDiff);
}

Map.prototype.updateRoboLocationJSON = function(jsonData) {
	var location = JSON.parse(jsonData);
	this.updateRoboLocation(location.x / 100, location.y / 100, -location.angle);
}