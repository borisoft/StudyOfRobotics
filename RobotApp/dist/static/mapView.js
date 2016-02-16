var MapView = function(canvasId, realWidth, realHeight, maxPathLength, netLvl) {
	this.realWidth = realWidth;
	this.realHeight = realHeight;
	this.maxPathLength = maxPathLength || 10;
	this.canvas = new fabric.StaticCanvas(canvasId, {backgroundColor : "#eee"});
	
	this.path = new fabric.Polyline([], {
				strokeWidth: 1,
				fill: '',
				left: 0,
				top: 0,
				stroke: 'red'
			});
	var startX = this.canvas.width / 2;
 	var startY = this.canvas.height / 2;
	this.path.points.push({x: startX, y: startY});

	this.robo = new fabric.Triangle({
				left: 0,
				top: 0,
				angle: 0,
				originX: 'center',
				originY: 'center',
				fill: 'red',
				width: 12,
				height: 24
			});

	//net drawing
	netLvl = netLvl || 4;
	var op = 1;
	for (var lineLvl = 1; lineLvl <= netLvl; lineLvl *= 2) {
		op -= 1 / netLvl;
		for (var i = 1; i < lineLvl * 2; i+=2) {
			var vline = new fabric.Line([this.canvas.width * i / (lineLvl * 2), 0, this.canvas.width * i / (lineLvl * 2), this.canvas.height], {
		        stroke: 'black',
		        strokeWidth: 0.6,
		        opacity: op
		    });
		    this.canvas.add(vline);
			var hline = new fabric.Line([0, this.canvas.height * i / (lineLvl * 2), this.canvas.width, this.canvas.height * i / (lineLvl * 2)], {
		        stroke: 'black',
		        strokeWidth: 0.6,
		        opacity: op
		    });
		    this.canvas.add(hline);
		}	
	}

	this.canvas.add(this.path);
	this.canvas.add(this.robo);
    this.canvas.renderAll();
}

MapView.prototype.realXtoCanvas = function(realX){
	return ((realWidth / 2) + realX) * this.canvas.width / this.realWidth;
	// return this.canvas.width / this.realWidth * realX;
}

MapView.prototype.realYtoCanvas = function(realY) {
	return ((realHeight / 2) - realY) * this.canvas.height / this.realHeight;
	// return this.canvas.height / this.realHeight * realY;
}

MapView.prototype.redraw = function(newRoboLocation) {
	var  newPoint = {x: this.realXtoCanvas(newRoboLocation.x), y: this.realYtoCanvas(newRoboLocation.y)};

	this.robo.set({
		left: newPoint.x,
		top: newPoint.y,
		angle : newRoboLocation.angle
	});

	this.path.points.push(newPoint);

	if (this.path.points.length == this.maxPathLength) {
		this.path.points.shift();
	}

	this.canvas.renderAll();
}