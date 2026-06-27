package com.yastro.npc.model;

public record ActionData(String type, String value, String trigger, String permission) {
    public ActionData {
        if (trigger == null || trigger.isBlank()) trigger = "direito";
        if (permission == null) permission = "";
    }

    public ActionData(String type, String value, String trigger) { this(type, value, trigger, ""); }

    public ActionData(String type, String value) { this(type, value, "direito", ""); }

    public boolean firesOn(String click) {
        return "qualquer".equalsIgnoreCase(trigger) || trigger.equalsIgnoreCase(click);
    }

    public ActionData withPermission(String perm) { return new ActionData(type, value, trigger, perm); }
}
