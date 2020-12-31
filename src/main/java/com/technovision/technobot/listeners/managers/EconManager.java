package com.technovision.technobot.listeners.managers;

import com.technovision.technobot.data.Configuration;
import com.technovision.technobot.util.exceptions.InvalidBalanceException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class EconManager {

    public static final String SYMBOL = ":sparkles:";
    public static final int SUCCESS_COLOR = 0x77b255;
    public static final DecimalFormat FORMATTER = new DecimalFormat("#,###");

    private final Configuration economy;

    public EconManager() {
        economy = new Configuration("data/", "economy.json") {
            @Override
            public void load() {
                super.load();
                if (!getJson().has("users")) getJson().put("users", new JSONArray());
            }
        };
        economy.save();
    }

    public Pair<Long, Long> getBalance(User user) {
        JSONObject profile = getProfile(user);
        long bal = profile.getLong("balance");
        long bank = profile.getLong("bank");
        return Pair.of(bal, bank);
    }

    public void deposit(User user, long amount) throws InvalidBalanceException {
        JSONObject profile = getProfile(user);
        long bal = profile.getLong("balance");
        long newBalance = bal - amount;
        if (newBalance < 0) {
            throw new InvalidBalanceException();
        }
        long bank = profile.getLong("bank");
        profile.put("bank", bank + amount);
        profile.put("balance", newBalance);
    }

    public void deposit(User user, String amount) throws InvalidBalanceException {
        if (amount.equalsIgnoreCase("all")) {
            JSONObject profile = getProfile(user);
            long bal = profile.getLong("balance");
            long newBalance = 0;
            if (newBalance < 0) {
                throw new InvalidBalanceException();
            }
            long bank = profile.getLong("bank");
            profile.put("bank", bank + bal);
            profile.put("balance", newBalance);
        } else if (amount.equalsIgnoreCase("half")) {
            JSONObject profile = getProfile(user);
            long bal = profile.getLong("balance");
            long newBalance = bal - (bal / 2);
            if (newBalance < 0) {
                throw new InvalidBalanceException();
            }
            long bank = profile.getLong("bank");
            profile.put("bank", bank + (bal / 2));
            profile.put("balance", newBalance);
        }
    }

    public void withdraw(User user, long amount) throws InvalidBalanceException {
        JSONObject profile = getProfile(user);
        long bank = profile.getLong("bank");
        long newBank = bank - amount;
        if (newBank < 0) {
            throw new InvalidBalanceException();
        }
        long bal = profile.getLong("balance");
        profile.put("bank", newBank);
        profile.put("balance", bal + amount);
    }

    public void withdraw(User user, String amount) throws InvalidBalanceException {
        if (amount.equalsIgnoreCase("all")) {
            JSONObject profile = getProfile(user);
            long bank = profile.getLong("bank");
            long newBank = 0;
            if (newBank < 0) {
                throw new InvalidBalanceException();
            }
            long bal = profile.getLong("balance");
            profile.put("bank", newBank);
            profile.put("balance", bank);
        } else if (amount.equalsIgnoreCase("half")) {
            JSONObject profile = getProfile(user);
            long bank = profile.getLong("bank");
            long newBank = bank / 2;
            if (newBank < 0) {
                throw new InvalidBalanceException();
            }
            long bal = profile.getLong("balance");
            profile.put("bank", newBank);
            profile.put("balance", bal / 2);
        }
    }

    public long rob(JSONObject robber, JSONObject victim) throws InvalidBalanceException {
        long bal = victim.getLong("balance");
        if (bal <= 0) {
            throw new InvalidBalanceException();
        }
        long amount = (long) (bal * 0.3);

        removeMoney(victim, amount, Activity.NULL);
        addMoney(robber, amount, Activity.NULL);
        return amount;
    }

    public void removeMoney(JSONObject user, long amount, Activity activity) {
        long bal = user.getLong("balance");
        long remaining = bal - amount;
        user.put("balance", remaining);
        if (activity == Activity.CRIME) {
            user.put("crime-timestamp", System.currentTimeMillis());
        }
        economy.save();
    }

    public void removeMoney(User user, long amount, Activity activity) {
        JSONObject profile = getProfile(user);
        removeMoney(profile, amount, activity);
    }

    public void pay(User sender, User receiver, long amount) throws InvalidBalanceException {
        JSONObject senderProfile = getProfile(sender);
        JSONObject receiverProfile = getProfile(receiver);

        long senderBal = senderProfile.getLong("balance");
        if (senderBal - amount < 0) {
            throw new InvalidBalanceException();
        }
        senderProfile.put("balance", senderBal - amount);

        long receiverBal = receiverProfile.getLong("balance");
        receiverProfile.put("balance", receiverBal + amount);
        economy.save();
    }

    public void addMoney(User user, long amount, Activity activity) {
        JSONObject profile = getProfile(user);
        addMoney(profile, amount, activity);
    }

    public void addMoney(JSONObject user, long amount, Activity activity) {
        long bal = user.getLong("balance");
        user.put("balance", bal + amount);
        switch (activity) {
            case WORK:
                user.put("work-timestamp", System.currentTimeMillis());
                break;
            case CRIME:
                user.put("crime-timestamp", System.currentTimeMillis());
                break;
        }
        economy.save();
    }

    public JSONObject getProfile(User user) {
        JSONArray profiles = economy.getJson().getJSONArray("users");
        for (Object o : profiles) {
            if (((JSONObject) o).getLong("id") == user.getIdLong()) {
                return (JSONObject) o;
            }
        }
        profiles.put(new JSONObject() {{
            put("id", user.getIdLong());
            put("balance", 0);
            put("bank", 0);
            put("work-timestamp", 0);
            put("crime-timestamp", 0);
            put("rob-timestamp", 0);
        }});
        economy.save();
        return (JSONObject) profiles.get(profiles.length() - 1);
    }

    public String getCooldown(long timestamp, int cooldown) {
        long milliseconds = cooldown - (System.currentTimeMillis() - timestamp);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        if (minutes == 0) {
            return hours + " hours";
        }
        return hours + " hours and " + minutes + " minutes";
    }

    public enum Activity {
        WORK, CRIME, NULL;
    }
}
