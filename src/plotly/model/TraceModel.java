package plotly.model;

public class TraceModel {

	// Model for ONE trace
	// plotly doc : https://plot.ly/javascript/reference/#scatter

	private String type;
	private String data;
	private String mode; // FOR SCATTER, Any combination of "lines", "markers", "text" joined with a "+" OR "none".

	private String name; // trace name
	private String marker; //FOR SCATTER, Sets the marker symbol type.
	private String text; // Sets text elements associated with each (x,y) pair.

	private String contours; //FOR SURFACE, show the z axis (or other) on 2D

	private String otherOption; // Any other options

	public TraceModel() {
		super();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMarker() {
		return marker;
	}

	public void setMarker(String marker) {
		this.marker = marker;
	}

	public String getContours() {
		return contours;
	}

	public void setContours(String contours) {
		this.contours = contours;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getOtherOption() {
		return otherOption;
	}

	public void setOtherOption(String otherOption) {
		this.otherOption = otherOption;
	}

}
