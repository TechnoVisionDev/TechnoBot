package technobot.data.cache;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.codecs.pojo.annotations.BsonProperty;
import technobot.handlers.economy.EconomyHandler;
import technobot.util.embeds.EmbedColor;

/**
 * POJO object that stores a shop item.
 *
 * @author TechnoVision
 */
public class Item {

    private String uuid;

    private String name;

    private Long price;

    private String description;

    @BsonProperty("show_in_inventory")
    private Boolean showInInventory;

    @BsonProperty("expire_timestamp")
    private Long expireTimestamp;

    private Long stock;

    @BsonProperty("required_role")
    private Long requiredRole;

    @BsonProperty("given_role")
    private Long givenRole;

    @BsonProperty("removed_role")
    private Long removedRole;

    @BsonProperty("required_balance")
    private Long requiredBalance;

    @BsonProperty("reply_message")
    private String replyMessage;

    public Item() { }

    public Item(String name) {
        this.uuid = RandomStringUtils.randomAlphanumeric(8);
        this.name = name;
        this.price = 0L;
        this.description = null;
        this.showInInventory = true;
        this.expireTimestamp = null;
        this.stock = null;
        this.requiredRole = null;
        this.givenRole = null;
        this.removedRole = null;
        this.requiredBalance = null;
        this.replyMessage = null;
    }

    /**
     * Converts item details into a Discord embed.
     *
     * @param currency - the currency symbol for the guild this object is from.
     * @return a nice, readable embed with full item details.
     */
    public MessageEmbed toEmbed(String currency) {
        String priceField = currency+" "+ EconomyHandler.FORMATTER.format(price);
        String descField = (description != null) ? description : "None provided";
        String inventoryField = (showInInventory) ? "Yes" : "No";
        String timeField = (expireTimestamp != null) ? TimeFormat.RELATIVE.format(expireTimestamp) : "None";
        String stockField = (stock != null) ? EconomyHandler.FORMATTER.format(stock) : "Infinity";
        String roleReqField = (requiredRole != null) ? "<@&"+requiredRole+">" : "None";
        String roleGivenField = (givenRole != null) ? "<@&"+givenRole+">" : "None";
        String roleRemovedField = (removedRole != null) ? "<@&"+removedRole+">" : "None";
        String reqBalance = (requiredBalance != null) ? currency+" "+EconomyHandler.FORMATTER.format(requiredBalance) : "None";
        String reply = (replyMessage != null) ? replyMessage : "None";
        return new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .setTitle("Item Details")
                .addField("Name", name, true)
                .addField("Price", priceField, true)
                .addField("Description", descField, false)
                .addField("Show in inventory?", inventoryField, true)
                .addField("Expiration date", timeField, true)
                .addField("Stock remaining", stockField, true)
                .addField("Role required", roleReqField, true)
                .addField("Role given", roleGivenField, true)
                .addField("Role removed", roleRemovedField, true)
                .addField("Required balance", reqBalance, true)
                .addField("Reply message", reply, true)
                .build();
    }

    /**
     * Check if item has expired based on expire timestamp
     * @return true if expired, otherwise false.
     */
    public boolean checkIfExpired() {
        return this.getExpireTimestamp() != null && this.getExpireTimestamp() <= System.currentTimeMillis();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getShowInInventory() {
        return showInInventory;
    }

    public void setShowInInventory(Boolean showInInventory) {
        this.showInInventory = showInInventory;
    }

    public Long getExpireTimestamp() {
        return expireTimestamp;
    }

    public void setExpireTimestamp(Long expireTimestamp) {
        this.expireTimestamp = expireTimestamp;
    }

    public Long getStock() {
        return stock;
    }

    public void setStock(Long stock) {
        this.stock = stock;
    }

    public Long getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(Long requiredRole) {
        this.requiredRole = requiredRole;
    }

    public Long getGivenRole() {
        return givenRole;
    }

    public void setGivenRole(Long givenRole) {
        this.givenRole = givenRole;
    }

    public Long getRemovedRole() {
        return removedRole;
    }

    public void setRemovedRole(Long removedRole) {
        this.removedRole = removedRole;
    }

    public Long getRequiredBalance() {
        return requiredBalance;
    }

    public void setRequiredBalance(Long requiredBalance) {
        this.requiredBalance = requiredBalance;
    }

    public String getReplyMessage() {
        return replyMessage;
    }

    public void setReplyMessage(String replyMessage) {
        this.replyMessage = replyMessage;
    }
}
