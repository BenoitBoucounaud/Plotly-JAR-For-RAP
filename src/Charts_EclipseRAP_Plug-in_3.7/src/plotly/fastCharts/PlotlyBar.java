package plotly.fastCharts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.io.InputStream;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.ClientFileLoader;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.widgets.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Invoke this class to build a fast Bar chart. <br>
 * Call PlotlyBar(Composite parent, int style, double[][][] datas) to obtain
 * this composite.<br>
 * <br>
 * Exemple:<br>
 * String[][][] barDatas = { { { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
 * "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" }, { "20", "14", "25", "16", "18",
 * "22", "19", "15", "12", "16", "14", "17" } }, { { "Jan", "Feb", "Mar", "Apr",
 * "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" }, { "19", "14", "22",
 * "14", "16", "19", "15", "14", "10", "12", "12", "16" } } };<br>
 * PlotlyBar pb = new PlotlyBar(HomeComposite, SWT.NONE, barDatas);<br>
 * 
 * @author Benoit Boucounaud
 * @version 1.0
 */
public class PlotlyBar extends Composite {

	private static final long serialVersionUID = 4173410556573336700L;
	private final RemoteObject remoteObject;

	// To rebuild
	private static String[][][] fixedDatas;

	private static Map<String, List<String>> selectedMap;
	private static HashMap<String, String> optionsMap = null;

	// Keys for optionsMap
	private static final String chartTitle = "title";
	private static final String xAxisTitle = "xaxis";
	private static final String yAxisTitle = "yaxis";
	private static final String legend = "name";
	private static final String showLegend = "showlegend";
	private static final String height = "height";
	private static final String width = "width";

	private static final String marker = "marker";

	private static final String traceColor = "color";
	private static final String text = "text";
	private static final String widthBar = "width";
	private static final String base = "base";
	private static final String barmode = "barmode";
	private static final String bargap = "bargap";
	private static final String bargroupgap = "bargroupgap";
	private static final String showLink = "showLink";
	private static final String scrollZoom = "scroolZoom";
	private static final String staticPlot = "staticPlot";
	private static final String displayModeBar = "displayModeBar";
	private static final String displayLogo = "displaylogo";

	/**
	 * Create the composite.</br>
	 * 
	 * 
	 * @param parent A widget which will be the parent of the new instance (cannot
	 *               be null)
	 * @param style  The style of widget to construct
	 * @param datas  String[][][] - Datas form : [ [ [x], [y] ], [ [x], [y] ], ... ]
	 * @throws FileNotFoundException
	 */
	public PlotlyBar(Composite parent, int style, String[][][] datas) {
		super(parent, style);

		// Cleaner
		optionsMap = null;
		selectedMap = null;

		ClientFileLoader loader = RWT.getClient().getService(ClientFileLoader.class);
		loader.requireJs("js"+File.separator+"d3.min.js");
		loader.requireJs("js"+File.separator+"plotly.js");
		loader.requireJs("js"+File.separator+"plotlyFast.js");
		remoteObject = RWT.getUISession().getConnection().createRemoteObject("PlotlyGraphFast");
		remoteObject.set("parent", WidgetUtil.getId(this));

		try {
			buildBar(datas);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.addDisposeListener(new DisposeListener() {
			private static final long serialVersionUID = 7780300831817645309L;

			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				remoteObject.destroy();
			}
		});

		remoteObject.setHandler(new AbstractOperationHandler() {

			private static final long serialVersionUID = 1L;

			@Override
			public void handleNotify(String eventName, JsonObject data) {
				System.out.println("Notify");
				if ("Selection".equals(eventName)) {
					Event event = new Event();
					event.data = data;
					notifyListeners(SWT.Selection, event);
				}
			}

			public void handleSet(JsonObject properties) {
				System.out.println("********************");
				if (properties != null) {
					if (properties.get("selected") != null) {
						System.out.println(properties.get("selected"));
					}

					if (properties.get("ans") != null) {

						JsonValue arrayValue = properties.get("ans");

						JsonArray arr = arrayValue.asArray();

						List<String> curveNumberList = new ArrayList<String>();
						List<String> pointNumberList = new ArrayList<String>();
						List<String> xList = new ArrayList<String>();
						List<String> yList = new ArrayList<String>();

						for (int i = 0; i < arr.size(); i++) {

							JsonObject obj = arr.get(i).asObject();

							curveNumberList.add(String.valueOf(obj.get("curveNumber")));
							pointNumberList.add(String.valueOf(obj.get("pointNumber")));
							xList.add(String.valueOf(obj.get("x")));
							yList.add(String.valueOf(obj.get("y")));
						}

						selectedMap = new HashMap<>();

						selectedMap.put("curveNumber", curveNumberList);
						selectedMap.put("pointNumber", pointNumberList);
						selectedMap.put("x", xList);
						selectedMap.put("y", yList);
					}

				}
				System.out.println("--------------------");
			}
		});

	}

