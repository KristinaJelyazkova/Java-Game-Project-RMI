package get15;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Get15ClientRMI extends Application {
    // color shows with what color the player marks the number taken by him
    private char color;

    // myTurn indicates whether the player can take a number now
    private boolean myTurn = false;

    //  get15 is the game server for coordinating with the players
    private Get15Interface get15;

    // lblColor identifies the player as the red or the blue one
    private Label lblColor;

    // lblMessage is used for displaying messages received by the server
    private Label lblMessage;

    // every number is a button which will be colored in red or blue (depending on the player) when pressed
    private Button[] numbers = new Button[9];

    //marks
    //private boolean[] isNumberTaken = new boolean[9];

    /** Initialize RMI */
    private boolean initializeRMI() throws RemoteException {
        String host = "localhost";

        try {
            Registry registry = LocateRegistry.getRegistry(host, 1099);
            get15 = (Get15Interface) registry.lookup("Get15Implementation");
            System.out.println("Server object " + get15 + " found");
        }
        catch (Exception ex) {
            System.out.println(ex);
        }

        // Create callback for use by the server to control the client
        CallBackImplementation callBackControl = new CallBackImplementation(this);

        if (
                (color = get15.connect((CallBack)callBackControl)) != ' ')
        {
            String col;

            if(color == 'r') {
                col = "red";
            }else{
                col = "blue";
            }

            System.out.println("Connected as " + col + " player.");
            lblColor.setText("You are the " + col + " player.");

            return true;
        }
        else {
            System.out.println("Already two players connected.");
            return false;
        }
    }

    /** Set variable myTurn to true or false */
    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    /** Set message on the status label */
    public void setMessage(String message) {
        Platform.runLater(() -> lblMessage.setText(message));
    }

    /** Mark the specified number using the color */
    public void mark(int number, char color) {
        if(color == 'r') {
            numbers[number - 1].setStyle("-fx-background-color: rgba(255,0,0,0.7)");
        }
        else{
            numbers[number - 1].setStyle("-fx-background-color: rgba(0,0,255,0.7)");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        lblColor = new Label();
        lblMessage = new Label();

        for (int i = 0; i < 9; i++) {
            numbers[i] = new Button("" + (i + 1));
            numbers[i].setShape(new Circle(20));
            numbers[i].setMinSize(40, 40);
            numbers[i].setMaxSize(40, 40);
        }

        GridPane root = new GridPane();

        root.setVgap(8);
        root.setHgap(8);
        root.setPadding(new Insets(14));
        root.setAlignment(Pos.CENTER);

        root.add(lblColor, 0, 0, 3, 1);

        for (int i = 0; i < 9; i++) {
            final int index = i;

            numbers[i].setOnAction(e -> {
                if(myTurn){
                    try {
                        boolean isFree = get15.takeNumber(index + 1, color);

                        if(isFree) {
                            if (color == 'r') {
                                numbers[index].setStyle("-fx-background-color: rgba(255,0,0,0.71)");
                            } else {
                                numbers[index].setStyle("-fx-background-color: rgba(0,0,255,0.7)");
                            }
                        }
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    }
                }
            });

            root.add(numbers[i], i % 3, i / 3 + 1);
        }

        root.add(lblMessage, 0, 4, 3, 1);

        Scene scene = new Scene(root, 250, 220);

        try {
            initializeRMI();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        primaryStage.setOnCloseRequest(e -> {
            try {
                get15.disconnect(color);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
            Platform.exit();
            System.exit(0);
        });
        primaryStage.setScene(scene);
        primaryStage.setTitle("Get 15");
        primaryStage.show();
    }
}
