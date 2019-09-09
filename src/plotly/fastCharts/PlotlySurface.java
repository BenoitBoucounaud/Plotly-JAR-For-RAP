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
 * Invoke this class to build a fast Surface chart. <br>
 * Call PlotlySurface(Composite parent, int style, double[][] datas) to obtain
 * this composite.<br>
 * <br>
 * Exemple:<br>
 * double[][] datas = { { 20.5, 40.5, 39 }, { 33, 33, 34 } };<br>
 * PlotlySurface ps = new PlotlySurface(HomeComposite, SWT.NONE, values);<br>
 * 
 * @author Benoit Boucounaud
 * @version 1.0
 */
public class PlotlySurface extends Composite {

	private static final long serialVersionUID = 4173410556573336700L;
	private final RemoteObject remoteObject;

	// To rebuild
	private static double[][] fixedDatas;

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

	private static final String zAxisTitle = "zaxis";
	private static final String scene = "scene";
	private static final String traceColor = "colorscale";
	private static final String showScale = "showscale";
	private static final String x = "x";
	private static final String y = "y";
	private static final String z = "z";
	private static final String hoverinfo = "hoverinfo";

	/**
	 * Create the composite.</br>
	 * 
	 * 
	 * @param parent A widget which will be the parent of the new instance (cannot
	 *               be null)
	 * @param style  The style of widget to construct
	 * @param datas  double[][]
	 */
	public PlotlySurface(Composite parent, int style, double[][] datas) {
		super(parent, style);

		// Cleaner
		optionsMap = null;
		selectedMap = null;

		ClientFileLoader loader = RWT.getClient().getService(ClientFileLoader.class);
		loader.requireJs("js/d3.min.js");
		loader.requireJs("js/plotly.js");
		loader.requireJs("js/plotlyFast.js");
		remoteObject = RWT.getUISession().getConnection().createRemoteObject("PlotlyGraphFast");
		remoteObject.set("parent", WidgetUtil.getId(this));

		try {
			buildSurface(datas);
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
	 * @param datas double[][]
	 * 
	 */
	public void updateData(double[][] datas) throws FileNotFoundException {
		buildSurface(datas);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	private void buildSurface(double[][] datas) throws FileNotFoundException {

		// Read JSON file;
		// All json's value must be string

		fixedDatas = datas;

		String directory = System.getProperty("user.dir");
		String fileName = "json\\plotly\\surface.json";
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

		str += "{";

		// z
		str += "z: " + Arrays.deepToString(datas) + ",";

		Iterator<String> itTrace = trace.keys();
		while (itTrace.hasNext()) {
			String key = itTrace.next();
			String value = (String) trace.get(key);

			if (optionsMap != null) {
				switch (key) {

				case legend:
					if (optionsMap.get(legend) != null)
						value = optionsMap.get(legend);
					break;

				case traceColor:
					if (optionsMap.get(traceColor) != null)
						value = optionsMap.get(traceColor);
					break;

				case x:
					if (optionsMap.get(x) != null)
						value = optionsMap.get(x);
					break;

				case y:
					if (optionsMap.get(y) != null)
						value = optionsMap.get(y);
					break;

				case showScale:
					if (optionsMap.get(showScale) != null)
						value = optionsMap.get(showScale);
					break;

				case hoverinfo:
					if (optionsMap.get(hoverinfo) != null)
						value = optionsMap.get(hoverinfo);
					break;

				}
			}
			str += key + " : " + value + ",";

		}
		str = str.substring(0, str.length() - 1) + " },";

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

				case showLegend:
					if (optionsMap.get(legend) != null)
						value = "true";
					break;

				case scene:
					if (optionsMap.get(xAxisTitle) != null)
						value = value.subSequence(0, value.length() - 1) + optionsMap.get(xAxisTitle)
								+ value.subSequence(value.length() - 1, value.length());
					if (optionsMap.get(yAxisTitle) != null)
						value = value.subSequence(0, value.length() - 1) + optionsMap.get(yAxisTitle)
								+ value.subSequence(value.length() - 1, value.length());
					if (optionsMap.get(zAxisTitle) != null)
						value = value.subSequence(0, value.length() - 1) + optionsMap.get(zAxisTitle)
								+ value.subSequence(value.length() - 1, value.length());
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
	 * To add/update title to the chart
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
				buildSurface(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To add/update height of the chart
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
				buildSurface(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To add/update width of the chart
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
				buildSurface(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To add/update title to a axis
	 * 
	 * @param axis      String - Axis you want to renamed ("x" or "y" or "z").
	 * @param axisTitle String - New title for this axe.
	 */
	public void upAxisTitle(String axis, String axisTitle) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		String axisSelected = "";
		String value = "";

		switch (axis) {
		case x:
			axisSelected = xAxisTitle;
			value = ", xaxis : {title :'" + axisTitle + "'}";
			break;
		case y:
			axisSelected = yAxisTitle;
			value = ", yaxis : {title :'" + axisTitle + "'}";
			break;

		case z:
			axisSelected = zAxisTitle;
			value = ", zaxis : {title :'" + axisTitle + "'}";
			break;
		}

		if (optionsMap.containsKey(axisSelected) == false)
			optionsMap.put(axisSelected, value);
		else
			optionsMap.replace(axisSelected, value);

		if (fixedDatas != null) {
			try {
				buildSurface(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To add/update legends.
	 * 
	 * @param legends String - Legend of the trace (his name).
	 */
	public void upTracesLegends(String legends) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		if (optionsMap.containsKey(legend) == false)
			optionsMap.put(legend, "'" + legends + "'");
		else
			optionsMap.replace(legend, "'" + legends + "'");

		if (fixedDatas != null) {
			try {
				buildSurface(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To update surface's color.<br>
	 * 
	 * 
	 * @param colors String[] - Arrays of traces colors, lowest to highest value (ex
	 *               : [ "rgb(158,202,225)", "rgb(8,48,107)"] )
	 */
	public void upColors(String[] colors) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		String str = "[";

		for (int i = 0; i < colors.length; i++)
			if (i == 0)
				str += "[" + Double.valueOf((double) (i) / (double) colors.length) + ", '" + colors[i] + "'],";
			else
				str += "[" + Double.valueOf((double) (i + 1) / (double) colors.length) + ", '" + colors[i] + "'],";

		str = str.subSequence(0, str.length() - 1) + "]";

		if (optionsMap.containsKey(traceColor) == false)
			optionsMap.put(traceColor, str);
		else
			optionsMap.replace(traceColor, str);

		if (fixedDatas != null) {
			try {
				buildSurface(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To update/add x or y coordinates.<br>
	 * 
	 * 
	 * @param axis        String - Axis you want coordinates
	 * @param coordinates String[] - Coordinates
	 */
	public void upCoordinates(String axis, String[] coordinates) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		if (axis.equals("x")) {

			for (int j = 0; j < coordinates.length; j++)
				coordinates[j] = "'" + coordinates[j] + "'";

			if (optionsMap.containsKey(x) == false)
				optionsMap.put(x, Arrays.toString(coordinates));
			else
				optionsMap.replace(x, Arrays.toString(coordinates));
		}

		if (axis.equals("y")) {

			for (int j = 0; j < coordinates.length; j++)
				coordinates[j] = "'" + coordinates[j] + "'";

			if (optionsMap.containsKey(y) == false)
				optionsMap.put(y, Arrays.toString(coordinates));
			else
				optionsMap.replace(y, Arrays.toString(coordinates));
		}

		if (fixedDatas != null) {
			try {
				buildSurface(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To show scale or not.
	 * 
	 * @param scale boolean
	 */
	public void showScale(boolean scale) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		if (optionsMap.containsKey(showScale) == false)
			optionsMap.put(showScale, String.valueOf(scale));
		else
			optionsMap.replace(showScale, String.valueOf(scale));

		if (fixedDatas != null) {
			try {
				buildSurface(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To update hoverInfo.<br>
	 * Info : <br>
	 * https://plot.ly/javascript/reference/#surface-hoverinfo<br>
	 * <br>
	 * Sets hover text elements associated with each sector. If a single string, the
	 * same string appears for all data points. If an array of string, the items are
	 * mapped in order of this trace's sectors.
	 * 
	 * @param infos String - Determines which trace information appear on hover. If
	 *              `none` or `skip` are set, no information is displayed upon
	 *              hovering.
	 */
	public void upHoverInfo(String infos) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		if (optionsMap.containsKey(hoverinfo) == false)
			optionsMap.put(hoverinfo, "'" + infos + "'");
		else
			optionsMap.replace(hoverinfo, "'" + infos + "'");

		if (fixedDatas != null) {
			try {
				buildSurface(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
