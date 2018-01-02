package vrptw;

import vrptw.OriginalMain;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;


public class Main extends Application {

    Stage window;
    Scene scene1;
    Button button1;
    String filepath = "NULL";
    VBox layout1 = new VBox(20);

    public String getFilepath() {
        return filepath;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        //window
        window = primaryStage;
        window.setTitle("VRPTW - LNS");

        //label
        Label label1 = new Label("Choose file to open and click the button below to compute.");

        //label2
        Label label2 = new Label("Currently selected: " + filepath);

        //canvas
        Canvas canvas = new Canvas(1000, 600);
        GraphicsContext context = canvas.getGraphicsContext2D();

        //fileMenu
        Menu fileMenu = new Menu("File");

        //fileMenu items
        MenuItem openFile = new MenuItem("Open");
        openFile.setOnAction(e -> {
            filepath = SelectionBox.display(window);
            label2.setText("Currently selected: " + filepath);
        });

        MenuItem clearCanvas = new MenuItem("Clear");
        clearCanvas.setOnAction(e -> {
            context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        });

        fileMenu.getItems().add(openFile);
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(clearCanvas);

        //runMenu
        Menu runMenu = new Menu("Run");

        //runMenu items
        MenuItem run = new MenuItem("Run");
        run.setOnAction(e -> {
            runCalc(label2, layout1, context, canvas);
        });
        runMenu.getItems().add(run);

        //exitMenu
        Menu exitMenu = new Menu("Exit");

        //exitMenu items
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> {
            System.out.println("exit");
            System.out.println("----");
            System.exit(0);
        });
        exitMenu.getItems().add(exit);

        //Menu bar
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, runMenu, exitMenu);

        //button1
        button1 = new Button();
        button1.setText("Run");
        button1.setOnAction(e -> {
            String endOfPath = filepath.substring(filepath.lastIndexOf("\\") + 1);
            OriginalMain.setNodesDir(endOfPath);
            runCalc(label2, layout1, context, canvas);
        });

        //Layout1 - children are laid out in vertical column
        //VBox layout1 = new VBox(20);
        layout1.getChildren().addAll(menuBar, label1, label2, button1, canvas);
        layout1.setAlignment(Pos.TOP_CENTER);

        scene1 = new Scene(layout1, 1080, 720);
        setUserAgentStylesheet(STYLESHEET_CASPIAN);

        scene1.setFill(Color.LIGHTGREY);
        window.setScene(scene1);
        window.show();
    }

    public void runCalc(Label label2, VBox layout1, GraphicsContext context, Canvas canvas){
        if (filepath == "NULL") {
            System.out.println("Select file first!");
        }
        else{
            System.out.println("Computing ... " + filepath);
            OriginalMain newComputation = new OriginalMain();
            newComputation.originalMain();
            drawShapes(context, canvas);
        }
        label2.setText("Currently selected: " + filepath);
    }

    private void drawShapes(GraphicsContext gc, Canvas canvas) {

        //gc.setFill(Color.color(Math.random(), Math.random(), Math.random()));

        // vehicles<singleVehicle[x_1, x_2, ... , x_n], singleVehicle[y_1, y_2, ... , y_n]>
        for (int i = 0; i < OriginalMain.getPointsToDraw().size(); i++)
        {
            gc.setFill(Color.color(Math.random(), Math.random(), Math.random()));
            gc.setStroke(Color.color(Math.random(), Math.random(), Math.random()));
            gc.setLineWidth(5);
            gc.strokePolyline(OriginalMain.getPointsToDraw().get(i).get(0), OriginalMain.getPointsToDraw().get(i).get(1),
                    OriginalMain.getPointsToDraw().get(i).get(1).length);
        }

//        gc.setFill(Color.color(Math.random(), Math.random(), Math.random()));
//        gc.setStroke(Color.color(Math.random(), Math.random(), Math.random()));
//        gc.setLineWidth(5);
//        double[] xes = {500, randomWithRange(0,1000), randomWithRange(0,1000), randomWithRange(0,1000)};
//        double[] ys = {300, randomWithRange(0,500), randomWithRange(0,500), randomWithRange(0,500)};
//        int pointsTotal = xes.length;
//        gc.strokePolyline(xes, ys, pointsTotal);
//        gc.strokePolyline(new double[]{110, 140, 110, 140, 110},
//                new double[]{210, 210, 240, 240, 210}, 5);
//        gc.strokePolygon(new double[]{110+100, 140+100, 110+100, 140+100},
//                new double[]{210, 210, 240, 240}, 4);
    }

    public int randomWithRange(int min, int max)
    {
        int range = (max - min) + 1;
        return (int)(Math.random() * range) + min;
    }
}