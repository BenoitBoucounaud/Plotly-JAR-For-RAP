package plotly.blank;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
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
 * Invoke this class to build a personalised chart. <br>
 * Call PlotlyBlank(Composite parent, int style, String traces, String layout,
 * String options) to obtain this composite.<br>
 * Traces, layout and options must be written as they could be in
 * "Plotly.newPlot (this.element.id, traces, layout, options);" as a block<br>
 * <br>
 * <br>
 * Exemple:<br>
 * String traces = "[{ title:'Before'," + "x:
 * [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36],"
 * + "y:
 * [64,66,62,63,61,64,62,66,64,61,63,64,60,64,63,61,63,62,66,62,62,65,61,66,64,61,62,64,63,61,65,61,61,63,62,62,62],"
 * + "type: 'scatter', mode: 'lines+markers', hovermode: 'closest'," + "marker:
 * {color: 'rgb(255,50,50)'}, yaxis : 'y2' }," + "{ title:'after'," + "x:
 * [0,1,2]," + "y: [64,66,62]," + "type: 'bar', mode: 'markers', hovermode:
 * 'closest', yaxis : 'y1' }]";<br>
 * String layout = "{title: 'Global Emissions 1990-2011'," + "legend: { yref:
 * 'paper', font: { family: 'Arial, sans-serif', size: 20, color: 'grey'}}, " +
 * " yaxis: { title: 'yaxis title', titlefont: {color: '#1f77b4'}, tickfont:
 * {color: '#1f77b4'}, side : 'right'}," + " yaxis2: { title: 'yaxis2 title',
 * titlefont: {color: '#ff7f0e'}," + " tickfont: {color: '#ff7f0e'}, overlaying:
 * 'y', side : 'left'} }";<br>
 * String options = "{ displayModeBar : true, reponsive : true }";<br>
 * 
 * PlotlyBlank pblank = new PlotlyBlank(HomeComposite, SWT.NONE, traces, layout,
 * options);<br>
 * 
 * @author Benoit Boucounaud
 * @version 1.0
 */
public class PlotlyBlank extends Composite {

	private static final long serialVersionUID = 4173410556573336700L;
	private final RemoteObject remoteObject;

	private static Map<String, List<String>> selectedMap;

	/**
	 * Create the composite.</br>
	 * 
	 * 
	 * @param parent  A widget which will be the parent of the new instance (cannot
	 *                be null)
	 * @param style   The style of widget to construct
	 * @param traces  String - Chart's traces.
	 * @param layout  String - Chart's layout.
	 * @param options String - Chart's options.
	 * @throws FileNotFoundException
	 */
	public PlotlyBlank(Composite parent, int style, String traces, String layout, String options) {
		super(parent, style);

		selectedMap = null;

		ClientFileLoader loader = RWT.getClient().getService(ClientFileLoader.class);
		loader.requireJs("js/d3.min.js");
		loader.requireJs("js/plotly.js");
		loader.requireJs("js/plotlyBlank.js");
		remoteObject = RWT.getUISession().getConnection().createRemoteObject("PlotlyGraphBlank");
		remoteObject.set("parent", WidgetUtil.getId(this));

		buildBlank(traces, layout, options);

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
	 * @param traces  String - Chart's traces.
	 * @param layout  String - Chart's layout.
	 * @param options String - Chart's options.
	 * 
	 */
	public void updateData(String traces, String layout, String options) throws FileNotFoundException {
		buildBlank(traces, layout, options);
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

	private void buildBlank(String traces, String layout, String options) {

		String js = "{ traces : " + traces;

		if (layout != null)
			js += ", layout : " + layout;
		if (options != null)
			js += ", options : " + options;
		js += "}";

//		System.out.println(js);

		remoteObject.set("options", JsonObject.readFrom(new JSONObject(js).toString()));
	}

}
