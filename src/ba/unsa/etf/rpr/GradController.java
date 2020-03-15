package ba.unsa.etf.rpr;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class GradController {
    public TextField fieldNaziv;
    public TextField fieldBrojStanovnika;
    public ChoiceBox<Drzava> choiceDrzava;
    public ObservableList<Drzava> listDrzave;
    private Grad grad;
    public TextField fieldPostanskiBroj;


    public GradController(Grad grad, ArrayList<Drzava> drzave) {
        this.grad = grad;
        listDrzave = FXCollections.observableArrayList(drzave);
    }

    @FXML
    public void initialize() {
        choiceDrzava.setItems(listDrzave);
        if (grad != null) {
            fieldNaziv.setText(grad.getNaziv());
            fieldBrojStanovnika.setText(Integer.toString(grad.getBrojStanovnika()));
            // choiceDrzava.getSelectionModel().select(grad.getDrzava());
            // ovo ne radi jer grad.getDrzava() nije identički jednak objekat kao član listDrzave
            for (Drzava drzava : listDrzave)
                if (drzava.getId() == grad.getDrzava().getId())
                    choiceDrzava.getSelectionModel().select(drzava);
        } else {
            choiceDrzava.getSelectionModel().selectFirst();
        }
    }

    public Grad getGrad() {
        return grad;
    }

    public void clickCancel(ActionEvent actionEvent) {
        grad = null;
        Stage stage = (Stage) fieldNaziv.getScene().getWindow();
        stage.close();
    }

    public void clickOk(ActionEvent actionEvent) {
        boolean sveOk = true;

        if (fieldNaziv.getText().trim().isEmpty()) {
            fieldNaziv.getStyleClass().removeAll("poljeIspravno");
            fieldNaziv.getStyleClass().add("poljeNijeIspravno");
            sveOk = false;
        } else {
            fieldNaziv.getStyleClass().removeAll("poljeNijeIspravno");
            fieldNaziv.getStyleClass().add("poljeIspravno");
        }


        int brojStanovnika = 0;
        try {
            brojStanovnika = Integer.parseInt(fieldBrojStanovnika.getText());
        } catch (NumberFormatException e) {
            // ...
        }
        if (brojStanovnika <= 0) {
            fieldBrojStanovnika.getStyleClass().removeAll("poljeIspravno");
            fieldBrojStanovnika.getStyleClass().add("poljeNijeIspravno");
            sveOk = false;
        } else {
            fieldBrojStanovnika.getStyleClass().removeAll("poljeNijeIspravno");
            fieldBrojStanovnika.getStyleClass().add("poljeIspravno");
        }

        if (!sveOk) return;
        else {
            try {
                URL lokacija = new URL("http://c9.etf.unsa.ba/proba/postanskiBroj.php?postanskiBroj=" + fieldPostanskiBroj.getText());
                fieldPostanskiBroj.getStyleClass().removeAll("poljeIspravno");
                fieldPostanskiBroj.getStyleClass().removeAll("poljeNijeIspravno");
                new Thread(() -> {
                    String json = "", line = null;
                    BufferedReader ulaz = null;
                    try {
                        ulaz = new BufferedReader(new InputStreamReader(lokacija.openStream(), StandardCharsets.UTF_8));
                        while ((line = ulaz.readLine()) != null)
                            json = json + line;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (json.equals("NOT OK")) {
                        Platform.runLater(() -> {
                            fieldPostanskiBroj.getStyleClass().add("poljeNijeIspravno");
                        });
                    } else {
                        Platform.runLater(() -> {
                            fieldPostanskiBroj.getStyleClass().add("poljeIspravno");
                            if (grad == null) grad = new Grad();
                            grad.setNaziv(fieldNaziv.getText());
                            grad.setBrojStanovnika(Integer.parseInt(fieldBrojStanovnika.getText()));
                            grad.setDrzava(choiceDrzava.getValue());
                            grad.setPostanskiBroj(Integer.parseInt(fieldPostanskiBroj.getText()));
                            Stage stage = (Stage) fieldNaziv.getScene().getWindow();
                            stage.close();
                        });
                    }
                }).start();
            } catch (Exception e) {
                //
            }
        }
    }
}
