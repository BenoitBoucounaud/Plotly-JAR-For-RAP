package main;

import java.util.List;
import java.util.Map;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.json.JSONObject;

import plotly.factory.D2Factory;
import plotly.factory.SurfaceFactory;
import plotly.model.Layout2DModel;
import plotly.model.Layout3DModel;
import plotly.model.OptionsModel;
import plotly.model.TraceModel;
import ui.graphCompsite.GraphComp;

/**
 * Invoke this class to build a precise chart. <br>
 * Call <b>build</b>( String type, String[][] traces, String[] layout, String[]
 * options, Composite parent) to obtain a Composite with your chart.<br>
 * Don't forget to import "d3.min.js", "plotly.js" and "plotlyGraph.js" in
 * WebContent/js.<br>
 * <br>
 * <ul>
 * <li><b>Traces exemples:</b><br>
 * <br>
 * <ul>
 * <li>For a surface chart : <br>
 * String[][] traces = { { "x: [2, 3, 4, 5], y: [16, 5, 11, 9], z :
 * [[8,9,10,11], [11,12,13,14], [14,15,16,15], [15,18,21,17]]" } };</li><br>
 * <br>
 * <li>For a scatter chart : <br>
 * String[][] traces = { { "x: [2, 3, 4, 5], y: [16, 5, 11, 9]", "mode :
 * 'markers+lines'", "name : 'First trace'", "text : ['point 1.1', 'point 1.2',
 * 'point 1.3', 'point 1.4']" }, { "x: [1, 3.2, 5.9, 8.2], y: [4.1, 6.9, 18,
 * 11]", "mode : 'lines'", "name : 'Second trace'", "text : ['point 2.1', 'point
 * 2.2', 'point 2.3', 'point 2.4']" } };</li> <br>
 * <br>
 * <li>For a bar chart : <br>
 * String[][] traces = { { "x: [2, 3, 4, 5], y: [16, 5, 11, 9]", "mode :
 * 'markers+lines'", "name : 'First trace'", "text : ['point 1.1', 'point 1.2',
 * 'point 1.3', 'point 1.4']" }, { "x: [1, 3.2, 5.9, 8.2], y: [4.1, 6.9, 18,
 * 11]", "mode : 'lines'", "name : 'Second trace'", "text : ['point 2.1', 'point
 * 2.2', 'point 2.3', 'point 2.4']" } };</li> <br>
 * <br>
 * <li>For a pie chart : <br>
 * String[][] traces = { { " values: [27, 11, 25, 8, 1, 3, 25]", " name : 'GHG
 * Emissons'", " labels: ['US', 'China', 'European Union', 'Russian Federation',
 * 'Brazil', 'India', 'Rest of World' ], hoverinfo: 'label+percent+name'" }, {
 * "values: [27, 11, 25, 8, 1, 3, 25]", "name : 'CO2 Emissions'", "labels:
 * ['US', 'China', 'European Union', 'Russian Federation', 'Brazil', 'India',
 * 'Rest of World' ], hoverinfo: 'label+percent+name'" } };</li> <br>
 * <br>
 * <br>
 * </ul>
 * </li>
 * <li><b>Layout exemples:</b><br>
 * <br>
 * <ul>
 * <li>For a 3D chart :<br>
 * String[] layoutChart = { " title: '3D Chart title'", "scene: {camera: {eye:
 * {x: 1.87, y: 0.88, z: -0.64}}}", };</li><br>
 * <br>
 * <li>For a 2D chart : <br>
 * String[] layoutChart = { " title: 'Scatter Chart title'", " xaxis: {range: [
 * 1, 5]}", " yaxis: {range: [ 8, 17]}", "legend: {y: 0.5, yref: 'paper', font:
 * { family: 'Arial, sans-serif', size: 20, color: 'grey'}}",
 * "hovermode:'closest', slider:{ visible:true, plotlycommand:'animate', args:[
 * 'slider.value', { duration:400, ease:'cubic-in-out' } ]}" };</li> <br>
 * <br>
 * <br>
 * </ul>
 * </li>
 * <li><b>Options exemples:</b><br>
 * <br>
 * String[] options = {"displayModeBar: false"};<br>
 * String[] options = {"staticPlot : true", "displayModeBar : false", "reponsive
 * : true"}<br>
 * <br>
 * <br>
 * </li>
 * <li><b>Rest of the call exemple:</b><br>
 * <br>
 * String[] types = {"bar", "scatter"}; MainBuilder main = new
 * MainBuilder();<br>
 * GraphComp gc = (GraphComp) main.build(types, traces, layoutChart, options,
 * HomeComposite);</li>
 * </ul>
 * 
 * @author Benoit Boucounaud
 * @version 2.0
 *
 */

