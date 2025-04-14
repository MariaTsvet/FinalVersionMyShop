package nntc.isip.myshop;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Objects;
import java.util.prefs.Preferences;

public class DatabaseManager {
    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static String SCHEMA;

    private Preferences prefs;

    private Connection connection;

    // конструктор
    // создали для инициализации prefs на уровне всего класса
    public DatabaseManager() {
        prefs = Preferences.userNodeForPackage(DesktopApplication.class);
    }

    // Метод для подключения к базе данных
    public void connect() throws SQLException {

        URL = String.format(
                "jdbc:postgresql://%s:%s/%s?currentSchema=%s",
                prefs.get("subdAddress", "localhost"),
                prefs.get("subdPort", "5432"),
                prefs.get("subdDbname", "postgres"),
                prefs.get("subdSchema", "public")
        );
        USER = prefs.get("subdUser", "postgres");
        PASSWORD = prefs.get("subdPassword", "postgres");
        SCHEMA = prefs.get("subdSchema", "public");

        connection = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("Успешно подключено к базе данных.");
    }

    // Метод для отключения от базы данных
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Соединение с базой данных закрыто.");
            } catch (SQLException e) {
                System.out.println("Ошибка при закрытии соединения: " + e.getMessage());
                // Показываем модальное окно с ошибкой
                Platform.runLater(() -> ErrorDialog.showError("Ошибка при закрытии соединения: ", e.getMessage()));
            }
        }
    }

    // Метод развёртывания таблиц базы данных
    public void ensureTablesExists() {
        String ddlQueries = String.format("""
                    CREATE TABLE IF NOT EXISTS %s.customers (
                            	id serial4 NOT NULL PRIMARY KEY,
                            	"name" varchar(256) NOT NULL,
                            	email varchar(128) NOT NULL,
                            	CONSTRAINT customers_email_key UNIQUE (email)
                            );
                     
                    CREATE TABLE IF NOT EXISTS %s.orders (
                                id serial4 NOT NULL PRIMARY KEY,
                                customer_id int4 NULL,
                                created timestamp DEFAULT now() NOT NULL,
                                CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES %s.customers(id) ON DELETE CASCADE ON UPDATE CASCADE
                             );
                    CREATE TABLE IF NOT EXISTS %s.products (
                        id serial4 NOT NULL PRIMARY KEY,
                        "name" varchar(256) NOT NULL,
                        price numeric(10, 2) NOT NULL,
                        image_data bytea NULL
                    );
                    CREATE TABLE IF NOT EXISTS %s.order_product (
                             	order_id int4 NOT NULL,
                             	product_id int4 NOT NULL,
                             	quantity numeric NOT NULL,
                             	CONSTRAINT order_product_pk PRIMARY KEY (order_id, product_id),
                             	CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES %s.products(id) ON DELETE CASCADE ON UPDATE CASCADE,
                             	CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES %s.orders(id) ON DELETE CASCADE ON UPDATE CASCADE
                             );
                """, SCHEMA, SCHEMA, SCHEMA, SCHEMA, SCHEMA, SCHEMA, SCHEMA);

        String dmlQueries = String.format("""
                    INSERT INTO %s.products ("name",price) VALUES
                    ('Сгущенное молоко',60),
                    ('Сыр',200),
                    ('Молоко',50),
                    ('Сахар',150),
                    ('Сливочное масло',150),
                    ('Батон',35);
                    INSERT INTO %s.customers ("name",email) VALUES
                    ('Вася','vasya@mail.ru'),
                    ('Маша','masha@mail.ru');
                    INSERT INTO %s.orders (customer_id) VALUES
                    (1),(2);
                    INSERT INTO %s.order_product (order_id, product_id, quantity) VALUES
                    (1, 1, 1),(1, 2, 2),(1, 3, 3),(2, 4, 3),(2, 5, 1),(2, 6, 2);
                """, SCHEMA, SCHEMA, SCHEMA, SCHEMA);


        try (Statement statement = connection.createStatement()) {
            statement.execute(ddlQueries);
            // statement.execute(dmlQueries); // Если нужно, то можно и DML запросы здесь запустить
            System.out.println("Проверка структуры базы данных завершена.");
        } catch (SQLException e) {
            System.out.println("Ошибка при проверке/создании таблицы: " + e.getMessage());
            // Показываем модальное окно с ошибкой
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при проверке/создании таблицы: ", e.getMessage()));
        }
    }

    public void productsInsertData(String name, Float price) {
        String query = String.format("INSERT INTO %s (name, price) VALUES ('%s', %s)", SCHEMA.concat(".products"), name, String.format("%f", price).replace(",", "."));
        try (var preparedStatement = connection.prepareStatement(query)) {
            int rowsInserted = preparedStatement.executeUpdate();
            System.out.println("Добавлено строк: " + rowsInserted);
        } catch (SQLException e) {
            System.out.println("Ошибка при вставке данных: " + e.getMessage());
            // Показываем модальное окно с ошибкой
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при вставке данных: ", e.getMessage()));
        }
    }

    public void productsUpdateData(Integer id, String name, float price) {
        String query = String.format("UPDATE %s SET name='%s', price=%s WHERE id=%d", SCHEMA.concat(".products"), name, String.format("%f", price).replace(",", "."), id);

        System.out.println("QUERY:");
        System.out.println(query);

        try (var preparedStatement = connection.prepareStatement(query)) {
            int rowsUpdated = preparedStatement.executeUpdate();
            System.out.println("Обновлено строк: " + rowsUpdated);
        } catch (SQLException e) {
            System.out.println("Ошибка при изменении данных: " + e.getMessage());
            // Показываем модальное окно с ошибкой
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при изменении данных: ", e.getMessage()));
        }
    }

    public void productsDeleteData(int id) {
        String query = String.format("DELETE FROM %s WHERE id=%d", SCHEMA.concat(".products"), id);

        try (var preparedStatement = connection.prepareStatement(query)) {
            int rowsDeleted = preparedStatement.executeUpdate();
            System.out.println("Удалено строк: " + rowsDeleted);
        } catch (SQLException e) {
            System.out.println("Ошибка при удалении данных: " + e.getMessage());
            // Показываем модальное окно с ошибкой
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при удалении данных: ", e.getMessage()));
        }
    }

    // Пример: Получение данных
    public ObservableList<Product> productsFetchData() {

        ObservableList<Product> re = FXCollections.observableArrayList();

        String query = String.format("SELECT id, name, price FROM %s", SCHEMA.concat(".products"));

        try (var preparedStatement = connection.prepareStatement(query);
             var resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                re.add(new Product(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getFloat("price")));
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
            // Показываем модальное окно с ошибкой
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при выполнении запроса: ", e.getMessage()));
        }
        return re;
    }

    public ObservableList<ProductInOrder> productsFetchDataInOrder(int id) {

        ObservableList<ProductInOrder> re = FXCollections.observableArrayList();

        String query = String.format(
                "SELECT p.id id, p.name \"name\", p.price price, op.quantity \"count\" " +
                        "FROM %s p " +
                        "INNER JOIN %s op on op.product_id = p.id " +
                        "WHERE op.order_id = %d ORDER BY p.id;",
                SCHEMA.concat(".products"),
                SCHEMA.concat(".order_product"), id);

        System.out.println("QUERY: ".concat(query));

        try (var preparedStatement = connection.prepareStatement(query);
             var resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                re.add(new ProductInOrder(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getFloat("price"),
                        resultSet.getFloat("count")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
            // Показываем модальное окно с ошибкой
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при выполнении запроса: ", e.getMessage()));
        }
        return re;
    }

    public void customersInsertData(String name, String email) {
        String query = String.format("INSERT INTO %s (name, email) VALUES ('%s', '%s')", SCHEMA.concat(".customers"), name, email);
        try (var preparedStatement = connection.prepareStatement(query)) {
            int rowsInserted = preparedStatement.executeUpdate();
            System.out.println("Добавлено строк: " + rowsInserted);
        } catch (SQLException e) {
            System.out.println("Ошибка при вставке данных: " + e.getMessage());
            // Показываем модальное окно с ошибкой
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при вставке данных: ", e.getMessage()));
        }
    }

    public void customersUpdateData(Integer id, String name, String email) {
        String query = String.format("UPDATE %s SET name='%s', email='%s' WHERE id=%d", SCHEMA.concat(".customers"), name, email, id);

        System.out.println("QUERY:");
        System.out.println(query);

        try (var preparedStatement = connection.prepareStatement(query)) {
            int rowsUpdated = preparedStatement.executeUpdate();
            System.out.println("Обновлено строк: " + rowsUpdated);
        } catch (SQLException e) {
            System.out.println("Ошибка при изменении данных: " + e.getMessage());
            // Показываем модальное окно с ошибкой
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при изменении данных: ", e.getMessage()));
        }
    }

    public void customersDeleteData(int id) {
        String query = String.format("DELETE FROM %s WHERE id=%d", SCHEMA.concat(".customers"), id);

        try (var preparedStatement = connection.prepareStatement(query)) {
            int rowsDeleted = preparedStatement.executeUpdate();
            System.out.println("Удалено строк: " + rowsDeleted);
        } catch (SQLException e) {
            System.out.println("Ошибка при удалении данных: " + e.getMessage());
            // Показываем модальное окно с ошибкой
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при удалении данных: ", e.getMessage()));
        }
    }

    // Пример: Получение данных
    public ObservableList<Customer> customersFetchData() {

        ObservableList<Customer> re = FXCollections.observableArrayList();

        String query = String.format("SELECT id, name, email FROM %s", SCHEMA.concat(".customers"));

        try (var preparedStatement = connection.prepareStatement(query);
             var resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                re.add(new Customer(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("email")));
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
            // Показываем модальное окно с ошибкой
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при выполнении запроса: ", e.getMessage()));
        }
        return re;
    }

    public ObservableList<KeyValue> customersGetList() {
        ObservableList<KeyValue> re = FXCollections.observableArrayList();

        String query = String.format("SELECT id, name, email FROM %s", SCHEMA.concat(".customers"));

        try (var preparedStatement = connection.prepareStatement(query);
             var resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                re.add(new KeyValue(resultSet.getInt("id"), resultSet.getString("name")));
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
            // Показываем модальное окно с ошибкой
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при выполнении запроса: ", e.getMessage()));
        }
        return re;
    }

    public void orderInsertData() {
        // FIXME: реализовать потом
    }

    public void orderUpdateData() {
        // FIXME: реализовать потом
    }

    public void orderDeleteData(int id) {
        String query = String.format("DELETE FROM %s WHERE id=%d", SCHEMA.concat(".orders"), id);

        try (var preparedStatement = connection.prepareStatement(query)) {
            int rowsDeleted = preparedStatement.executeUpdate();
            System.out.println("Удалено строк: " + rowsDeleted);
        } catch (SQLException e) {
            System.out.println("Ошибка при удалении данных: " + e.getMessage());
            // Показываем модальное окно с ошибкой
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при удалении данных: ", e.getMessage()));
        }
    }

    // Пример: Получение данных
    public ObservableList<Order> orderFetchData() {

        ObservableList<Order> re = FXCollections.observableArrayList();
        String query = String.format("SELECT o.id id, o.created date, c.name customer " +
                        "FROM %s o " +
                        "LEFT JOIN %s c on c.id = o.customer_id " +
                        "ORDER BY date DESC;",
                SCHEMA.concat(".orders"),
                SCHEMA.concat(".customers"));

        try (var preparedStatement = connection.prepareStatement(query);
             var resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                re.add(new Order(resultSet.getInt("id"), resultSet.getString("date"), resultSet.getString("customer")));
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
            // Показываем модальное окно с ошибкой
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при выполнении запроса: ", e.getMessage()));
        }
        return re;
    }

    public Order getOrderById(int orderEditId) {
        Order re = null;
        String query = String.format("SELECT o.id id, o.customer_id customer_id, o.created date " +
                        "FROM %s o " +
                        "WHERE id = %d",
                SCHEMA.concat(".orders"),
                orderEditId);

        try (var preparedStatement = connection.prepareStatement(query);
             var resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            re = new Order(resultSet.getInt("id"), resultSet.getString("date"), "");
            re.setCustomer(resultSet.getInt("customer_id"));
            return re;
        } catch (SQLException e) {
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
            // Показываем модальное окно с ошибкой
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при выполнении запроса: ", e.getMessage()));
        }
        return re;
    }

    public boolean addProductToOrder(int orderId, int productId, float quantity) {
        String query = "INSERT INTO " + SCHEMA.concat(".order_product") +
                " (order_id, product_id, quantity) VALUES (?, ?, ?)";

        try (var preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, orderId);
            preparedStatement.setInt(2, productId);
            preparedStatement.setFloat(3, quantity);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении: " + e.getMessage());
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при добавлении товара в заказ", e.getMessage()));
            return false;
        }
    }

    public boolean removeProductFromOrder(int orderId, int productId) {
        String query = "DELETE FROM " + SCHEMA.concat(".order_product") +
                " WHERE order_id = ? AND product_id = ?";
        try (var preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, orderId);
            preparedStatement.setInt(2, productId);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении: " + e.getMessage());
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при удалении: ", e.getMessage()));
            return false;
        }
    }

    public boolean changeCustomerInOrder(int orderId, KeyValue customer) {
        String query = "UPDATE " + SCHEMA.concat(".orders") +
                " SET customer_id = ? WHERE id = ?";
        try (var preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, customer.getKey());
            preparedStatement.setInt(2, orderId);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении: " + e.getMessage());
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при удалении: ", e.getMessage()));
            return false;
        }
    }

    public boolean updateCountinOrder(int orderId, int productId, float quantity) {
        String query = "UPDATE " + SCHEMA.concat(".order_product") +
                " SET quantity = ? WHERE order_id = ? AND product_id = ?";
        try (var preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setFloat(1, quantity);
            preparedStatement.setInt(2, orderId);
            preparedStatement.setInt(3, productId);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении: " + e.getMessage());
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при удалении: ", e.getMessage()));
            return false;
        }
    }

    public Order createAndGetNewOrder() {
        String queryInsert = "INSERT INTO " + SCHEMA.concat(".orders") +
                " DEFAULT VALUES";
        try (var preparedStatementInsert = connection.prepareStatement(queryInsert)) {
            int affectedRows = preparedStatementInsert.executeUpdate();
            if(affectedRows > 0) {
                String query = String.format("SELECT * " +
                                "FROM %s o " +
                                "ORDER BY o.id DESC LIMIT 1", SCHEMA.concat(".orders"));

                try (var preparedStatement = connection.prepareStatement(query);
                    var resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    Order re = new Order(resultSet.getInt("id"), resultSet.getString("created"), "");
                    return re;
                } catch (SQLException e) {
                    System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
                    // Показываем модальное окно с ошибкой
                    Platform.runLater(() -> ErrorDialog.showError("Ошибка при выполнении запроса: ", e.getMessage()));
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении: " + e.getMessage());
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при удалении: ", e.getMessage()));
            return null;
        }
        return null;
    }

    public boolean saveProductImage(File file, int productId) {
        String query = "UPDATE " + SCHEMA.concat(".products") + " SET image_data = ? WHERE id = ?";
        try (
                InputStream inputStream = new FileInputStream(file);
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setBinaryStream(1, inputStream, (int) file.length());
            preparedStatement.setInt(2, productId);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            System.out.println("Ошибка при обновлении изображения: " + e.getMessage());
            // Показываем модальное окно с ошибкой
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при обновлении изображения: ", e.getMessage()));
        }
        return false;
    }

    public Image getProductImage(int productId) {
        String query = "SELECT image_data FROM " + SCHEMA.concat(".products") + " WHERE id = ?";
        try (var preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, productId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                InputStream inputStream = resultSet.getBinaryStream("image_data");
                if (inputStream != null) {
                    return new Image(inputStream);
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка при получении изображения: " + e.getMessage());
            // Показываем модальное окно с ошибкой
            Platform.runLater(() -> ErrorDialog.showError("Ошибка при получении изображения: ", e.getMessage()));
        }
        InputStream defaultImageStream = getClass().getResourceAsStream("/noimage.jpg");
        return new Image(Objects.requireNonNull(defaultImageStream));
    }
}
