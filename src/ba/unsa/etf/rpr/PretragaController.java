package ba.unsa.etf.rpr;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PretragaController implements Initializable {
    public TextField fldUzorak;
    public ListView<String> listViewSlike;
    private ObservableList<String> apsolutnePutanje;
    private String slika;
    public ArrayList<String> putanje;

    private Thread thread;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        thread = new Thread();
        putanje = new ArrayList<>();
        apsolutnePutanje = FXCollections.observableArrayList(putanje);
        listViewSlike.setItems(apsolutnePutanje);
    }

    public void traziSliku() {
        putanje.clear();
        apsolutnePutanje.setAll(putanje);
        thread = new Thread(this::trazi);
        thread.start();
    }

    private void trazi() {
        if(fldUzorak.getText().isEmpty()) fldUzorak.setText("");
        pronadji(fldUzorak.getText(), new File(System.getProperty("user.home")));
    }

    public void pronadji(String uzorak, File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    pronadji(uzorak, f);
                } else if (f.getAbsolutePath().toLowerCase().contains(uzorak.toLowerCase())) {
                    Platform.runLater(() -> apsolutnePutanje.add(f.getAbsolutePath()));
                }
            }
        }
    }

    public void izaberiSliku() {
        thread.interrupt();
        if (!listViewSlike.getSelectionModel().isEmpty()) {
            slika =  listViewSlike.getSelectionModel().getSelectedItem();
        }
        Stage stage = (Stage) listViewSlike.getScene().getWindow();
        stage.close();
    }

    public String getSlika() {
        if(slika==null) {
            slika = "";
        }
        return slika;
    }

}
