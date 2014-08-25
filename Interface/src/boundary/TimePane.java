package boundary;

import java.awt.FlowLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.ComboBoxModel;

import arch.Cell;
import domain.Configuration;
import domain.Dominoes;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.geometry.*;


public class TimePane extends Pane{

	private Label labelTime;
	private Label labelDatabase;
	
	private ComboBox comboBoxTime;
	private ComboBox comboBoxDatabase;
	
	private BarChart<String, Number> barChart;
	private IntervalSlider slider;
	
	public TimePane(){
		
		this.setHeight(Configuration.height/2);
		this.setWidth(Configuration.width);
		
		labelTime = new Label("Period");
		labelDatabase = new Label("Repository");
		
		comboBoxTime = new ComboBox();
		ObservableList<String> itemsTime = FXCollections.observableArrayList();
		
        itemsTime.add("1 Month");
        itemsTime.add("3 Month");
        itemsTime.add("6 Month");
        
        comboBoxTime.setItems(itemsTime);
        comboBoxTime.setValue(itemsTime.get(0));
        Tooltip.install(comboBoxTime, new Tooltip("specify the period to work in the repository"));
        
        comboBoxDatabase = new ComboBox();
        comboBoxDatabase.setMaxWidth(130);
        ObservableList<String> itemsDatabase = FXCollections.observableArrayList();
        
        //receber do banco de dados
        itemsDatabase.add("database 0");
        itemsDatabase.add("database 1");
        itemsDatabase.add("database 2");
		
        comboBoxDatabase.setItems(itemsDatabase);
        comboBoxDatabase.setValue(itemsDatabase.get(0));
        Tooltip.install(comboBoxDatabase, new Tooltip("select the repository to work with in the database"));
        
        GridPane gridPaneConfigurations = new GridPane();
        gridPaneConfigurations.setHgap(20);
        gridPaneConfigurations.setVgap(10);
        gridPaneConfigurations.setPrefWidth(200);
        gridPaneConfigurations.add(labelTime, 0, 0);
        gridPaneConfigurations.add(comboBoxTime, 1, 0);
        gridPaneConfigurations.add(labelDatabase, 0, 1);
        gridPaneConfigurations.add(comboBoxDatabase, 1, 1);
        
		final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        
        xAxis.setLabel("label X");
        xAxis.setPrefHeight(30);
        yAxis.setLabel("label Y");
        yAxis.setPrefWidth(50 + 10 * yAxis.getLabel().split("\\n").length);

        this.barChart = new BarChart<String, Number>(xAxis, yAxis);
        this.barChart.setAnimated(false);        
        this.barChart.setPrefHeight(this.getHeight()/4);
        this.barChart.setPrefWidth(this.getWidth() - gridPaneConfigurations.getPrefWidth());
        
        double x = yAxis.getPrefWidth() + yAxis.getTickLength() + yAxis.getTickLabelGap() + yAxis.getTickUnit();
        double y = xAxis.getPrefHeight() + xAxis.getTickLength() + xAxis.getTickLabelGap() + xAxis.getTickLabelFont().getSize();
		
		slider = new IntervalSlider(0, 1, 0.2, 0.8, this.barChart.getPrefWidth() - x - 14);
		slider.setTranslateX(x);
		slider.setTranslateY(-y + slider.getHeight());
		
		
		GridPane pane = new GridPane();
		pane.add(gridPaneConfigurations, 0, 0);		
		pane.add(new FlowPane(barChart,slider), 1, 0);
		pane.setGridLinesVisible(true);
		
		pane.setPrefHeight(Configuration.width/2);
		this.getChildren().add(pane);
		
	}
}
