package org.colomoto.biolqm.tool.simulation.assessreachability.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.OHLCChart;
import org.knowm.xchart.OHLCChartBuilder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.Marker;
import org.knowm.xchart.style.markers.None;

/**
 * Plotting facilities using JavaPlot library
 * 
 * @author teras
 */
public class ChartGNUPlot {
	
	/**
	 * Saves a given image in a PNG file
	 * 
	 * @param img
	 *            the image to be saved
	 * @param file
	 *            the PNG file where the image is to be saved
	 * @throws IOException
	 */
	public static void writePNGFile(BufferedImage img, File file) throws IOException {
		// System.out.println("File:"+new File("").getAbsolutePath()+file.toString());
		file.createNewFile();
		ImageIO.write(img, "png", file);
	}

	/**
	 * Creates a chart with statistics on the depth for a list of attractors
	 * 
	 * @param depths
	 *            the list of depths at which an attractor was found
	 * @param title
	 *            the title of the plot
	 * @param xaxis
	 *            the x-axis label
	 * @param yaxis
	 *            the y-axis label
	 * @return the associated plot with the mean depth and error bars associated
	 *         with the list of attractors
	 */
	public static Plot getErrorBars(Map<String, List<Integer>> depths, Map<String, String> names, String title,
			String xaxis, String yaxis) {

		OHLCChart chart = new OHLCChartBuilder().width(800).height(600).title(title).xAxisTitle(xaxis).yAxisTitle(yaxis).build();
		chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);

		int it = 0;
		double overallMax = 0;
		for (String att : depths.keySet()) {
			double[] datapoints = new double[6]; // new
														// double[][]{{0,5,1,8,2,0.4},{1,4,1,8,2,0.4},{2,6,1,8,3,0.4}};
			double mean = MathFunctions.mean(depths.get(att)), std = MathFunctions.std(depths.get(att));
			double min = MathFunctions.min(depths.get(att)), max = MathFunctions.max(depths.get(att));
			overallMax = Math.max(max, overallMax);
			datapoints[0] = it++;
			datapoints[1] = Math.max(min, mean - std);
			datapoints[2] = min;
			datapoints[3] = max;
			datapoints[4] = Math.min(max, mean + std);
			datapoints[5] = 0.4;
		}

		chart.getStyler()
				.setXAxisMin(-1.0).setXAxisMax((double)depths.size())
				.setYAxisMin(0.0).setYAxisMax(overallMax * 1.1 + 1);
		return new InnerPlotX(chart);
	}

	/**
	 * Creates a chart based on the convergence of probabilities per attractor
	 * 
	 * @param dataset
	 *            the evolution of probabilities per attractor as the number of
	 *            iterations increases
	 * @param names
	 * @param space
	 *            number of iterations between two measured points
	 * @param title
	 *            the title of the plot
	 * @param xaxis
	 *            the x-axis label
	 * @param yaxis
	 *            the y-axis label
	 * @return the associated plot with the convergence of probabilities per
	 *         attractor across iterations
	 */
	public static Plot getConvergence(double[][] dataset, List<String> names, int space, String title, String xaxis,
			String yaxis) {
		XYChart chart = new XYChartBuilder().width(600).height(500).title(title).xAxisTitle(xaxis).yAxisTitle(yaxis).build();
		chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
		chart.getStyler().setYAxisMin(0.0).setYAxisMax(1.0);

		Marker nomarker = new None();
		for (int i = 0; i < dataset.length; i++) {
			double[] xdata = new double[dataset[i].length];
			double[] ydata = new double[dataset[i].length];
			for (int j = 0, l2 = dataset[i].length; j < l2; j++) {
				xdata[j] = j * space;
				ydata[j] = dataset[i][j];
			}
			XYSeries s = chart.addSeries(names.get(i), xdata, ydata);
			s.setLineWidth(3);
			s.setMarker(nomarker);

			// TODO: adapt more styling from JavaPlot ?
//			myPlotStyle.setLineType(colorPalette()[i % colors.length]);
		}
		return new InnerPlotX(chart);
	}

	/**
	 * Creates a chart based on the progression of probabilities per state-set (F,
	 * N, A)
	 * 
	 * @param progression
	 *            the evolution of probabilities as the number of iterations
	 *            increases
	 * @param title
	 *            the title of the plot
	 * @param xaxis
	 *            the x-axis label
	 * @param yaxis
	 *            the y-axis label
	 * @return the associated plot with the progression of probabilities on
	 *         state-sets across iterations
	 */
	public static Plot getProgression(List<double[]> progression, String title, String xaxis, String yaxis) {

		XYChart chart = new XYChartBuilder().width(600).height(500).title(title).xAxisTitle(xaxis).yAxisTitle(yaxis).build();
		chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);

		Marker nomarker = new None();
		for (int i = 0; i < 3; i++) {
			double[] ydata = new double[progression.size()];
			for (int k = 0, l = progression.size(); k < l; k++) {
				ydata[k] = progression.get(k)[i];
			}
			XYSeries s = chart.addSeries((i == 0) ? "F" : ((i == 1) ? "N" : "A"), ydata);
			s.setMarker(nomarker);
			s.setLineWidth(3);
		}
		return new InnerPlotX(chart);
	}
}


class InnerPlotX implements Plot {
	Chart inner;

	InnerPlotX(Chart chart) {
		this.inner = chart;
	}

	public BufferedImage asImage() {
		return BitmapEncoder.getBufferedImage(inner);
	}
}