public class MainBuilder {

	private static String script;
	private static final String SURFACE = "surface";

	/**
	 * Build the chart you want and return it. <br>
	 * Parameters who can not be null : - type - traces.data <br>
	 * Except traces.data each array component is optional<br>
	 * 
	 * 
	 * 
	 * @param type    String[] - Type of chart (only surface for 3D chart) (ex:
	 *                ['scatter', 'bar']
	 * 
	 * @param traces  String [][] - full references :
	 *                https://plot.ly/javascript/reference/ <br>
	 *                [x][] trace [][x] parameters<br>
	 * 
	 *                <ul>
	 *                <li>Datas :<br>
	 *                ex : "x: [1,2,3,...], y: [7,8,9,...]"</li>
	 *                <li>Mode : Determines the drawing mode for this scatter
	 *                trace.<br>
	 *                ex : "mode : lines" or "mode : markers"</li>
	 *                <li>Name : Sets the trace name. The trace name appear as the
	 *                legend item and on hover.<br>
	 *                ex : "name : 'trace number 1'</li>
	 *                <li>Marker : Sets the marker symbol type. <br>
	 *                ex : "marker :{ size: 12 }"</li>
	 *                <li>Text : Sets text elements associated with each (x,y)
	 *                pair.<br>
	 *                ex : "text : ['B-a', 'B-b', 'B-c', 'B-d', 'B-e']"</li>
	 *                <li>Other options : any other option in plotly trace options
	 *                <br>
	 *                ex : "textfont : {family:'Times New Roman'},textposition:
	 *                'bottom center'"</li>
	 *                </ul>
	 * 
	 * @param layout  String[] - layout parameters (can be null)
	 * 
	 *                <ul>
	 *                <li>Title : chart title <br>
	 *                ex: "title:'Data Labels on the Plot'"</li>
	 *                <li>2D Chart only - Xaxis : parameters of X axis<br>
	 *                ex : "xaxis: {range: [ 0.75, 5.25 ]}"</li>
	 *                <li>2D Chart only - Yaxis : parameters of Y axis<br>
	 *                ex : "yaxis: {range: [ 0, 8 ]}"</li>
	 *                <li>3D Chart only - Scene : chart representation options<br>
	 *                ex : "scene:{ camera: {eye: { x: -1.4, y: -1.4, z: 1.5
	 *                }}}"</li>
	 *                <li>Legend : parameters of chart's legend<br>
	 *                ex : "legend: {y: 0.5, yref: 'paper', font: { family: 'Arial,
	 *                sans-serif', size: 20, color: 'grey'}}"</li>
	 *                <li>Show legend : Determines whether or not a legend is
	 *                drawn.<br>
	 *                ex : "showlegend : false"</li>
	 *                <li>Auto size : Determines whether or not a layout width or
	 *                height that has been left undefined by the user is initialized
	 *                on each relayout.<br>
	 *                ex : "autosize : false"</li>
	 *                <li>Height : <br>
	 *                ex : "height : 700"</li>
	 *                <li>Width : <br>
	 *                ex : "width : 800"</li>
	 *                <li>Margin : <br>
	 *                ex : "margin: {l: 50, r: 50, b: 100, t: 100, pad: 4 }"</li>
	 *                <li>Annotation : An annotation is a text element that can be
	 *                placed anywhere in the plot.<br>
	 *                ex : "hovermode:'closest', slider:{ visible:true,
	 *                plotlycommand:'animate', args:[ 'slider.value', {
	 *                duration:400, ease:'cubic-in-out' } ]}"</li>
	 *                </ul>
	 * 
	 * @param options String[] - options parameters(can be null)<br>
	 *                <ul>
	 *                <li>Scroll zoom : enable zoom <br>
	 *                ex : "scrollZoom : true"</li>
	 * 
	 *                <li>Static plot : enable static plot <br>
	 *                ex : "staticPlot: true"</li>
	 * 
	 *                <li>Display ModeBar : display modeBar <br>
	 *                ex : "displayModeBar: false"</li>
	 * 
	 *                <li>Display logo : display logo<br>
	 *                ex : "displaylogo: false"</li>
	 * 
	 *                <li>Responsive : display responsive<br>
	 *                ex : "responsive: true"</li>
	 *                </ul>
	 * 
	 * @param parent  Composite <br>
	 *                Parent composite where the final composite will be placed
	 * 
	 * @return Object - Must be convert to GraphComp <br>
	 */
	public Object build(String[] type, String[][] traces, String[] layout, String[] options, Composite parent) {

		TraceModel[] traceModels = traceModelBuiler(type, traces);
		OptionsModel optionsModel = optionsBuilder(options);

		/*
		 * 2D
		 */
		if (!type[0].equals(SURFACE)) {

			Layout2DModel layout2D = layout2DBuilder(layout);
			D2Factory sf = new D2Factory();

			script = sf.createScript(traceModels, layout2D, optionsModel);
			
			JSONObject test = new JSONObject(script);
			
			System.out.println(JsonObject.readFrom(test.toString()));

			if (parent != null)
				return createComposite(script, parent);

			else
				return null;

		}
		/*
		 * SURFACE
		 */
		else if (type[0].equals(SURFACE)) {

			Layout3DModel layout3D = layout3DBuilder(layout);
			SurfaceFactory sf = new SurfaceFactory();

			script = sf.createScript(traceModels, layout3D, optionsModel);

			if (parent != null)
				return createComposite(script, parent);

			else
				return null;
		} else
			return null;

	}

