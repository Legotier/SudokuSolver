package main;

import gui.CenterController;
import gui.MainController;
import gui.fxml.FXMLManager;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    public static final String FILE_EXTENSION = "sudoku";

    private static Main main;
    private MainController mainController;
    private CenterController centerController;
    private Stage stage;
    private Sudoku sudoku = new Sudoku();
    private String fileLocation;

    public static Main instance() {
        return main;
    }

    public MainController getMainController() {
        return mainController;
    }

    public CenterController getCenterController() {
        return centerController;
    }

    public Stage getStage() {
        return stage;
    }

    public Sudoku getSudoku() {
        return sudoku;
    }

    public void setSudoku(Sudoku sudoku) {
        this.sudoku = sudoku;
        getCenterController().onSudokuChanged();
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String location) {
        fileLocation = location;
    }

    @Override
    public void start(Stage primaryStage) {
        if (main == null)
            main = this;
        stage = primaryStage;

        try {
            primaryStage.setTitle("Sudoku Solver");
            //primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("sudoku.png")));
            primaryStage.getIcons().add(new Image("sudoku.png"));

            FXMLLoader rootLoader = FXMLManager.getLoader("main");
            BorderPane root = rootLoader.load();
            mainController = rootLoader.getController();

            FXMLLoader centerLoader = FXMLManager.getLoader("center");
            StackPane center = centerLoader.load();
            centerController = centerLoader.getController();
            double size =  Math.min(Screen.getPrimary().getVisualBounds().getHeight(), Screen.getPrimary().getVisualBounds().getWidth()) / 2;
            ToolBar t = (ToolBar) root.getTop();
            if(t.getPrefWidth() > size)
                size += t.getPrefWidth() - size;
            center.setPrefHeight(size);
            center.setPrefWidth(size);

            root.setCenter(center);
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                mainController.handleKeyEvent(event);
                centerController.handleKeyEvent(event);
            });

            ChangeListener<Number> sizeListener = (observable, oldValue, newValue) -> centerController.onStageResize();
            scene.widthProperty().addListener(sizeListener);
            scene.heightProperty().addListener(sizeListener);

            primaryStage.show();
            primaryStage.setMinWidth(primaryStage.getWidth());
            primaryStage.setMinHeight(primaryStage.getHeight());

            for (Node n : ((ToolBar) root.getTop()).getItems()) {
                if (n instanceof Button && ((Button) n).isDefaultButton()) {
                    n.requestFocus();
                    break;
                }
            }
        } catch (Exception e) {
            main = null;
            e.printStackTrace();
            errorAndWait("An exception occurred while trying to start the application: " + e.getMessage());
            crash();
        }

    }

    public void errorAndWait(String text) {
        new Alert(Alert.AlertType.ERROR, text).showAndWait();
    }

    public void error(String text) {
        new Alert(Alert.AlertType.ERROR, text).show();
    }

    public void crash() {
        System.exit(-1);
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            instance().errorAndWait("An unexpected exception occurred in the following Thread: " + t.getName() + "\n Message: " + e.getMessage());
            instance().crash();
        });

        launch(args);
    }
}
