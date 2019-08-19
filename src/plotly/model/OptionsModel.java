package plotly.model;

public class OptionsModel {

	private String scrollZoom;
	private String staticPlot;
	private String displayModeBar;
	private String displayLogo;
	private String responsive;

	public OptionsModel() {
		super();
	}

	public String getScrollZoom() {
		return scrollZoom;
	}

	public void setScrollZoom(String scrollZoom) {
		this.scrollZoom = scrollZoom;
	}

	public String getStaticPlot() {
		return staticPlot;
	}

	public void setStaticPlot(String staticPlot) {
		this.staticPlot = staticPlot;
	}

	public String getDisplayModeBar() {
		return displayModeBar;
	}

	public void setDisplayModeBar(String displayModeBar) {
		this.displayModeBar = displayModeBar;
	}

	public String getDisplayLogo() {
		return displayLogo;
	}

	public void setDisplayLogo(String displayLogo) {
		this.displayLogo = displayLogo;
	}

	public String getResponsive() {
		return responsive;
	}

	public void setResponsive(String responsive) {
		this.responsive = responsive;
	}

}
