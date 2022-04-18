package gui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Paint;

class SelectorLabel extends Label {
    private static final Background SELECTED = new Background(new BackgroundFill(Paint.valueOf("#0093ff"), null, null));
    private static final Background DEFAULT = Background.EMPTY;
    private static final Background ERROR = new Background(new BackgroundFill(Paint.valueOf("crimson"), null, null));

    private boolean error = false;
    private Background background;
    private boolean mouse = false;

    public SelectorLabel(String text, Node graphic) {
        super(text, graphic);
    }

    public SelectorLabel(String text) {
        super(text);
        setListeners();
    }

    public SelectorLabel() {
        super();
        setListeners();
    }

    public void error() {
        if (!error) {
            error = true;
            setBackground(ERROR);
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setBackground(mouse ? SELECTED : background);
                error = false;
            }).start();
        }
    }

    private void setListeners() {
        backgroundProperty().addListener((observable, oldValue, newValue) -> {
            if (background == null && newValue != SELECTED && newValue != ERROR)
                background = newValue;
        });

        addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
            mouse = false;
            setBackground(error ? ERROR : SELECTED);
        });
        addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            mouse = false;
            setBackground(error ? ERROR : background == null ? DEFAULT : background);
        });
    }
}
