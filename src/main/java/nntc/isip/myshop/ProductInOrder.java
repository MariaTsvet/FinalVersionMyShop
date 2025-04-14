package nntc.isip.myshop;

public class ProductInOrder {
    private int id;
    private String name;
    private float price;
    private float count;


    public ProductInOrder(int id, String name, float price, float count) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.count = count;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getPrice() {
        return price;
    }

    public float getCount() {
        return count;
    }

}
