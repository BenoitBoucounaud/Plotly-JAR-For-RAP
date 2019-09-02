package plotly.factory;

import plotly.model.Layout3DModel;
import plotly.model.OptionsModel;
import plotly.model.TraceModel;

public class SurfaceFactory {

	public String createScript(TraceModel[] tracesModels, Layout3DModel layoutModel, OptionsModel optionsModel) {

		String chart = "{";
		chart += tracesCreation(tracesModels);
		chart += layoutCreation(layoutModel);
		chart += optionsCreation(optionsModel);
		chart += "}";

		return chart;

	}

	private String tracesCreation(TraceModel[] tracesModels) {

		String traces = "traces:[";

		for (int i = 0; i < tracesModels.length; i++) {
			traces += "{";

			// Datas
			traces += tracesModels[i].getData() + ", ";

			// TYPE
			traces += "type : '" + tracesModels[i].getType() + "' ,";

			// Contours
			if (tracesModels[i].getContours() != null)
				traces += tracesModels[i].getContours() + " ,";
			else
				traces += " contours: { z: { show:true, usecolormap: true, highlightcolor:\"#42f462\", project:{z: true}}}, ";

			// NAME
			if (tracesModels[i].getName() != null)
				traces += tracesModels[i].getName() + " ,";

			// TEXT
			if (tracesModels[i].getText() != null)
				traces += tracesModels[i].getText() + " ,";

			// OTHER OPTIONS
			if (tracesModels[i].getOtherOption() != null)
				traces += tracesModels[i].getOtherOption() + " ,";

			traces = traces.substring(0, traces.length() - 1) + "}, ";
		}
		traces = traces.substring(0, traces.length() - 1) + "], ";
		
		return traces;

	}

	public String layoutCreation(Layout3DModel layoutModel) {
		String layout = " layout : {";

		if (layoutModel != null) {

			// Title
			if (layoutModel.getTitle() != null)
				layout += layoutModel.getTitle() + ",";

			// Scene
			if (layoutModel.getScene() != null)
				layout += layoutModel.getScene() + ",";
			else
				layout += "scene:{ camera: {eye: { x: -1.4, y: -1.4, z: 1.5 }}}, ";

			// Legend
			if (layoutModel.getLegend() != null)
				layout += layoutModel.getLegend() + ",";

			// showLegend
			if (layoutModel.getShowLegend() != null)
				layout += layoutModel.getShowLegend() + ",";
			else if (layoutModel.getLegend() != null)
				layout += " showlegend : true,";
			else
				layout += " showlegend : false,";

			// Autosize
			if (layoutModel.getAutoSize() != null)
				layout += layoutModel.getAutoSize() + ",";
			else if (layoutModel.getHeight() != null || layoutModel.getWidth() != null)
				layout += "autosize : false,";
			else
				layout += "autosize : true,";

			// Height
			if (layoutModel.getHeight() != null)
				layout += layoutModel.getHeight() + ",";

			// Width
			if (layoutModel.getWidth() != null)
				layout += layoutModel.getWidth() + ",";

			// Margin
			if (layoutModel.getMargin() != null)
				layout += layoutModel.getMargin() + ",";
			else
				layout += "margin: { l: 50, r: 50, b: 50, t: 50 }, ";

			// Annotations
			if (layoutModel.getAnnotations() != null)
				layout += layoutModel.getAnnotations() + ",";

		} else {
			layout += " showlegend : false, autosize : true , margin: { l: 50, r: 50, b: 50, t: 50 }, ";
		}

		layout = layout.substring(0, layout.length() - 1) + " }, ";
		return layout;
	}

	public String optionsCreation(OptionsModel optionsModel) {
		String options = " options : {";

		if (optionsModel != null) {
			// scroolzoom
			if (optionsModel.getScrollZoom() != null)
				options += optionsModel.getScrollZoom() + ", ";
			else
				options += " scrollZoom : true, ";

			// staticPlot
			if (optionsModel.getStaticPlot() != null)
				options += optionsModel.getStaticPlot() + ", ";

			// displayModeBar
			if (optionsModel.getDisplayModeBar() != null)
				options += optionsModel.getDisplayModeBar() + ", ";
			else
				options += " displayModeBar : true, ";

			// displayLago
			if (optionsModel.getDisplayLogo() != null)
				options += optionsModel.getDisplayLogo() + ", ";
			else
				options += " displaylogo : false, ";

			// responsive
			if (optionsModel.getResponsive() != null)
				options += optionsModel.getResponsive() + ", ";
			else
				options += " responsive : true, ";

		} else {
			options += "scrollZoom : true, displayModeBar : true, displaylogo: false, responsive: true,";
		}

		options = options.substring(0, options.length() - 1) + " }";
		return options;
	}

}
