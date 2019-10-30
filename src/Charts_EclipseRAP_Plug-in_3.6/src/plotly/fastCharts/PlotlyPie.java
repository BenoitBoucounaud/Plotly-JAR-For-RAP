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
 * Invoke this class to build a fast pie chart. <br>
 * Call PlotlyPie(Composite parent, int style, double[][] datas) to obtain this
 * composite.<br>
 * <br>
 * Exemple:<br>
 * double[][] values = { { 20.5, 40.5, 39 }, { 33, 33, 34 } };<br>
 * PlotlyPie pp = new PlotlyPie(HomeComposite, SWT.NONE, values);<br>
 * 
 * @author Benoit Boucounaud
 * @version 1.0
 */
public class PlotlyPie extends Composite {

	private static final long serialVersionUID = 4173410556573336700L;
	private final RemoteObject remoteObject;

	// To rebuild
	private static double[][] fixedDatas;

	private static Map<String, List<String>> selectedMap;
	private static HashMap<String, String> optionsMap = null;

	// Keys for optionsMap
	private static final String chartTitle = "title";
	private static final String legend = "name";
	private static final String showLegend = "showlegend";
	private static final String height = "height";
	private static final String width = "width";

	private static final String marker = "marker";
	private static final String traceColor = "colors";
	private static final String label = "labels";
	private static final String hoverinfo = "hoverinfo";
	private static final String domain = "domain";
	private static final String hole = "hole";
	private static final String grid = "grid";
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
	 * @param datas  double[][] - Datas form : [ [ values1 ], [ values2 ], ... ]
	 * @throws FileNotFoundException
	 */
	public PlotlyPie(Composite parent, int style, double[][] datas) {
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
			buildPie(datas);
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
		buildPie(datas);
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

	private void buildPie(double[][] datas) throws FileNotFoundException {

		// Read JSON file and convert it in a json object
		// All json's value must be string

		fixedDatas = datas;

		String directory = System.getProperty("user.dir");
		String fileName = "json\\plotly\\pie.json";
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

			// values
			str += "values: " + Arrays.toString(datas[i]) + ",";

			Iterator<String> itTrace = trace.keys();
			while (itTrace.hasNext()) {
				String key = itTrace.next();
				String value = (String) trace.get(key);

				if (key.equals(domain)) {

					switch (datas.length % 3) {

					case 0:
						int row = 0;
						if (Math.floor(datas.length / 3) != 0)
							row = (int) (Math.floor(datas.length / 3) - 1);

						value = "{row : " + row + ", column : 2}";
						break;

					default:

						value = "{row : " + ((int) (Math.floor(datas.length / 3))) + ", column : " + (((i + 1) % 3) - 1)
								+ "}";
						break;

					}
				}

				if (optionsMap != null) {
					switch (key) {

					case legend:
						if (optionsMap.get(legend + i) != null)
							value = optionsMap.get(legend + i);
						break;

					case label:
						if (optionsMap.get(label + i) != null)
							value = optionsMap.get(label + i);
						break;

					case marker:
						if (optionsMap.get(traceColor + i) != null)
							value = value.substring(0, 1) + optionsMap.get(traceColor + i)
									+ value.substring(value.length() - 1, value.length());
						break;

					case hoverinfo:
						if (optionsMap.get(hoverinfo + i) != null)
							value = optionsMap.get(hoverinfo + i);
						break;

					case hole:
						if (optionsMap.get(hole + i) != null)
							value = optionsMap.get(hole + i);
						break;

					case domain:
						if (optionsMap.get(domain + i) != null)
							value = optionsMap.get(domain + i);
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

			if (key.equals(grid)) {

				switch (datas.length % 3) {

				case 0:
					int row = 0;
					if (Math.floor(datas.length / 3) != 0)
						row = (int) (Math.floor(datas.length / 3) + 1);
					else
						row = 1;
					value = "{rows : " + row + ", columns : 2}";
					break;

				default:
					int clm = 3;
					if (Math.floor(datas.length / 3) == 0)
						clm = datas.length % 3;
					value = "{rows : " + ((int) (Math.floor(datas.length / 3) + 1)) + ", columns : " + clm + "}";
					break;
				}
			}

			if (optionsMap != null) {

				switch (key) {

				case height:
					if (optionsMap.get(height) != null)
						value = optionsMap.get(height);
					break;

				case width:
					if (optionsMap.get(width) != null)
						value = optionsMap.get(width);
					break;

				case chartTitle:
					if (optionsMap.get(chartTitle) != null)
						value = optionsMap.get(chartTitle);
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
	 * To update chart's height.
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
				buildPie(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To update chart's width.
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
				buildPie(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To add/update a title to the chart
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
				buildPie(fixedDatas);
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
				buildPie(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To add/update Labels. [ [trace1 texts, ...], [trace2 texts, ...], ... ]
	 * 
	 * @param labels String[][] - Arrays of labels (ex : [ ["jan", "feb"],
	 *               ["2018","2019"] ]
	 */
	public void upLabels(String[][] labels) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		for (int i = 0; i < labels.length; i++) {

			for (int j = 0; j < labels[i].length; j++)
				labels[i][j] = "'" + labels[i][j] + "'";

			if (optionsMap.containsKey(label + i) == false)
				optionsMap.put(label + i, Arrays.toString(labels[i]));
			else
				optionsMap.replace(label + i, Arrays.toString(labels[i]));
		}

		if (fixedDatas != null) {
			try {
				buildPie(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To update pie's colors.<br>
	 * When you change color, all marker's values are delete, only new color stay.
	 * 
	 * @param colors String[][] - Arrays of traces colors (ex : [ ["rgb(56, 75,
	 *               126)", "rgb(18, 36, 37)", "rgb(34, 53, 101)", "rgb(36, 55,
	 *               57)", "rgb(6, 4, 4)"], ["rgb(146, 123, 21)", "rgb(177, 180,
	 *               34)", "rgb(206, 206, 40)", "rgb(175, 51, 21)", "rgb(35, 36,
	 *               21)"] ] )
	 */
	public void upColors(String[][] colors) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		for (int i = 0; i < colors.length; i++) {

			for (int j = 0; j < colors[i].length; j++)
				colors[i][j] = "'" + colors[i][j] + "'";

			if (optionsMap.containsKey(traceColor + i) == false)
				optionsMap.put(traceColor + i, "colors :" + Arrays.toString(colors[i]));
			else
				optionsMap.replace(traceColor + i, "colors :" + Arrays.toString(colors[i]));
		}

		if (fixedDatas != null) {
			try {
				buildPie(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To add/update hover infos. [trace1 hoverinfo, trace2 hoverinfo, ...].<br>
	 * https://plot.ly/javascript/reference/#pie-hoverinfo<br>
	 * <br>
	 * Sets hover text elements associated with each sector. If a single string, the
	 * same string appears for all data points. If an array of string, the items are
	 * mapped in order of this trace's sectors.
	 * 
	 * @param infos String[] - Arrays of plot name (ex : ["label+percent",
	 *              "percent"]
	 */
	public void upHoverInfo(String[] infos) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		for (int i = 0; i < infos.length; i++) {

			if (optionsMap.containsKey(hoverinfo + i) == false)
				optionsMap.put(hoverinfo + i, "'" + infos[i] + "'");
			else
				optionsMap.replace(hoverinfo + i, "'" + infos[i] + "'");
		}

		if (fixedDatas != null) {
			try {
				buildPie(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To add/update donuting.<br>
	 * Number between or equal to 0 and 1.<br>
	 * Sets the fraction of the radius to cut out of the pie. Use this to make a
	 * donut chart.
	 * 
	 * @param hole double[] - (ex : [0.2, 0.5, ...])
	 */
	public void upHole(double[] holes) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		for (int i = 0; i < holes.length; i++) {

			if (optionsMap.containsKey(hole + i) == false)
				optionsMap.put(hole + i, String.valueOf(holes[i]));
			else
				optionsMap.replace(hole + i, String.valueOf(holes[i]));
		}

		if (fixedDatas != null) {
			try {
				buildPie(fixedDatas);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To update pie's domain. [ [row, column] ]<br>
	 * 
	 * @param domains int[][] - Arrays of traces domains (ex : [ [0,0], [0,1] ] )
	 */
	public void upDomains(int[][] domains) {

		if (optionsMap == null)
			optionsMap = new HashMap<String, String>();

		for (int i = 0; i < domains.length; i++) {

			if (optionsMap.containsKey(domain + i) == false)
				optionsMap.put(domain + i, "{row :" + domains[i][0] + ", column :" + domains[i][1] + "}");
			else
				optionsMap.replace(domain + i, "{row :" + domains[i][0] + ", column :" + domains[i][1] + "}");
		}

		if (fixedDatas != null) {
			try {
				buildPie(fixedDatas);
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
				buildPie(fixedDatas);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