	@Override
	public void addListener(int eventType, Listener listener) {
		boolean wasListening = isListening(SWT.Selection);
		super.addListener(eventType, listener);
		if (!wasListening)
			remoteObject.listen("Selection", true);

	}

	@Override
	public void removeListener(int eventType, Listener listener) {
		boolean wasListening = isListening(SWT.Selection);
		super.removeListener(eventType, listener);
		if (wasListening && !isListening(SWT.Selection)) {
			remoteObject.listen("Selection", false);
		}
	}

	/**
	 * Update the current chart. <br>
	 * 
	 * @param datas String[][][] - Datas form : [ [ [x], [y] ], [ [x], [y] ], ... ]
	 * 
	 */
	public void updateData(String[][][] datas) throws FileNotFoundException {
		buildBar(datas);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/**
	 * Return the selected data.<br>
	 * 
	 * @return Map&ltString, List&ltString&gt&gt - Map of selected data.<br>
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
	public Map<String, List<String>> getSelectedDatas() {

		return selectedMap;
	}

	private double foundZ(double[] values, double value) {
		double variance = StatUtils.populationVariance(values);
		double sd = Math.sqrt(variance);
		double mean = StatUtils.mean(values);
		double zscore = (value - mean) / sd;
		return zscore;
	}

	private void buildBar(String[][][] datas) throws FileNotFoundException {

		double yMin = 0.0;
		double yMax = 0.0;

		// Search min and max y
		for (int i = 0; i < datas.length; i++) {

			for (int j = 0; j < datas[i][1].length; j++)
				if (datas[i][1][j].substring(0, 1).equals("'")
						&& datas[i][1][j].substring(datas[i][1][j].length() - 1, datas[i][1][j].length()).equals("'"))
					datas[i][1][j] = datas[i][1][j].substring(1, datas[i][1][j].length() - 1);

			for (int j = 0; j < datas[i][1].length; j++) {

				if (NumberUtils.isCreatable(datas[i][1][j])) {

					double[] yValues = Arrays.stream(datas[i][1]).mapToDouble(Double::parseDouble).toArray();

					double value = Double.valueOf(datas[i][1][j]);

					if (yMin == 0.0 && Math.abs(foundZ(yValues, value)) < 3)
						yMin = value;
					else if (yMin > value && Math.abs(foundZ(yValues, value)) < 3)
						yMin = value;

					if (yMax == 0.0 && Math.abs(foundZ(yValues, value)) < 3)
						yMax = value;
					else if (yMax < value && Math.abs(foundZ(yValues, value)) < 3)
						yMax = value;
				}
			}
		}

		for (int i = 0; i < datas.length; i++) {

			for (int j = 0; j < datas[i][0].length; j++)
				if (!datas[i][0][j].substring(0, 1).equals("'")
						&& !datas[i][0][j].substring(datas[i][0][j].length() - 1, datas[i][0][j].length()).equals("'"))
					datas[i][0][j] = "'" + datas[i][0][j] + "'";

			for (int j = 0; j < datas[i][1].length; j++)
				if (!datas[i][1][j].substring(0, 1).equals("'")
						&& !datas[i][1][j].substring(datas[i][1][j].length() - 1, datas[i][1][j].length()).equals("'"))
					datas[i][1][j] = "'" + datas[i][1][j] + "'";
		}

		fixedDatas = datas;

		String directory = System.getProperty("user.dir");
		String fileName = "json"+File.separator+"plotly"+File.separator+"bar.json";
		String absolutePath = directory + File.separator + fileName;

		InputStream input = new FileInputStream(absolutePath);
		JSONTokener tokener = new JSONTokener(input);

		JSONObject jsonObj = new JSONObject(tokener);

		JSONObject trace = jsonObj.getJSONObject("trace");
		JSONObject layout = jsonObj.getJSONObject("layout");
		JSONObject options = jsonObj.getJSONObject("options");

		// DATA
		String str = "{";
		// TRACES
		str += "inputs:[";
		for (int i = 0; i < datas.length; i++) {

			str += "{";

			// x
			str += "x: " + Arrays.toString(datas[i][0]) + ",";
			// y
			str += "y: " + Arrays.toString(datas[i][1]) + ",";

			Iterator<String> itTrace = trace.keys();
			while (itTrace.hasNext()) {
				String key = itTrace.next();
				String value = (String) trace.get(key);

				if (optionsMap != null) {
					switch (key) {

					case legend:
						if (optionsMap.get(legend + i) != null)
							value = optionsMap.get(legend + i);
						break;

					case text:
						if (optionsMap.get(text + i) != null)
							value = optionsMap.get(text + i);
						break;

					case marker:
						if (optionsMap.get(traceColor + i) != null)
							value = value.substring(0, 1) + optionsMap.get(traceColor + i)
									+ value.substring(value.length() - 1, value.length());
						break;

					case widthBar:
						if (optionsMap.get(widthBar + i) != null)
							value = optionsMap.get(widthBar + i);
						break;

					case base:
						if (optionsMap.get(base + i) != null)
							value = optionsMap.get(base + i);
						break;
					}
				}
				str += key + " : " + value + ",";

			}
			str = str.substring(0, str.length() - 1) + " },";
		}
		str = str.substring(0, str.length() - 1) + "], ";

		// LAYOUT
		str += "layout : {";
		Iterator<String> itLayout = layout.keys();
		while (itLayout.hasNext()) {
			String key = itLayout.next();
			String value = (String) layout.get(key);

			if (optionsMap != null) {

				switch (key) {

				case chartTitle:
					if (optionsMap.get(chartTitle) != null)
						value = optionsMap.get(chartTitle);
					break;

				case height:
					if (optionsMap.get(height) != null)
						value = optionsMap.get(height);
					break;

				case width:
					if (optionsMap.get(width) != null)
						value = optionsMap.get(width);
					break;

				case xAxisTitle:
					if (optionsMap.get(xAxisTitle) != null)
						value = value.substring(0, 1) + optionsMap.get(xAxisTitle) + value.substring(1, value.length());
					break;

				case yAxisTitle:
					if (optionsMap.get(yAxisTitle) != null)
						value = value.substring(0, 1) + optionsMap.get(yAxisTitle) + value.substring(1, value.length());
					break;

				case showLegend:
					if (optionsMap.get(legend + 0) != null)
						value = "true";
					break;

				case barmode:
					if (optionsMap.get(barmode) != null)
						value = optionsMap.get(barmode);
					break;

				case bargap:
					if (optionsMap.get(bargap) != null)
						value = optionsMap.get(bargap);
					break;

				case bargroupgap:
					if (optionsMap.get(bargroupgap) != null)
						value = optionsMap.get(bargroupgap);
					break;

				}
			}

			if (key.equals(yAxisTitle) && (yMin != 0.0 || yMax != 0.0))
				str += key + " : " + value.substring(0, 1) + "range : [" + yMin + "," + yMax + "], "
						+ value.substring(1, value.length()) + ",";
			else
				str += key + " : " + value + ",";
		}
		str = str.substring(0, str.length() - 1) + "}, ";

		// OPTIONS
		str += "options : {";
		Iterator<String> itOptions = options.keys();
		while (itOptions.hasNext()) {
			String key = itOptions.next();
			String value = (String) options.get(key);

			if (optionsMap != null) {

				switch (key) {

				case showLink:
					if (optionsMap.get(showLink) != null)
						value = optionsMap.get(showLink);
					break;

				case scrollZoom:
					if (optionsMap.get(scrollZoom) != null)
						value = optionsMap.get(scrollZoom);
					break;

				case staticPlot:
					if (optionsMap.get(staticPlot) != null)
						value = optionsMap.get(staticPlot);
					break;

				case displayModeBar:
					if (optionsMap.get(displayModeBar) != null)
						value = optionsMap.get(displayModeBar);
					break;

				case displayLogo:
					if (optionsMap.get(displayLogo) != null)
						value = optionsMap.get(displayLogo);
					break;
				}
			}

			str += key + " : " + value + ",";
		}
		str = str.substring(0, str.length() - 1) + "}";

		str += "}";

		remoteObject.set("options", JsonObject.readFrom(new JSONObject(str).toString()));

	}

	/**
	 * To add/update chart's title.
	 * 
	 * @param title String - New Chart Title
	 */
	public void upTitle(String title) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		if (optionsMap.containsKey(chartTitle) == false)
			optionsMap.put(chartTitle, "'" + title + "'");
		else
			optionsMap.replace(chartTitle, "'" + title + "'");

		if (fixedDatas != null) {
			try {
				buildBar(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To add/update chart's height.
	 * 
	 * @param heightChart int - New Chart height
	 */
	public void upHeight(int heightChart) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		if (optionsMap.containsKey(height) == false)
			optionsMap.put(height, String.valueOf(heightChart));
		else
			optionsMap.replace(height, String.valueOf(heightChart));

		if (fixedDatas != null) {
			try {
				buildBar(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To add/update chart's width.
	 * 
	 * @param widthChart int - New Chart width
	 */
	public void upWidth(int widthChart) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		if (optionsMap.containsKey(width) == false)
			optionsMap.put(width, String.valueOf(widthChart));
		else
			optionsMap.replace(width, String.valueOf(widthChart));

		if (fixedDatas != null) {
			try {
				buildBar(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To add/update a title to a axis
	 * 
	 * @param axis      String - Axis you want to renamed ("x" or "y").
	 * @param axisTitle String - New title for this axe.
	 */
	public void upAxisTitle(String axis, String axisTitle) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		String axisSelected = "";

		switch (axis) {
		case "x":
			axisSelected = xAxisTitle;
			break;
		case "y":
			axisSelected = yAxisTitle;
			break;
		}

		if (optionsMap.containsKey(axisSelected) == false)
			optionsMap.put(axisSelected, "title: '" + axisTitle + "', ");
		else
			optionsMap.replace(axisSelected, "title: '" + axisTitle + "', ");

		if (fixedDatas != null) {
			try {
				buildBar(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To add/update legends. [trace1 legend, trace2lgend, ...]
	 * 
	 * @param legends String[] - Arrays of plot name (ex : ["", "trace 2"]
	 */
	public void upTracesLegends(String[] legends) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		for (int i = 0; i < legends.length; i++) {

			if (optionsMap.containsKey(legend + i) == false)
				optionsMap.put(legend + i, "'" + legends[i] + "'");
			else
				optionsMap.replace(legend + i, "'" + legends[i] + "'");
		}

		if (fixedDatas != null) {
			try {
				buildBar(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To add/update text elements associated with each (x,y) pair. [ [trace1 texts,
	 * ...], [trace2 texts, ...], ... ]
	 * 
	 * @param texts String[][] - Arrays of texts (ex : [ ["jan", "feb"],
	 *              ["2018","2019"] ]
	 */
	public void upTexts(String[][] texts) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		for (int i = 0; i < texts.length; i++) {

			for (int j = 0; j < texts[i].length; j++)
				texts[i][j] = "'" + texts[i][j] + "'";

			if (optionsMap.containsKey(text + i) == false)
				optionsMap.put(text + i, Arrays.toString(texts[i]));
			else
				optionsMap.replace(text + i, Arrays.toString(texts[i]));
		}

		if (fixedDatas != null) {
			try {
				buildBar(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To update trace's color.<br>
	 * When you change color, all marker's values are delete, only new color stay.
	 * 
	 * @param colors String[] - Arrays of traces colors (ex : [ "rgb(158,202,225)",
	 *               "rgb(8,48,107)"] )
	 */
	public void upColors(String[] colors) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		for (int i = 0; i < colors.length; i++) {

			if (optionsMap.containsKey(traceColor + i) == false)
				optionsMap.put(traceColor + i, "color :'" + colors[i] + "' ");
			else
				optionsMap.replace(traceColor + i, "color :'" + colors[i] + "' ");
		}

		if (fixedDatas != null) {
			try {
				buildBar(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To update bar mode.<br>
	 * Values : "stack" | "group" | "overlay" | "relative"<br>
	 * Determines how bars at the same location coordinate are displayed on the
	 * graph. With "stack", the bars are stacked on top of one another With
	 * "relative", the bars are stacked on top of one another, with negative values
	 * below the axis, positive values above With "group", the bars are plotted next
	 * to one another centered around the shared location. With "overlay", the bars
	 * are plotted over one another, you might need to an "opacity" to see multiple
	 * bars.
	 * 
	 * @param colors String[] - Arrays of traces colors (ex : [ ["jan", "feb"],
	 *               [2018,2019] ]
	 */
	public void upBarMode(String mode) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		if (optionsMap.containsKey(barmode) == false)
			optionsMap.put(barmode, "'" + mode + "'");
		else
			optionsMap.replace(barmode, "'" + mode + "'");

		if (fixedDatas != null) {
			try {
				buildBar(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To update traces's widths.<br>
	 * 
	 * @param widths double[][] - Arrays of traces's widths (ex : [ [0.8, 0.9,
	 *               1.2],[0.5, 0.8, 0.7]])
	 */
	public void upWidths(double[][] widths) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		for (int i = 0; i < widths.length; i++) {

			if (optionsMap.containsKey(widthBar + i) == false)
				optionsMap.put(widthBar + i, Arrays.toString((widths[i])));
			else
				optionsMap.replace(widthBar + i, Arrays.toString((widths[i])));
		}

		if (fixedDatas != null) {
			try {
				buildBar(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To update traces's bases.<br>
	 * 
	 * @param bases double[][] - Arrays of traces colors (ex : [ [-500, -13.2],[-30,
	 *              -25.4]])
	 */
	public void upBases(double[][] bases) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		for (int i = 0; i < bases.length; i++) {

			if (optionsMap.containsKey(base + i) == false)
				optionsMap.put(base + i, Arrays.toString((bases[i])));
			else
				optionsMap.replace(base + i, Arrays.toString((bases[i])));
		}

		if (fixedDatas != null) {
			try {
				buildBar(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To update bar gap.<br>
	 * Number between or equal to 0 and 1
	 * 
	 * @param gap double - gap for the bars.
	 */
	public void upBarGap(double gap) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		if (optionsMap.containsKey(bargap) == false)
			optionsMap.put(bargap, String.valueOf(gap));
		else
			optionsMap.replace(bargap, String.valueOf(gap));

		if (fixedDatas != null) {
			try {
				buildBar(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To update bar group gap.<br>
	 * Number between or equal to 0 and 1
	 * 
	 * @param groupGap double - group gap for the bars.
	 */
	public void upBarGroupGap(double groupGap) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		if (optionsMap.containsKey(bargroupgap) == false)
			optionsMap.put(bargroupgap, String.valueOf(groupGap));
		else
			optionsMap.replace(bargroupgap, String.valueOf(groupGap));

		if (fixedDatas != null) {
			try {
				buildBar(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To update plot's options.<br>
	 * 
	 * @param option int - Option to change : <br>
	 *               <ul>
	 *               <li>1 : showLink</li>
	 *               <li>2 : scrollZoom</li>
	 *               <li>3 : staticPlot</li>
	 *               <li>4 : displayModeBar</li>
	 *               <li>5 : displaylogo</li>
	 *               </ul>
	 * @param bool   boolean - Option's value.
	 * 
	 */
	public void upOptions(int option, boolean bool) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		switch (option) {

		case 1:
			if (!optionsMap.containsKey(showLink))
				optionsMap.put(showLink, String.valueOf(bool));
			else
				optionsMap.replace(showLink, String.valueOf(bool));

		case 2:
			if (!optionsMap.containsKey(scrollZoom))
				optionsMap.put(scrollZoom, String.valueOf(bool));
			else
				optionsMap.replace(scrollZoom, String.valueOf(bool));

		case 3:
			if (!optionsMap.containsKey(staticPlot))
				optionsMap.put(staticPlot, String.valueOf(bool));
			else
				optionsMap.replace(staticPlot, String.valueOf(bool));

		case 4:
			if (!optionsMap.containsKey(displayModeBar))
				optionsMap.put(displayModeBar, String.valueOf(bool));
			else
				optionsMap.replace(displayModeBar, String.valueOf(bool));

		case 5:
			if (!optionsMap.containsKey(displayLogo))
				optionsMap.put(displayLogo, String.valueOf(bool));
			else
				optionsMap.replace(displayLogo, String.valueOf(bool));

		}

		if (fixedDatas != null) {
			try {
				buildBar(fixedDatas);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

}
