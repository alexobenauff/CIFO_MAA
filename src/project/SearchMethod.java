package project;

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public abstract class SearchMethod implements Runnable {

	protected XYSeries series;

	public SearchMethod() {
		JFreeChart chart = Problem.view.getChart();
		XYSeriesCollection sc = (XYSeriesCollection) chart.getXYPlot().getDataset();
		series = sc.getSeries(0);
		series.clear();
	}
}