	// Return Composite
	private GraphComp createComposite(String json, Composite parent) {

		GraphComp gc = new GraphComp(parent, SWT.NONE);

		gc.setOptions(new JSONObject(json));

		return gc;
	}

	/**
	 * Update the current chart. <br>
	 * Parameters who can not be null : - type - traces.data <br>
	 * Except traces.data each array component is optional<br>
	 * 
	 * 
	 * 
	 * @param type    String - Type of chart (only surface for 3D chart)
	 * 
	 * @param traces  String [][] - full references :
	 *                https://plot.ly/javascript/reference/ <br>
	 *                [x][] trace [][x] parameters<br>
	 * 
	 *                <ul>
	 *                <li>Datas</li>
	 *                <li>Mode : Determines the drawing mode for this scatter
	 *                trace.</li>
	 *                <li>Name : Sets the trace name. The trace name appear as the
	 *                legend item and on hover.</li>
	 *                <li>Marker : Sets the marker symbol type.</li>
	 *                <li>Text : Sets text elements associated with each (x,y)
	 *                pair.</li>
	 *                <li>Other options : any other option in plotly trace options
	 *                </li>
	 *                </ul>
	 * 
	 * @param layout  String[] - layout parameters (can be null)
	 * 
	 *                <ul>
	 *                <li>Title : chart title</li>
	 *                <li>2D Chart only - Xaxis : parameters of X axis</li>
	 *                <li>2D Chart only - Yaxis : parameters of Y axis</li>
	 *                <li>3D Chart only - Scene : chart representation options</li>
	 *                <li>Legend : parameters of chart's legend</li>
	 *                <li>Show legend : Determines whether or not a legend is
	 *                drawn.</li>
	 *                <li>Auto size : Determines whether or not a layout width or
	 *                height that has been left undefined by the user is initialized
	 *                on each relayout.</li>
	 *                <li>Height</li>
	 *                <li>Width</li>
	 *                <li>Margin</li>
	 *                <li>Annotation : An annotation is a text element that can be
	 *                placed anywhere in the plot.</li>
	 *                </ul>
	 * 
	 * @param options String[] - options parameters(can be null)<br>
	 *                <ul>
	 *                <li>Scroll zoom : enable zoom</li>
	 * 
	 *                <li>Static plot : enable static plot</li>
	 * 
	 *                <li>Display ModeBar : display modeBar</li>
	 * 
	 *                <li>Display logo : display logo</li>
	 * 
	 *                <li>Responsive : display responsive</li>
	 *                </ul>
	 * 
	 * @param gc      GraphComp - Parent composite where the final composite is
	 *                placed
	 * 
	 */
	public void updateData(String[] type, String[][] traces, String[] layout, String[] options, GraphComp gc) {

		TraceModel[] traceModels = traceModelBuiler(type, traces);
		OptionsModel optionsModel = optionsBuilder(options);
		/*
		 * 2D
		 */
		if (!type[0].equals(SURFACE)) {

			Layout2DModel layout2D = layout2DBuilder(layout);
			D2Factory sf = new D2Factory();

			script = sf.createScript(traceModels, layout2D, optionsModel);

		}
		/*
		 * SURFACE
		 */
		else if (type[0].equals(SURFACE)) {

			Layout3DModel layout3D = layout3DBuilder(layout);
			SurfaceFactory sf = new SurfaceFactory();

			script = sf.createScript(traceModels, layout3D, optionsModel);

		}

		gc.setOptions(new JSONObject(script));
	}

