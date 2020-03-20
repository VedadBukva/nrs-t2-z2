package ba.unsa.etf.rpr;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

public class GradController {
    public TextField fieldNaziv;
    public TextField fieldBrojStanovnika;
    public ChoiceBox<Drzava> choiceDrzava;
    public ObservableList<Drzava> listDrzave;
    private Grad grad;
    public TextField fieldPostanskiBroj;
    public ListView<Znamenitost> listViewZnamenitosti;
    public Button btnDodajZnamenitost;
    private GeografijaDAO dao;

    public Label lblNaziv;
    public Label lblBrojStanovnika;
    public Label lblDrzava;
    public Label lblPostanski;
    public Label lblZnamenitost;


    public ResourceBundle bundle = ResourceBundle.getBundle("Translation");

    public ObservableList<Znamenitost> listaZnamenitosti;


    public GradController(Grad grad, ArrayList<Drzava> drzave) {
        dao = GeografijaDAO.getInstance();
        this.grad = grad;
        listDrzave = FXCollections.observableArrayList(drzave);
        if (grad == null) listaZnamenitosti = FXCollections.observableArrayList();
        else listaZnamenitosti = FXCollections.observableArrayList( grad.getZnamenitosti() );
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
            listViewZnamenitosti.setVisible(false);
            btnDodajZnamenitost.setVisible(false);
        }
        listViewZnamenitosti.setItems(listaZnamenitosti);
    }

    public void dodajZnamenitost(ActionEvent actionEvent) {
        if (grad == null) return;
        Stage stage = new Stage();
        Parent root = null;
        try {
            // todo: odraditi dodavanje u bazu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/znamenitosti.fxml"));
            ZnamenitostController z = new ZnamenitostController(grad);
            loader.setController(z);
            root = loader.load();
            stage.setTitle("Znamenitosti");
            stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
            stage.setResizable(true);
            stage.show();

            stage.setOnHiding( event -> {
                Znamenitost zn = z.getZnamenitost();
                if (zn != null) {
                    dao.dodajZnamenitost(zn);
                    grad.getZnamenitosti().add(zn);
                    listaZnamenitosti.setAll(grad.getZnamenitosti());
                    listViewZnamenitosti.refresh();
                }
            } );
        } catch (IOException e) {
            e.printStackTrace();
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
                    String json = "";
                    String line = null;
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
