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
 * Invoke this class to build a fast Scatter chart. <br>
 * Call PlotlyScatter(Composite parent, int style, double[][][] datas) to obtain
 * this composite.<br>
 * <br>
 * Exemple:<br>
 * double[][][] datas = { { { 1, 2, 3, 4, 5 }, { 6, 7, 8, 9, 10 } }, { { 6, 7,
 * 8, 9, 10 }, { 1, 2, 3, 4, 5 } } }; <br>
 * PlotlyScatter ps = new PlotlyScatter(HomeComposite, SWT.NONE, datas);<br>
 * 
 * @author Benoit Boucounaud
 * @version 1.0
 */
public class PlotlyScatter extends Composite {

	private static final long serialVersionUID = 4173410556573336700L;
	private final RemoteObject remoteObject;

	// To rebuild
	private static double[][][] fixedDatas;

	private static Map<String, List<Double>> selectedMap;
	private static HashMap<String, String> optionsMap = null;

	// Keys for optionsMap
	private static final String chartTitle = "title";
	private static final String xAxisTitle = "xaxis";
	private static final String yAxisTitle = "yaxis";
	private static final String legend = "name";
	private static final String showLegend = "showlegend";
	private static final String height = "height";
	private static final String width = "width";

	/**
	 * Create the composite.</br>
	 * 
	 * 
	 * @param parent A widget which will be the parent of the new instance (cannot
	 *               be null)
	 * @param style  The style of widget to construct
	 * @param datas  double[][][] - Datas form : [ [ [x], [y] ], [ [x], [y] ], ... ]
	 * @throws FileNotFoundException
	 */
	public PlotlyScatter(Composite parent, int style, double[][][] datas) {
		super(parent, style);

		// Cleaner
		optionsMap = null;
		selectedMap = null;

		ClientFileLoader loader = RWT.getClient().getService(ClientFileLoader.class);
		loader.requireJs("js/d3.min.js");
		loader.requireJs("js/plotly.js");
		loader.requireJs("js/plotlyFast.js");
		remoteObject = RWT.getUISession().getConnection().createRemoteObject("PlotlyGraph");
		remoteObject.set("parent", WidgetUtil.getId(this));

		try {
			buildScatter(datas);
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

						List<Double> curveNumberList = new ArrayList<Double>();
						List<Double> pointNumberList = new ArrayList<Double>();
						List<Double> xList = new ArrayList<Double>();
						List<Double> yList = new ArrayList<Double>();

						for (int i = 0; i < arr.size(); i++) {

							JsonObject obj = arr.get(i).asObject();

							curveNumberList.add(Double.valueOf(obj.get("curveNumber").asDouble()));
							pointNumberList.add(Double.valueOf(obj.get("pointNumber").asDouble()));
							xList.add(Double.valueOf(obj.get("x").asDouble()));
							yList.add(Double.valueOf(obj.get("y").asDouble()));
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
	 * @param datas double[][][] - Datas form : [ [ [x], [y] ], [ [x], [y] ], ... ]
	 * 
	 */
	public void updateData(double[][][] datas) throws FileNotFoundException {
		buildScatter(datas);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/**
	 * Return the selected data.<br>
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

		return selectedMap;
	}

	private void buildScatter(double[][][] datas) throws FileNotFoundException {

		// Read JSON file;
		// All json's value must be string

		fixedDatas = datas;

		String directory = System.getProperty("user.dir");
		String fileName = "json\\plotly\\scatter.json";
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

				case height : 
					if (optionsMap.get(height) != null)
						value = optionsMap.get(height);
					break;
					
				case width : 
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

				}
			}

			str += key + " : " + value + ",";
		}
		str = str.substring(0, str.length() - 1) + "}, ";

		// OPTIONS
		str += "options : {";
		Iterator<String> itOptions = options.keys();
		while (itOptions.hasNext()) {
			String key = itOptions.next();
			String value = (String) options.get(key);
			str += key + " : " + value + ",";
		}
		str = str.substring(0, str.length() - 1) + "}";

		str += "}";

		remoteObject.set("options", JsonObject.readFrom(new JSONObject(str).toString()));

	}

	/**
	 * To add/update a title to the chart.
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
				buildScatter(fixedDatas);
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
				buildScatter(fixedDatas);
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
				buildScatter(fixedDatas);
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
				buildScatter(fixedDatas);
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
				buildScatter(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
