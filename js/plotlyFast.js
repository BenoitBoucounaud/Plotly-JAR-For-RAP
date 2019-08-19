var divGraphID = 0;

PlotlyGraph = function(parent) {

	this.createElement = function(parent) {
		divGraphID = divGraphID + 1;
		var element = document.createElement("div");
		element.id = "divGraph" + divGraphID;
		element.style.position = "absolute";
		element.style.left = "0";
		element.style.top = "0";
		element.style.width = "100%";
		element.style.height = "100%";
		parent.append(element);
		return element;
	},

	this.dirty = true;
	this.parent = parent;

	this.glow = true;

	this.draw = function() {
		if (!this.dirty) {
			return;
		}
		var those = this;
		try {
			if (!this.element) {
				this.element = this.createElement(this.parent);
			}
			var ar = this.parent.getClientArea();
			var width = ar[2];
			var height = ar[3];

			// Construct the chart
			if (document.getElementById(this.element.id)) {

				Plotly.newPlot(this.element.id, this.inputs, this.layout,
						this.optionsStr);
			} else {
				setTimeout(function() {
					those.draw();
				}, 150);
				return;
			}

			if (this.glow) {
				this.glowit(this.element.id);
			}

			document
					.getElementById(this.element.id)
					.on(
							'plotly_selected',
							function(data) {
								var remoteObject = rap.getRemoteObject(those);
								var ans = new Array();
								for (var i = 0; i < data.points.length; i++) {
									ans[i] = {
										curveNumber : data.points[i].curveNumber,
										pointNumber : data.points[i].pointNumber,
										x : data.points[i].x,
										y : data.points[i].y
									};

								}

								remoteObject.set("ans", ans);

								remoteObject.set("selected", {
									selectedPoints : ans
								});

								remoteObject.notify("Selection", {
									selectedPoints : ans
								});

								var elements = those.element
										.getElementsByClassName("select-outline");
								if (elements)
									while (elements[0]) {
										elements[0].parentNode
												.removeChild(elements[0]);
									}

							});
			this.dirty = false;
		} catch (e) {
		}
	}

	this.resize = function() {
		console.log('resize');
		var ar = this.parent.getClientArea();
		var width = ar[2];
		var height = ar[3];
		try {
			Plotly.relayout(this.element.id, {
				width : width,
				height : height
			});
		} catch (e) {
		}
	}

	this.destroy = function() {
		console.log('destroy');
		rap.off("render", this.draw);
		parent.removeListener("Resize", this.resize);
		var element = this.element;
		if (element.parentNode) {
			element.parentNode.removeChild(element);
		}
	}

	// Scan and found the different param of the introduced object
	this.setOptions = function(data) {

		this.inputs = data.inputs;
		this.layout = data.layout;
		this.optionsStr = data.options;

		this.ans = data.ans;

		this.glow = data.glow;
		this.dirty = true;
	}

	rap.on("render", this.draw.bind(this));
	parent.addListener("Resize", this.resize.bind(this));
};

rap.registerTypeHandler("PlotlyGraph", {

	factory : function(properties) {

		var parent = rap.getObject(properties.parent);

		return new PlotlyGraph(parent);
	},

	destructor : "destroy",

	properties : [ "options" ],

	// methods : [ "setOptions" ],

	events : [ "Selection" ]

});
