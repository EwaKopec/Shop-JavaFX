package shopProject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;


public class adminScreenController {
    // Inicjalizacja zmiennych
    ObservableList<Product> products =  FXCollections.observableArrayList();
    public Button editionButton;
    public TableView<Product> tableOfDB;
    public TableColumn<Product, Integer> IDproduct;
    public TableColumn<Product, String> NameProduct;
    public TableColumn<Product, Double> PriceProduct;
    public TableColumn<Product, String> Descrpition;
    public TableColumn<Product, String> room;
    public TableColumn<Product, String> Category;
    public TableColumn<Product, String> Subcategory;
    public TableColumn<Product, String> color;
    public TableColumn<Product, String> material;
    public TableColumn<Product, Double> width;
    public TableColumn<Product, Double> height;
    public TableColumn<Product, Double> length;
    public TableColumn<Product, Integer> shelf;
    public TableColumn<Product, Integer> regal;
    public TableColumn<Product, Integer> stock;

    Product selectedProduct;

    // Inicjalizaca klasy
    public void initialize() {
        IDproduct.setCellValueFactory(new PropertyValueFactory<Product, Integer>("productID"));
        NameProduct.setCellValueFactory(new PropertyValueFactory<Product, String>("nameOfProduct"));
        PriceProduct.setCellValueFactory(new PropertyValueFactory<Product, Double>("price"));
        Descrpition.setCellValueFactory(new PropertyValueFactory<Product, String>("description"));
        room.setCellValueFactory(new PropertyValueFactory<Product, String>("room"));
        Category.setCellValueFactory(new PropertyValueFactory<Product, String>("category"));
        Subcategory.setCellValueFactory(new PropertyValueFactory<Product, String>("subcategory"));
        color.setCellValueFactory(new PropertyValueFactory<Product, String>("color"));
        material.setCellValueFactory(new PropertyValueFactory<Product, String>("material"));
        width.setCellValueFactory(new PropertyValueFactory<Product, Double>("width"));
        height.setCellValueFactory(new PropertyValueFactory<Product, Double>("height"));
        length.setCellValueFactory(new PropertyValueFactory<Product, Double>("length"));
        shelf.setCellValueFactory(new PropertyValueFactory<Product, Integer>("shelf"));
        regal.setCellValueFactory(new PropertyValueFactory<Product, Integer>("regal"));
        stock.setCellValueFactory(new PropertyValueFactory<Product, Integer>("stock"));
        initializeTable();
    }

    // Pobranie wszystkich produkt??w
    public void getAllData(ObservableList<Product> products) {
        this.products.setAll(products);
    }

    // Inicjalizacja tablicy
    void initializeTable() {
        tableOfDB.setItems(products);
    }


    // Funkcja reakcji na wci??ni??cie przycisku dodawanie nowego produktu do bazy danych
    @FXML
    private void onAddButtonPressed() throws IOException {

        Stage addNewElementStage = new Stage();
        addNewElementStage.setTitle("Dodawanie nowego elementu");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/modifyProductScreen.fxml"));
        Parent root = loader.load();
        root.getStylesheets().add("Stylesheets/style.css");

        modifyProductScreenController newController = loader.getController();
        newController.setAddOrEdit(true);
        newController.setLabelINFO("Dodawanie nowego elementu");

        addNewElementStage.setScene(new Scene(root));
        addNewElementStage.setResizable(false);
        addNewElementStage.show();
        addNewElementStage.setOnCloseRequest(we -> {
            products.add(newController.getSelectedProduct());
            tableOfDB.refresh();
        });

    }

