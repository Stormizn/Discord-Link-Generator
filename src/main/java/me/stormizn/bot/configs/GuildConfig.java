package me.stormizn.bot.configs;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class GuildConfig {

    // anti-ping
    private Set<String> helpChannels = new HashSet<>();
    private Set<String> coreRoles = new HashSet<>();

    // anti-spam
    private int spamThreshold = 5;
    private int spamWindowSeconds = 10;

    // commands
    private String websiteUrl = "";
    private String outdatedMessage = "You appear to be using an outdated version. Please update to the latest release.";

    // thread management
    private String resolvedTag = "Resolved";
    private String staleTag = "Stale";

    // faq
    private LinkedHashMap<String, String> faq = new LinkedHashMap<>();

    public Set<String> getHelpChannels() { return helpChannels; }
    public Set<String> getCoreRoles() { return coreRoles; }
    public int getSpamThreshold() { return spamThreshold; }
    public void setSpamThreshold(int v) { spamThreshold = v; }
    public int getSpamWindowSeconds() { return spamWindowSeconds; }
    public void setSpamWindowSeconds(int v) { spamWindowSeconds = v; }
    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String v) { websiteUrl = v; }
    public String getOutdatedMessage() { return outdatedMessage; }
    public void setOutdatedMessage(String v) { outdatedMessage = v; }
    public String getResolvedTag() { return resolvedTag; }
    public void setResolvedTag(String v) { resolvedTag = v; }
    public String getStaleTag() { return staleTag; }
    public void setStaleTag(String v) { staleTag = v; }
    public LinkedHashMap<String, String> getFaq() { return faq; }

}
