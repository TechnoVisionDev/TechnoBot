package com.technovision.technobot.util.enums;

import static com.technovision.technobot.commands.Command.PREFIX;

public enum SuggestionResponse {

    APPROVE("Approved", PREFIX + "approve", 0xd2ffd0),
    DENY("Denied", PREFIX + "deny", 0xffd0ce),
    CONSIDER("Considered", PREFIX + "consider", 0xfdff91),
    IMPLEMENTED("Implemented", PREFIX + "implement", 0x91fbff);

    private final String response;
    private final String cmd;
    private final int color;

    SuggestionResponse(String response, String cmd, int color) {
        this.response = response;
        this.cmd = cmd;
        this.color = color;
    }

    public String getResponse() {
        return response;
    }

    public String getCommand() {
        return cmd;
    }

    public int getColor() {
        return color;
    }
}
