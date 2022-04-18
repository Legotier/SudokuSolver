package gui;

import gui.fxml.FXMLManager;
import javafx.css.Selector;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import main.Main;
import main.Sudoku;

import java.awt.*;
import java.io.IOException;

public class CenterController {

    @FXML
    private StackPane stack;
    @FXML
    private GridPane selector;
    private SelectorLabel[] selectorLabels = new SelectorLabel[9];
    @FXML
    private GridPane sudoku;
    private GridPane[][] sudokuPanes = new GridPane[3][3];
    private SelectorLabel[][] sudokuLabels = new SelectorLabel[9][9];

    /**
     * x = row, y = column (not the x- / y-axis)
     */
    private Point selected = new Point();
    private boolean inLabel = false;

    @FXML
    private void initialize() {
        try {
            stack.setAlignment(Pos.TOP_LEFT);

            FXMLLoader sudokuLoader = FXMLManager.getLoader("sudoku");
            sudoku = sudokuLoader.load();
            sudokuLoader.setController(this);
            stack.getChildren().add(0, sudoku);
            GridPane now = null;
            for (int i = 0; i < sudokuLabels.length; i++) {
                for (int j = 0; j < sudokuLabels[i].length; j++) {
                    if (j % 3 == 0 && i % 3 == 0) {
                        now = new GridPane();
                        sudokuPanes[i / 3][j / 3] = now;
                        sudoku.add(now, j / 3, i / 3);
                    } else if (j % 3 == 0) {
                        now = sudokuPanes[i / 3][j / 3];
                    }

                    final SelectorLabel label = new SelectorLabel();
                    sudokuLabels[i][j] = label;
                    label.setAlignment(Pos.CENTER);
                    if ((i + j) % 2 == 1)
                        label.setBackground(new Background(new BackgroundFill(Paint.valueOf("#3333"), null, null)));
                    now.add(label, j % 3, i % 3);
                    final int x = i, y = j;
                    label.setOnMouseEntered(event -> sudokuLabelSelected(x, y));
                    label.setOnMouseExited(event -> sudokuLabelDeselected());
                    label.setOnMouseClicked(this::sudokuLabelClicked);
                }
            }

            FXMLLoader selectorLoader = FXMLManager.getLoader("selector");
            selector = selectorLoader.load();
            selectorLoader.setController(this);
            selector.setVisible(false);
            stack.getChildren().add(1, selector);
            for (int i = 0; i < selectorLabels.length; i++) {
                //selectorLabels[i] = (SelectorLabel) selector.getChildren().get(i);
                final byte j = (byte) (i + 1);
                selectorLabels[i] = new SelectorLabel(Integer.toString(j));
                selectorLabels[i].setAlignment(Pos.CENTER);
                selector.add(selectorLabels[i], i % 3, i / 3);
                if (i % 2 == 1)
                    selectorLabels[i].setBackground(new Background(new BackgroundFill(Paint.valueOf("#3333"), null, null)));
                selectorLabels[i].setOnMouseClicked(event -> selectorClicked(j));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Main.instance().errorAndWait("An exception occurred while initializing the center controller: " + e.getMessage());
            Main.instance().crash();
        }
    }

    public void handleKeyEvent(KeyEvent event) {
        if (new KeyCodeCombination(KeyCode.DIGIT1).match(event))
            numberTyped((byte) 1);
        else if (new KeyCodeCombination(KeyCode.DIGIT2).match(event))
            numberTyped((byte) 2);
        else if (new KeyCodeCombination(KeyCode.DIGIT3).match(event))
            numberTyped((byte) 3);
        else if (new KeyCodeCombination(KeyCode.DIGIT4).match(event))
            numberTyped((byte) 4);
        else if (new KeyCodeCombination(KeyCode.DIGIT5).match(event))
            numberTyped((byte) 5);
        else if (new KeyCodeCombination(KeyCode.DIGIT6).match(event))
            numberTyped((byte) 6);
        else if (new KeyCodeCombination(KeyCode.DIGIT7).match(event))
            numberTyped((byte) 7);
        else if (new KeyCodeCombination(KeyCode.DIGIT8).match(event))
            numberTyped((byte) 8);
        else if (new KeyCodeCombination(KeyCode.DIGIT9).match(event))
            numberTyped((byte) 9);
        else if (new KeyCodeCombination(KeyCode.ESCAPE).match(event))
            selector.setVisible(false);
        else if (new KeyCodeCombination(KeyCode.DELETE).match(event) || new KeyCodeCombination(
                KeyCode.BACK_SPACE).match(event) || new KeyCodeCombination(KeyCode.DIGIT0).match(event))
            resetLabel();
    }

    public void onStageResize() {
        final double height =
                Main.instance().getStage().getScene().getHeight() - ((ToolBar) ((BorderPane) stack.getParent()).getTop()).getHeight(),
                width = Main.instance().getStage().getScene().getWidth(), size = Math.min(
                height, width), sizeSmall = size / 3;

        stack.setPrefSize(size, size);
        sudoku.setPrefSize(size, size);

        selector.setPrefSize(size / 4, size / 4);
        for (Label label : selectorLabels) {
            label.setPrefSize(sizeSmall / 4, sizeSmall / 4);
            label.setFont(new Font(size / 20));
        }

        for (GridPane[] panes : sudokuPanes) {
            for (GridPane pane : panes) {
                pane.setPrefSize(sizeSmall, sizeSmall);
            }
        }

        for (SelectorLabel[] labels : sudokuLabels) {
            for (SelectorLabel label : labels) {
                label.setPrefSize(sizeSmall / 3, sizeSmall / 3);
                label.setFont(new Font(sizeSmall / 5));
            }
        }

        relocateSelector();

        if (height <= width)
            ((DropShadow) stack.getEffect()).setHeight(0.0d);
        else
            ((DropShadow) stack.getEffect()).setHeight(21.0d);

    }

    public void onSudokuChanged() {
        paintSudoku();
    }

    private void numberTyped(byte i) {
        if (inLabel || selector.isVisible())
            selectorClicked(i);
    }

    private void selectorClicked(byte i) {
        sudokuLabels[selected.x][selected.y].requestFocus();
        selector.setVisible(false);
        if (i != Main.instance().getSudoku().get(selected.x, selected.y) && Main.instance().getSudoku().isValid(selected.x, selected.y, i)) {
            sudokuLabels[selected.x][selected.y].setText(Integer.toString(i));
            Main.instance().getSudoku().set(selected.x, selected.y, i);
        } else {
            Toolkit.getDefaultToolkit().beep();
            sudokuLabels[selected.x][selected.y].error();
        }
    }

    private void sudokuLabelClicked(MouseEvent event) {
        if (!selector.isVisible() && event.getButton() == MouseButton.PRIMARY) {
            relocateSelector();
            selector.setVisible(true);
        } else if (event.getButton() == MouseButton.SECONDARY)
            resetLabel();
        else if (selector.isVisible())
            selector.setVisible(false);
        sudokuLabels[selected.x][selected.y].requestFocus();
    }

    private void sudokuLabelSelected(int x, int y) {
        selected.setLocation(x, y);
        inLabel = true;
        if (selector.isVisible())
            relocateSelector();
    }

    private void sudokuLabelDeselected() {
        inLabel = false;
    }

    private void relocateSelector() {
        // stack.height == stack.width
        final double labelSize = stack.getHeight() / 9, selectorSize = stack.getHeight() / 4,
                height = Main.instance().getStage().getScene().getHeight() - ((ToolBar) ((BorderPane) stack.getParent()).getTop()).getHeight(),
                width = Main.instance().getStage().getScene().getWidth(),
                selectorHeight = (height - stack.getHeight()) / 2 + labelSize + selected.x * labelSize + selectorSize,
                selectorWidth = (width - stack.getWidth()) / 2 + selected.y * labelSize + selectorSize;

        if (selectorHeight <= height && selectorWidth <= width) {
            selector.setTranslateX(labelSize * selected.y);
            selector.setTranslateY(labelSize * selected.x + labelSize);
        } else if (selectorHeight <= height) {
            selector.setTranslateX(labelSize * selected.y - (selectorSize - labelSize));
            selector.setTranslateY(labelSize * selected.x + labelSize);
        } else if (selectorWidth <= width) {
            selector.setTranslateX(labelSize * selected.y);
            selector.setTranslateY(labelSize * selected.x - selectorSize);
        } else {
            selector.setTranslateX(labelSize * selected.y - (selectorSize - labelSize));
            selector.setTranslateY(labelSize * selected.x - selectorSize);
        }
    }

    private void resetLabel() {
        if (inLabel || selector.isVisible()) {
            sudokuLabels[selected.x][selected.y].setText(null);
            selector.setVisible(false);
            Main.instance().getSudoku().set(selected.x, selected.y, (byte) 0);
        }
    }

    private void paintSudoku() {
        Sudoku s = Main.instance().getSudoku();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int number = s.get(i, j);
                sudokuLabels[i][j].setText(number == 0 ? null : Integer.toString(number));
            }
        }
    }
}
