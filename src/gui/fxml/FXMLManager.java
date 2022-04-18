package gui.fxml;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

/**
 * This class provides convenience methods for loading fxml files located in the {@code gui.fxml} package
 */
public final class FXMLManager {

    private FXMLManager() { }

    public static FXMLLoader getLoader(String filename) {
        //return new FXMLLoader(FXMLManager.class.getResource(filename + ".fxml"));
        return new FXMLLoader(FXMLManager.class.getResource("/" + filename + ".fxml"));
    }
}
