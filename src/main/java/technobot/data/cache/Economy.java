package technobot.data.cache;

/**
 * POJO object that stores server economy data for a user.
 *
 * @author TechnoVision
 */
public class Economy {

    private long guild;
    private long user;
    private long balance;
    private long bank;

    public Economy() { }

    public Economy(long guild) {
        this.guild = guild;
    }

    public long getGuild() {
        return guild;
    }

    public void setGuild(long guild) {
        this.guild = guild;
    }

    public long getUser() {
        return user;
    }

    public void setUser(long user) {
        this.user = user;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getBank() {
        return bank;
    }

    public void setBank(long bank) {
        this.bank = bank;
    }
}
