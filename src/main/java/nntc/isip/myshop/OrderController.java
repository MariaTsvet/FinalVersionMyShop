package nntc.isip.myshop;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javax.swing.Timer;

public class OrderController {

    public Label orderId;
    public Label orderDate;
    public TableView tableAvailable;
    public TableColumn idColumnAvailable;
    public TableColumn nameColumnAvailable;
    public TableColumn priceColumnAvailable;
    public TableColumn countColumnAvailable;
    public TableView tableSelected;
    public TableColumn idColumnSelected;
    public TableColumn nameColumnSelected;
    public TableColumn priceColumnSelected;
    public TableColumn countColumnSelected;
    public ComboBox<KeyValue> customersList;
    public TextField fieldQuantity;
    public Label totalAmountLabel;

    private DatabaseManager primaryDatabaseManager;
    private Product selectedProduct;
    private ProductInOrder selectedProductInOrder;


    public void setPrimaryDatabaseManager(DatabaseManager dm) {
        this.primaryDatabaseManager = dm;
    }

    private int orderEditId = 0;

    public void setOrderId(String id) {
        try {
            this.orderEditId = Integer.parseInt(id);
        } catch (Exception e) {
            //
            this.orderEditId = 0;
        }
    }

    public void save(ActionEvent actionEvent) {

    }

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            orderId.setText("");
            orderDate.setText("");
            updateTableProducts();
            updateTableProductsInOrder(this.orderEditId);
            Order orderById = null;
            if (orderEditId == 0) {
                orderById = primaryDatabaseManager.createAndGetNewOrder();
                this.orderEditId = orderById.getId();
            } else {
                orderById = primaryDatabaseManager.getOrderById(orderEditId);
            }
            orderId.setText(String.format("%d", orderById.getId()));
            orderDate.setText(orderById.getDate());
            feelCustomersList(orderById != null ? orderById.getCustomerId() : 0);
        });

    }

    public void updateTableProducts() {
        ObservableList<Product> data = primaryDatabaseManager.productsFetchData();
        idColumnAvailable.setCellValueFactory(new PropertyValueFactory<Product, Integer>("id"));
        nameColumnAvailable.setCellValueFactory(new PropertyValueFactory<Product, String>("name"));
        priceColumnAvailable.setCellValueFactory(new PropertyValueFactory<Product, Float>("price"));
       // countColumnAvailable.setCellValueFactory(new PropertyValueFactory<Product, Integer>("count"));
        tableAvailable.setItems(data);
    }

    public void updateTableProductsInOrder(int orderId) {
        ObservableList<ProductInOrder> data = primaryDatabaseManager.productsFetchDataInOrder(orderId);
        idColumnSelected.setCellValueFactory(new PropertyValueFactory<Product, Integer>("id"));
        nameColumnSelected.setCellValueFactory(new PropertyValueFactory<Product, String>("name"));
        priceColumnSelected.setCellValueFactory(new PropertyValueFactory<Product, Float>("price"));
        countColumnSelected.setCellValueFactory(new PropertyValueFactory<Product, Integer>("count"));
        tableSelected.setItems(data);
        updateTotalAmount();
    }

    public void feelCustomersList(int id) {
        ObservableList<KeyValue> items = primaryDatabaseManager.customersGetList();
        customersList.setItems(items);
        if (id > 0) {
            for (KeyValue item : customersList.getItems()) {
                if (item.getKey() == id) {
                    customersList.setValue(item);
                    break;
                }
            }
        }
    }

    public void addProductToOrder() {
        try {
            // Получаем количество как целое число
            int quantity = Integer.parseInt(fieldQuantity.getText().replace(",", "."));

            // Проверяем, что количество больше 0
            if (quantity <= 0) {
                throw new NumberFormatException("Количество должно быть положительным числом.");
            }

            // Добавляем продукт в заказ
            if (primaryDatabaseManager.addProductToOrder(
                    orderEditId,
                    selectedProduct.getId(),
                    quantity)) {
                updateTableProductsInOrder(this.orderEditId);
                updateTotalAmount();
            }
        } catch (NumberFormatException e) {
                 System.out.println("Ошибка ввода: " + e.getMessage());
                }
    }



    public void removeProductFromOrder() {
        if (primaryDatabaseManager.removeProductFromOrder(
                orderEditId,
                selectedProductInOrder.getId())) {
            updateTableProductsInOrder(this.orderEditId);
        }
        ;
    }

    @FXML
    public void onProductInOrderClicked(MouseEvent event) {

        if (event.getClickCount() == 1) {
            System.out.println("onProductInOrderClicked");// Обработка одиночного клика
            this.selectedProductInOrder = (ProductInOrder) tableSelected.getSelectionModel().getSelectedItem();
            if (selectedProductInOrder != null) {
                fieldQuantity.setText(String.format("%.2f", selectedProductInOrder.getCount()));
            }
        }
    }

    @FXML
    public void onProductClicked(MouseEvent event) {
        if (event.getClickCount() == 1) {// Обработка одиночного клика
            System.out.println("onProductClicked");
            this.selectedProduct = (Product) tableAvailable.getSelectionModel().getSelectedItem();
        }
    }

    public void onCustomerChanged(ActionEvent event) {
        if (primaryDatabaseManager.changeCustomerInOrder(orderEditId, customersList.getValue())) {
            System.out.println("Заказчик был изменён");
        }
        ;
    }

    public void onQuantityChanged() {
         if (selectedProductInOrder != null) {
            float oldValue = selectedProductInOrder.getCount();
            float newValue = Float.parseFloat(!fieldQuantity.getText().isEmpty() ? fieldQuantity.getText()
                    .replace(",", ".")
                    .replace(" ", "") : "1.0");

            System.out.println(oldValue);
            System.out.println(newValue);

            if (oldValue != newValue) {
                        if (primaryDatabaseManager.updateCountinOrder(
                        orderEditId,
                        selectedProductInOrder.getId(),
                        Float.parseFloat(fieldQuantity.getText().replace(",", "."))
                )) {
                    System.out.println("Количество обновлено!");
                    updateTableProductsInOrder(orderEditId);

                    // run fieldQuantity.setText("1.0") after one second
                    new Timer(1000, evt -> fieldQuantity.setText("1.0")).start();
                }
            } else {
                System.out.println("Match!");
            }
        }
    }

    private void updateTotalAmount() {
        float totalAmount = 0.0f;
        ObservableList<ProductInOrder> productsInOrder = tableSelected.getItems();

        for (ProductInOrder product : productsInOrder) {
            totalAmount += product.getPrice() * product.getCount();
        }

        totalAmountLabel.setText(String.format("Итого: %.2f", totalAmount));
    }


}
