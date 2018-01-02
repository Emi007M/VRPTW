package vrptw;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;


public class SelectionBox {

    public static String display(Stage window){

        String path;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        String userDirectoryString = ".../";//System.getProperty("user.home");
        File userDirectory = new File(userDirectoryString);
        if(!userDirectory.canRead()) {
            userDirectory = new File("c:/");
        }
        fileChooser.setInitialDirectory(userDirectory);
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            path = file.getAbsolutePath();
            System.out.println("Selected file: " + path);
        }
        else{
            path = "NULL";
            System.out.println("No file selected!");
        }
        return path;
    }
}
