package gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import main.Main;
import main.Sudoku;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainController {

    private final Sudoku[] history = new Sudoku[10];
    private int pointer = -1;
    private FileChooser chooser;
    @FXML
    private Button undoButton;
    @FXML
    private MenuItem saveButton;

    @FXML
    private void initialize() {
        Main.instance().getSudoku().addChangeListener(s -> {
            if (pointer < history.length - 1) {
                pointer++;
                history[pointer] = s;
            }
        });
        chooser = new FileChooser();
        chooser.setInitialDirectory(new File(Main.instance().getFileLocation() == null ? System.getProperty("user.home") : Main.instance().getFileLocation()));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("The Sudoku format", "*." + Main.FILE_EXTENSION));
    }

    @FXML
    private void neww() {
        Main.instance().setSudoku(new Sudoku());
    }

    @FXML
    private void save() {

    }

    @FXML
    private void saveAs() {
        File file = chooser.showSaveDialog(Main.instance().getStage());
        if (file != null) {
            //Path path = Paths.get(file.getPath() + "." + Main.FILE_EXTENSION);
            Path path = file.toPath();
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(path))) {
                Main.instance().getSudoku().writeExternal(out);
            } catch (IOException e) {
                e.printStackTrace();
                Main.instance().error("An error occurred; the Sudoku could not be saved: " + e.getMessage());
            }
        } else
            System.out.println("cancelled");
    }

    @FXML
    private void open() {
        File file = chooser.showOpenDialog(Main.instance().getStage());
        if (file != null) {
            Path path = file.toPath();
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path))) {
                Sudoku s = new Sudoku();
                s.readExternal(in);
                Main.instance().setSudoku(s);
                Main.instance().setFileLocation(path.toString());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Main.instance().error("An error occurred; the Sudoku could not be loaded: " + e.getMessage());
            }
        }
    }

    @FXML
    private void undo() {
        System.out.println("Undo");
    }

    @FXML
    private void redo() {
        System.out.println("Redo");
    }

    @FXML
    private void solve() {
        if (Main.instance().getSudoku().solve())
            Main.instance().getCenterController().onSudokuChanged();
        else
            Toolkit.getDefaultToolkit().beep();
    }

    @FXML
    private void reset() {
        Main.instance().getSudoku().reset();
        Main.instance().getCenterController().onSudokuChanged();
    }

    @FXML
    private void generate() {
        System.out.println("Generate");
    }

    @FXML
    private void help() {
        System.out.println("Help");
    }

    public void handleKeyEvent(KeyEvent event) {
        if (new KeyCodeCombination(KeyCode.F1).match(event)) {
            help();
        } else if (new KeyCodeCombination(KeyCode.S).match(event)) {
            solve();
        } else if (new KeyCodeCombination(KeyCode.Z, KeyCodeCombination.CONTROL_DOWN).match(event)) {
            undo();
        } else if (new KeyCodeCombination(KeyCode.Y, KeyCodeCombination.CONTROL_DOWN).match(event)) {
            redo();
        } else if (new KeyCodeCombination(KeyCode.R, KeyCodeCombination.CONTROL_DOWN).match(event)) {
            reset();
        } else if (new KeyCodeCombination(KeyCode.G, KeyCodeCombination.CONTROL_DOWN).match(event)) {
            generate();
        }
    }
}
