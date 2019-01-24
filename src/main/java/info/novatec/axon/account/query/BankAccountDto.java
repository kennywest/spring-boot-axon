package info.novatec.axon.account.query;

public class BankAccountDto {

    private final String id;
    private double balance;
    private final String owner;

    public BankAccountDto(String id, double balance, String owner) {
        this.id = id;
        this.balance = balance;
        this.owner = owner;
    }

    public String getId() {
        return this.id;
    }

    public double getBalance() {
        return this.balance;
    }

    public void deposit(double amount) {
        this.balance += amount;

    }

    public String getOwner() {
        return this.owner;
    }

}
