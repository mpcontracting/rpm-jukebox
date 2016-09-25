package uk.co.mpcontracting.rpmjukebox.visualiser.impl;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.visualizer.AbstractVisualizer;

@Slf4j
public class EqVisualizer extends AbstractVisualizer {

	private Canvas canvas;
	private GraphicsContext g;
	
	public EqVisualizer(Pane pane) {
		super(pane);

		canvas = new Canvas();
		canvas.setLayoutX(0);
		canvas.setLayoutY(0);

		g = canvas.getGraphicsContext2D();
		
		pane.getChildren().add(canvas);
	}
	
	@Override
	protected void paneWidthChanged(double width) {
		canvas.setWidth(width);
		
		drawAxes();
	}

	@Override
	protected void paneHeightChanged(double height) {
		canvas.setHeight(height);
		
		drawAxes();
	}
	
	private void drawAxes() {
		g.setFill(Color.web("#474747"));
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		double yAxisX = (canvas.getWidth() / 100.0) * 5;
		double yAxisTop = (canvas.getHeight() / 100.0) * 5;
		double yAxisBottom = canvas.getHeight() - yAxisTop;

		double xAxisLeft = yAxisX;
		double xAxisRight = canvas.getWidth() - xAxisLeft;
		
		g.beginPath();
		g.setStroke(Color.web("#c3c3c3"));
		g.setLineWidth(1.0);
		g.moveTo(yAxisX - 0.5, yAxisTop - 0.5);
		g.lineTo(yAxisX - 0.5, yAxisBottom - 0.5);
		g.lineTo(xAxisRight - 0.5, yAxisBottom - 0.5);
		g.stroke();
		g.closePath();
	}

	@Override
	protected void drawFrame(double timestamp, double duration, float[] magnitudes, float[] phases) {
		drawAxes();
	}
}
