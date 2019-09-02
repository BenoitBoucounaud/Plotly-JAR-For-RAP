package ui.graphCompsite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.json.JSONObject;

public class GraphComp extends Composite {
	private static final long serialVersionUID = 6419634096566320689L;
	private final RemoteObject remoteObject;
	private static Map<String, List<String>> selectedMap;

	public GraphComp(Composite parent, int style) {
		super(parent, style);

		ClientFileLoader loader = RWT.getClient().getService(ClientFileLoader.class);
		loader.requireJs("js/d3.min.js");
		loader.requireJs("js/plotly.js");
		loader.requireJs("js/plotlyGraph.js");
		remoteObject = RWT.getUISession().getConnection().createRemoteObject("PlotlyGraph");
		remoteObject.set("parent", WidgetUtil.getId(this));

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

	public void getSelection() {

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

	public void setOptions(JSONObject obj) {
		remoteObject.set("options", JsonObject.readFrom(obj.toString()));
	}

	public static Map<String, List<String>> getSelectedMap() {
		return selectedMap;
	}

	public void updateData(JSONObject obj) {
		remoteObject.set("options", JsonObject.readFrom(obj.toString()));
	}

}
