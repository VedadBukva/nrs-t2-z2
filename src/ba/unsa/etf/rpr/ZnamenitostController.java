package ba.unsa.etf.rpr;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

public class ZnamenitostController {
    public TextField fldNaziv;
    public ImageView imageView;
    private String slika;
    private Grad grad;
    private Znamenitost znamenitost;

    public ZnamenitostController(Grad grad) {
        this.grad = grad;
        this.slika = "";
    }

    public Znamenitost getZnamenitost() {
        return znamenitost;
    }

    public void spasiIzmjene(ActionEvent actionEvent) {
        if (znamenitost == null) znamenitost = new Znamenitost();
        znamenitost.setNaziv(fldNaziv.getText());
        znamenitost.setSlika(slika);
        znamenitost.setGrad(grad);
        Stage stage = (Stage) fldNaziv.getScene().getWindow();
        stage.close();
    }

    public void odaberiSliku(ActionEvent actionEvent) {
        Stage stage = new Stage();
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pretraga.fxml"));
            PretragaController pretragaController = new PretragaController();
            loader.setController(pretragaController);
            root = loader.load();
            stage.setTitle("Traži");
            stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
            stage.setResizable(false);
            stage.show();

            stage.setOnHiding(event -> {
                slika = pretragaController.getSlika();
                try {
                    imageView.setImage(new Image(new FileInputStream(slika)));
                } catch (FileNotFoundException e) {
                    //..
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean sveOk = true;
       /* if (fldNaziv.getText().trim().isEmpty()) {
            fldNaziv.getStyleClass().removeAll("poljeIspravno");
            fldNaziv.getStyleClass().add("poljeNijeIspravno");
            sveOk = false;
        } else {
            fldNaziv.getStyleClass().removeAll("poljeNijeIspravno");
            fldNaziv.getStyleClass().add("poljeIspravno");
        }
        if (!sveOk) return;

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Odaberite sliku");
        dialog.setHeaderText("Unesite apsulutni put do slike:");
        dialog.setContentText("Slika:");

        Optional<String> result = dialog.showAndWait();
        String res = result.get();
        slika = res;
        imageView.imageProperty().bind(Bindings.createObjectBinding(() -> {
            File file = new File(res);
            if (file.exists()) {
                return new Image(file.toURI().toString());
            } else {
                return null;
            }
        }));*/

    }
}