	/*
	 * PLOTLY
	 */

	private TraceModel[] traceModelBuiler(String type[], String[][] traces) {
		TraceModel[] traceModels = new TraceModel[traces.length];

		for (int i = 0; i < traces.length; i++) {
			TraceModel tm = new TraceModel();

			// Type
			tm.setType(type[i]);

			for (int j = 0; j < traces[i].length; j++) {

				// Data
				if (traces[i][j].substring(0, 1).equals("x") || traces[i][j].substring(0, 6).equals("values"))
					tm.setData(traces[i][j]);

				// Mode
				else if (traces[i][j].substring(0, 4).equals("mode"))
					tm.setMode(traces[i][j]);

				// Name
				else if (traces[i][j].substring(0, 4).equals("name"))
					tm.setName(traces[i][j]);

				// Maker
				else if (traces[i][j].substring(0, 4).equals("mode"))
					tm.setMode(traces[i][j]);

				// Text
				else if (traces[i][j].substring(0, 4).equals("text"))
					tm.setText(traces[i][j]);

				// Contours
				else if (traces[i][j].substring(0, 8).equals("contours"))
					tm.setContours(traces[i][j]);

				// Other Options
				else
					tm.setOtherOption(traces[i][j]);
			}

			traceModels[i] = tm;

		}

		return traceModels;
	}

