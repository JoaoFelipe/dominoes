/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package boundary;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Control;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author Daniel
 */
public class IntervalSlider extends Control {

    private Group slider;
    
    private Rectangle backgroud;
    private Rectangle line;
    private Polygon pointMin;
    private Polygon pointMax;

    private double min;
    private double max;
    private double valueMin;
    private double valueMax;
    
    private double srcSceneX;
    private double srcTranslateX;
    
    public IntervalSlider(double min, double max, double valueMin, double valueMax, double width) throws IllegalArgumentException{
        this.min = min;
        this.max = max;
        this.valueMin = valueMin;
        this.valueMax = valueMax;
        this.setHeight(27);
        this.setWidth(width);
        this.setMin(min);
        this.setMax(max);
        this.setValueMin(valueMin);
        this.setValueMax(valueMax);

        this.initialize();
    }

    public double getMin() {
        return min;
    }

    private void setMin(double min) throws IllegalArgumentException{
        if(min > max){
            throw new IllegalArgumentException("Invalid argument.\nThe IntervalSlider.Min attribute not is valid");
        }
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    private void setMax(double max) throws IllegalArgumentException{
        if(max < min){
            throw new IllegalArgumentException("Invalid argument.\nThe IntervalSlider.Max attribute not is valid");
        }
        this.max = max;
    }

    public double getValueMin() {
        return valueMin;
    }

    private void setValueMin(double value) {
        if(value < this.min || value > this.valueMax){
            value = this.min;
        }
        this.valueMin = value;
    }

    public double getValueMax() {
        return valueMax;
    }

    private void setValueMax(double value) {
        if(value > this.max || value < this.valueMin){
            value = this.max;
        }
        this.valueMax = value;
    }

    private void initialize() {
        Color stroke = new Color(0,116.0/255.0, 154.0/255.0, 1);
        Color selectedBackground = new Color(0,150.0/255.0, 201.0/255.0, 1);
        Color selectedPointer = new Color(88.0/255.0,188.0/255.0, 222.0/255.0, 1);
        
        Double size = new Double(7.0);
        
        Double[] pointerShape = new Double[]{
            0.5 * size, 0.0 * size,
            0.0 * size, 0.5 * size,
            0.0 * size, 2.0 * size,
            1.0 * size, 2.0 * size,
            1.0 * size, 0.5 * size
        };

        this.setMinHeight(this.getHeight());
        this.setMinWidth(this.getWidth());
        
        this.backgroud = new Rectangle(this.getWidth(), 27);
        this.backgroud.setFill(Color.WHITE);
        
        this.line = new Rectangle(this.backgroud.getWidth() - 18, 6);
        this.line.setArcHeight(5);
        this.line.setArcWidth(5);
        this.line.setFill(Color.WHITE);
        this.line.setStroke(stroke);
        this.line.setTranslateX(this.backgroud.getWidth()/2 - this.line.getWidth()/2);
        this.line.setTranslateY(this.backgroud.getHeight()/2 - this.line.getHeight()/2);
        
        this.pointMin = new Polygon();
        this.pointMin.getPoints().addAll(pointerShape);
        this.pointMin.setFill(Color.WHITESMOKE);
        this.pointMin.setStroke(stroke);
        this.pointMin.setTranslateX((this.getValueMin())/(max - min) * (this.line.getWidth()) + this.line.getTranslateX() - size/2);
        this.pointMin.setTranslateY(this.backgroud.getHeight()/2 - size);
        
        this.pointMax = new Polygon();
        this.pointMax.getPoints().addAll(pointerShape);
        this.pointMax.setFill(Color.WHITESMOKE  );
        this.pointMax.setStroke(stroke);
        this.pointMax.setTranslateX((this.getValueMax())/(max - min) * (this.line.getWidth()) + this.line.getTranslateX() - size/2);
        this.pointMax.setTranslateY(this.backgroud.getHeight()/2 - size);
    
        this.slider = new Group();
        this.slider.getChildren().addAll(this.backgroud,
                this.line,
                this.pointMin,
                this.pointMax);
        this.slider.setTranslateX(0);
        this.slider.setTranslateY(0);
        
        this.pointMin.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                pointMin.setFill(Color.WHITE);
            }
        });
        this.pointMin.setOnMouseExited(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                pointMin.setFill(Color.WHITESMOKE);
            }
        });
        this.pointMin.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                
                srcSceneX = event.getSceneX();
                srcTranslateX = pointMin.getTranslateX();
                
                pointMin.toFront();
                
                pointMin.setStroke(selectedPointer);
                pointMin.setStrokeWidth(2);
                backgroud.setFill(Color.WHITE);
            }
        });
        this.pointMin.setOnMouseReleased(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                pointMin.setStroke(stroke);
                pointMin.setStrokeWidth(1);
            }
        });
        this.pointMin.setOnMouseDragged(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                double offsetX = event.getSceneX() - srcSceneX;
                double newTranslateX = srcTranslateX + offsetX - size/2;

                pointMin.setTranslateX(newTranslateX);
                valueMin = min + ((max - min) * (pointMin.getTranslateX() - line.getTranslateX() + size/2)/(line.getWidth()));
                if(newTranslateX < line.getTranslateX() - size/2){
                    pointMin.setTranslateX(line.getTranslateX() - size/2);
                    valueMin = min;
                }else if(newTranslateX > pointMax.getTranslateX()){
                    pointMin.setTranslateX(pointMax.getTranslateX() - size/2);
                    valueMin = valueMax;
                }
            }
        });
        
        this.pointMax.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                pointMax.setFill(Color.WHITE);
            }
        });
        this.pointMax.setOnMouseExited(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                pointMax.setFill(Color.WHITESMOKE);
            }
        });
        this.pointMax.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                srcSceneX = event.getSceneX();
                srcTranslateX = pointMax.getTranslateX();
                
                pointMax.toFront();
                
                pointMax.setStroke(selectedPointer);
                pointMax.setStrokeWidth(2);
                backgroud.setFill(Color.WHITE);
            }
        });
        this.pointMax.setOnMouseReleased(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                pointMin.setStroke(stroke);
                pointMin.setStrokeWidth(1);
            }
        });
        this.pointMax.setOnMouseDragged(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                double offsetX = event.getSceneX() - srcSceneX;
                double newTranslateX = srcTranslateX + offsetX - size/2;

                pointMax.setTranslateX(newTranslateX);
                
                valueMax = min + ((max - min) * (pointMax.getTranslateX() - line.getTranslateX() + size/2)/(line.getWidth()));
                if(newTranslateX > line.getTranslateX() + line.getWidth() - size/2 ){
                    pointMax.setTranslateX(line.getTranslateX() + line.getWidth() - size/2);
                    valueMin = max ;
                }else if(newTranslateX < pointMin.getTranslateX()){
                    pointMax.setTranslateX(pointMin.getTranslateX());
                    valueMax = valueMin;
                }
            }
        });
        
        this.slider.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                backgroud.setFill(selectedBackground);
            }
        });
        this.slider.setOnMouseExited(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                backgroud.setFill(Color.WHITE);
            }
        });
        
//        this.getChildren().addAll(new StackPane(this.slider));
        this.getChildren().addAll(new FlowPane(this.slider));
    }

    
}