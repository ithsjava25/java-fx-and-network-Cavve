package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class HelloFX extends Application {
//startar appen och laddar fxml filen

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloFX.class.getResource("hello-view.fxml"));
        //root objekt ,  som har flera parent noder som också har lövnoder, children
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 640, 480);
        stage.setTitle("Hello MVC");
        //olika vyer, kan byta scene för att ändra utseende
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {launch(); //anropar launch metoden i application klassen
    }

}