    // Funkcja reakcji na wci??ni??cie przycisku edycji wybranego produktu
    @FXML
    private void onEditionButtonPressed() throws IOException, SQLException, ClassNotFoundException {
        selectedProduct = tableOfDB.getSelectionModel().getSelectedItem();
        if(selectedProduct == null){
            Alert("B????d","B????d Nale??y zaznaczy?? odpowiedni wiersz do edycji." );
        }
        else{

            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/modifyProductScreen.fxml"));
            Parent root = loader.load();
            root.getStylesheets().add("Stylesheets/style.css");

            modifyProductScreenController newController = loader.getController();
            newController.setAddOrEdit(false);
            newController.setSelectedProduct(selectedProduct);
            newController.initializeData();
            newController.addProductToDB.setText("Edytuj");
            newController.setLabelINFO("Edycja wybranego elementu");


            Stage editProductbaseStage = new Stage();
            editProductbaseStage.setScene(new Scene(root));
            editProductbaseStage.setTitle("Edycja produktu o ID:" + selectedProduct.getProductID());
            editProductbaseStage.setResizable(false);
            editProductbaseStage.show();
            editProductbaseStage.setOnCloseRequest(we -> {
                changeProductInTable(newController.getSelectedProduct());
                tableOfDB.refresh();
            });
        }

    }

    // Funkcja reakcji na wci??ni??cie przycisku usuni??cia wybranego produktu
    @FXML
    private void onDeleteButtonPressed() throws SQLException, ClassNotFoundException {
        selectedProduct = tableOfDB.getSelectionModel().getSelectedItem();
        if(selectedProduct == null){
            Alert("B????d","B????d Nale??y zaznaczy?? odpowiedni wiersz do usuni??cia." );
        }
        else{

            Alert deleteClick = new Alert(Alert.AlertType.CONFIRMATION);
            deleteClick.setTitle("Usuwanie elementu");
            deleteClick.setHeaderText(null);
            deleteClick.setContentText("Czy na pewno usun???? element?");
            Optional<ButtonType> action = deleteClick.showAndWait();
            if(action.get() == ButtonType.OK) {


                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/Sklep?serverTimezone=UTC", "root", "bazadanych1-1");
                Statement statement = connection.createStatement();
                String sql = "DELETE FROM `sklep`.`produkty` WHERE (`IDProduktu` = '" + selectedProduct.getProductID() + "')";
                statement.executeUpdate(sql);
                sql = "DELETE FROM `sklep`.`szczegoly` WHERE (`IDProduktu` = '" + selectedProduct.getProductID() + "')";
                statement.executeUpdate(sql);


                for (Product pro : products) {
                    if (pro.getProductID() == selectedProduct.getProductID()) {
                        products.remove(pro);
                        break;
                    }
                }

                tableOfDB.refresh();

                statement.close();
                connection.close();
            }
        }
    }

    // Funkcja aktualizuj??ca tablice po zmiannach w bazie danych
    void changeProductInTable(Product product){
        int IDproduktu = product.getProductID();

        for(int i = 0; i < products.size(); i++)
        {
            if(IDproduktu == products.get(i).getProductID()){
                products.get(i).setNameOfProduct(product.getNameOfProduct());
                products.get(i).setPrice(product.getPrice());
                products.get(i).setDescription(product.getDescription());
                products.get(i).setRoom(product.getRoom());
                products.get(i).setCategory(product.getCategory());
                products.get(i).setSubcategory(product.getSubcategory());
                products.get(i).setColor(product.getColor());
                products.get(i).setMaterial(product.getMaterial());
                products.get(i).setWidth(product.getWidth());
                products.get(i).setHeight(product.getHeight());
                products.get(i).setLength(product.getLength());
                products.get(i).setShelf(product.getShelf());
                products.get(i).setRegal(product.getRegal());
                products.get(i).setStock(product.getStock());
                products.get(i).setImage(product.getImage());
                break;
            }
        }
    }

    // Funkcja wy??wietlaj??ca b????dy
    void Alert(String setTitle, String setContents) {
        Alert badClick = new Alert(Alert.AlertType.ERROR);
        badClick.setTitle(setTitle);
        badClick.setHeaderText(null);
        badClick.setContentText(setContents);

        badClick.showAndWait();
    }
}
