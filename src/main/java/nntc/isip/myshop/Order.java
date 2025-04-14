package nntc.isip.myshop;

public class Order {
    private int id;
    private String date;
    private String customer;
    public int customerId;


    public Order(int id, String date, String customer) {
        this.id = id;
        this.date = date;
        this.customer = customer;
    }

    public void setCustomer(int customerId) {
        this.customerId = customerId;
    }

    public int getId() {
        return id;
    }

    public int getCustomerId(){
        return customerId;
    }

    public String getDate() {
        return date;
    }

    public String getCustomer() {
        return customer;
    }

}