	/*
	 * LAYOUT 2D
	 */
	private Layout2DModel layout2DBuilder(String[] layout) {

		if (layout != null) {

			Layout2DModel layout2D = new Layout2DModel();

			for (int i = 0; i < layout.length; i++) {

				// Title
				if (layout[i].substring(0, 5).equals("title"))
					layout2D.setTitle(layout[i]);

				// Xaxis
				else if (layout[i].substring(0, 5).equals("xaxis"))
					layout2D.setXaxis(layout[i]);

				// Yaxis
				else if (layout[i].substring(0, 5).equals("yaxis"))
					layout2D.setYaxis(layout[i]);

				// Legend
				else if (layout[i].substring(0, 6).equals("legend"))
					layout2D.setLegend(layout[i]);

				// ShowLegend
				else if (layout[i].substring(0, 10).equals("showlegend"))
					layout2D.setShowLegend(layout[i]);

				// AutoSize
				else if (layout[i].substring(0, 8).equals("autoSize"))
					layout2D.setAutoSize(layout[i]);

				// Height
				else if (layout[i].substring(0, 6).equals("height"))
					layout2D.setHeight(layout[i]);

				// Width
				else if (layout[i].substring(0, 5).equals("width"))
					layout2D.setWidth(layout[i]);

				// Margin
				else if (layout[i].substring(0, 6).equals("margin"))
					layout2D.setMargin(layout[i]);

				// Annotation
				else if (layout[i].substring(0, 10).equals("annotation"))
					layout2D.setAnnotations(layout[i]);

			}

			return layout2D;
		} else
			return null;
	}

	/*
	 * LAYOUT 3D
	 */
	private Layout3DModel layout3DBuilder(String[] layout) {

		if (layout != null) {

			Layout3DModel layout3D = new Layout3DModel();

			for (int i = 0; i < layout.length; i++) {

				// Title
				if (layout[i].substring(0, 5).equals("title"))
					layout3D.setTitle(layout[i]);

				// Scene
				else if (layout[i].substring(0, 5).equals("scene"))
					layout3D.setScene(layout[i]);

				// Legend
				else if (layout[i].substring(0, 6).equals("legend"))
					layout3D.setLegend(layout[i]);

				// ShowLegend
				else if (layout[i].substring(0, 10).equals("showlegend"))
					layout3D.setShowLegend(layout[i]);

				// AutoSize
				else if (layout[i].substring(0, 8).equals("autoSize"))
					layout3D.setAutoSize(layout[i]);

				// Height
				else if (layout[i].substring(0, 6).equals("height"))
					layout3D.setHeight(layout[i]);

				// Width
				else if (layout[i].substring(0, 5).equals("width"))
					layout3D.setWidth(layout[i]);

				// Margin
				else if (layout[i].substring(0, 6).equals("margin"))
					layout3D.setMargin(layout[i]);

				// Annotation
				else if (layout[i].substring(0, 10).equals("annotation"))
					layout3D.setAnnotations(layout[i]);

			}

			return layout3D;
		} else
			return null;
	}

	/*
	 * Options
	 */
	private OptionsModel optionsBuilder(String[] options) {
		if (options != null) {
			OptionsModel optionsModel = new OptionsModel();

			for (int i = 0; i < options.length; i++) {

				// ScrollZoom
				if (options[i].substring(0, 10).equals("scrollZoom"))
					optionsModel.setScrollZoom(options[i]);

				// StaticPlt
				if (options[i].substring(0, 10).equals("staticPlot"))
					optionsModel.setStaticPlot(options[i]);

				// DisplayModeBar
				if (options[i].substring(0, 14).equals("displayModeBar"))
					optionsModel.setDisplayModeBar(options[i]);

				// DipslayLogo
				if (options[i].substring(0, 11).equals("displayLogo"))
					optionsModel.setDisplayLogo(options[i]);

				// Responsive
				if (options[i].substring(0, 10).equals("responsive"))
					optionsModel.setResponsive(options[i]);

			}

			return optionsModel;
		} else
			return null;
	}

	/*
	 * END Plotly
	 */

	/**
	 * Return the selected data.<br>
	 * Works with 2D charts only<br>
	 * 
	 * @return Map&ltString, List&ltDouble&gt&gt - Map of selected data.<br>
	 *         <br>
	 *         Keys : <br>
	 *         <ul>
	 *         <li>"curveNumber" : index in data of the trace associated with the
	 *         selected points</li>
	 *         <li>"pointNumber" : index of the selected points</li>
	 *         <li>"x" : x values</li>
	 *         <li>"y" : y values</li>
	 *         </ul>
	 */
	public Map<String, List<Double>> getSelectedDatas() {

		return GraphComp.getSelectedMap();
	}

}
