package ba.unsa.etf.rpr;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

public class GlavnaController {
    public TableView<Grad> tableViewGradovi;
    public TableColumn colGradId;
    public TableColumn colGradNaziv;
    public TableColumn colGradStanovnika;
    public TableColumn<Grad,String> colGradDrzava;
    private GeografijaDAO dao;
    private ObservableList<Grad> listGradovi;
    public Button btnDodajGrad;
    public Button btnDodajDrzavu;
    public Button btnIzmijeniGrad;
    public Button btnObrisiGrad;
    public Button btnJezik;
    public List<String> jezici;
    public ChoiceDialog<String> dialog;

    private ResourceBundle bundle = ResourceBundle.getBundle("Translation");
    private Locale locale = Locale.getDefault();

    public GlavnaController() {
        dao = GeografijaDAO.getInstance();
        listGradovi = FXCollections.observableArrayList(dao.gradovi());
    }

    @FXML
    public void initialize() {
        tableViewGradovi.setItems(listGradovi);
        colGradId.setCellValueFactory(new PropertyValueFactory("id"));
        colGradNaziv.setCellValueFactory(new PropertyValueFactory("naziv"));
        colGradStanovnika.setCellValueFactory(new PropertyValueFactory("brojStanovnika"));
        colGradDrzava.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDrzava().getNaziv()));
    }

    public void izaberiJezik(ActionEvent actionEvent) {
        jezici = new ArrayList<>();
        jezici.add("Bosanski");
        jezici.add("Engleski");
        dialog = new ChoiceDialog<>("Engleski", jezici);
        dialog.setTitle(bundle.getString("internacionalizacija"));
        dialog.setHeaderText(bundle.getString("dialogopis"));
        dialog.setContentText(bundle.getString("izabrani"));
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(s -> {
            if(s.equals("Bosanski")) ucitajJezik("bs");
            else if(s.equals("Engleski")) ucitajJezik("en_US");
        });

    }

    private void ucitajJezik(String jezik) {
        locale = new Locale(jezik);
        bundle = ResourceBundle.getBundle("Translation", locale);
        colGradNaziv.setText(bundle.getString("naziv"));
        colGradStanovnika.setText(bundle.getString("brojstanovnika"));
        colGradDrzava.setText(bundle.getString("drzava"));
        btnDodajDrzavu.setText(bundle.getString("dodajdrzavu"));
        btnDodajGrad.setText(bundle.getString("dodajgrad"));
        btnIzmijeniGrad.setText(bundle.getString("izmjeni"));
        btnObrisiGrad.setText(bundle.getString("izbrisi"));
        btnJezik.setText(bundle.getString("jezik"));
        dialog.setTitle(bundle.getString("internacionalizacija"));
        dialog.setHeaderText(bundle.getString("dialogopis"));
        dialog.setContentText(bundle.getString("izabrani"));

    }

    public void actionDodajGrad(ActionEvent actionEvent) {
        Stage stage = new Stage();
        Parent root = null;
        try {
            Locale.setDefault(new Locale(locale.getLanguage(),locale.getCountry()));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/grad.fxml"), bundle);
            GradController gradController = new GradController(null, dao.drzave());
            loader.setController(gradController);
            root = loader.load();
            stage.setTitle(bundle.getString("grad"));
            stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
            stage.setResizable(true);
            stage.show();

            stage.setOnHiding( event -> {
                Grad grad = gradController.getGrad();
                if (grad != null) {
                    dao.dodajGrad(grad);
                    listGradovi.setAll(dao.gradovi());
                }
            } );
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void actionDodajDrzavu(ActionEvent actionEvent) {
        Stage stage = new Stage();
        Parent root = null;
        try {
            Locale.setDefault(new Locale(locale.getLanguage(),locale.getCountry()));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/drzava.fxml"), bundle);
            DrzavaController drzavaController = new DrzavaController(null, dao.gradovi());
            loader.setController(drzavaController);
            root = loader.load();
            stage.setTitle(bundle.getString("drzava"));
            stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
            stage.setResizable(true);
            stage.show();

            stage.setOnHiding( event -> {
                Drzava drzava = drzavaController.getDrzava();
                if (drzava != null) {
                    dao.dodajDrzavu(drzava);
                    listGradovi.setAll(dao.gradovi());
                }
            } );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actionIzmijeniGrad(ActionEvent actionEvent) {
        Grad grad = tableViewGradovi.getSelectionModel().getSelectedItem();
        if (grad == null) return;
        Stage stage = new Stage();
        Parent root = null;
        try {
            Locale.setDefault(new Locale(locale.getLanguage(),locale.getCountry()));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/grad.fxml"), bundle);
            GradController gradController = new GradController(grad, dao.drzave());
            loader.setController(gradController);
            root = loader.load();
            stage.setTitle(bundle.getString("grad"));
            stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
            stage.setResizable(true);
            stage.show();

            stage.setOnHiding( event -> {
                Grad noviGrad = gradController.getGrad();
                if (noviGrad != null) {
                    dao.izmijeniGrad(noviGrad);
                    listGradovi.setAll(dao.gradovi());
                }
            } );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actionObrisiGrad(ActionEvent actionEvent) {
        Grad grad = tableViewGradovi.getSelectionModel().getSelectedItem();
        if (grad == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(bundle.getString("potvrdibrisanje"));
        alert.setHeaderText(bundle.getString("brisanjegrada")+" "+grad.getNaziv());
        alert.setContentText(bundle.getString("konacnapotvrdabrisanja") +grad.getNaziv()+"?");
        alert.setResizable(true);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            dao.obrisiGrad(grad);
            listGradovi.setAll(dao.gradovi());
        }
    }

    // Metoda za potrebe testova, vraća bazu u polazno stanje
    public void resetujBazu() {
        GeografijaDAO.removeInstance();
        File dbfile = new File("baza.db");
        dbfile.delete();
        dao = GeografijaDAO.getInstance();
    }
}
