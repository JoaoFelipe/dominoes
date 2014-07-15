package boundary;

import domain.Configuration;
import domain.Dominoes;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;

/**
 * A sample showing how to use JUNG's layout classes to position vertices in a
 * graph.
 *
 * @author jeffreyguenther
 * @author timheng
 */
public class Visual extends BorderPane {

    private double padding = Configuration.width;
    private TabPane tabPane;
    public Visual() {
        tabPane = new TabPane();

        this.setCenter(tabPane);

    }

    /**
     * This Functions is used to define the moving area size
     *
     * @param width
     * @param height
     */
    public void setSize(double width, double height) {
        this.setMinWidth(width - padding);
        this.setPrefWidth(width);
        this.setMaxWidth(width + padding);
        this.setPrefHeight(height);
    }
    
    public void addTabGraph(Dominoes domino){
        Tab tab = new Tab(domino.getIdRow() + "x" + domino.getIdCol() + " " + this.tabPane.getTabs().size());
        GraphPane graphPane = new GraphPane(domino);
        
        tab.setContent(graphPane);
        Tooltip.install(tab.getGraphic(), new Tooltip(domino.getHistoric().toString()));
        
        this.tabPane.getTabs().add(tab);       
    }

    void addTabMatrix(Dominoes domino) {
        Tab tab = new Tab(domino.getIdRow() + "x" + domino.getIdCol() + " " + this.tabPane.getTabs().size());
        MatrixPane graphPane = new MatrixPane(domino);
        
        tab.setContent(graphPane);
        Tooltip.install(tab.getGraphic(), new Tooltip(domino.getHistoric().toString()));

        this.tabPane.getTabs().add(tab);
        this.tabPane.getSelectionModel().selectLast();
    }
    
    
}
