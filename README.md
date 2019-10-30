
# Plotly-JAR-For-RAP

### Hide your data and chart options.
### Data selection option from the chart.


Easy way to quickly create a plotly chart as a Composite.

Use MainBuilder class to create a complexe chart, use a fast_chart class to quickly build a specific type chart (more detail in doc).

Import content of the "js" folder in WebContent/js.

Import the "json" folder in the user working directory.

Version:
- 3.1 : multi-2D type chart added to main builder.
- 3.2 : function getSelectedData() of MainBuilder and PlotlyScatter now return Map<String, List<String>>.
- 3.3 : **new class PlotlyBlank** : make your own chart from A to Z. Fix div naming bug.
- 3.4 : Fix javaScript functions names bug and remoteObject bug.
- 3.5 : New function for the FastCharts : upOptions. Set the yaxis range according to the normal distribution law for FastCharts.
- 3.6 : Remove the unnecessary GraphComp and Main class.
