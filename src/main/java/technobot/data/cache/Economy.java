package technobot.data.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * POJO object that stores server economy data for a user.
 *
 * @author TechnoVision
 */
public class Economy {

    private long guild;
    private Long user;
    private Long balance;
    private Long bank;

    private LinkedHashMap<String,Long> inventory;

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

    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Long getBank() {
        return bank;
    }

    public void setBank(Long bank) {
        this.bank = bank;
    }

    public LinkedHashMap<String,Long> getInventory() {
        return inventory;
    }

    public void setInventory(LinkedHashMap<String,Long> inventory) {
        this.inventory = inventory;
    }
}
