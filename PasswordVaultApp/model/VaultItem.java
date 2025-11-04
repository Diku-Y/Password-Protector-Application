package model;

public class VaultItem {
    private int id;
    private String site;
    private String siteUsername;
    private String sitePassword;

    // Constructor with ID (for reading from DB or updating)

    public VaultItem(int id, String site, String siteUsername, String sitePassword) {
        this.id = id;
        this.site = site;
        this.siteUsername = siteUsername;
        this.sitePassword = sitePassword;
    }

    // Constructor without ID (for adding new items)

    public VaultItem(String site, String siteUsername, String sitePassword) {
        this.id = -1; // -1 means not yet stored in DB
        this.site = site;
        this.siteUsername = siteUsername;
        this.sitePassword = sitePassword;
    }

    // Getters

    public int getId() { return id; }
    public String getSite() { return site; }
    public String getSiteUsername() { return siteUsername; }
    public String getSitePassword() { return sitePassword; }

    // Setters

    public void setId(int id) { this.id = id; }
    public void setSite(String site) { this.site = site; }
    public void setSiteUsername(String siteUsername) { this.siteUsername = siteUsername; }
    public void setSitePassword(String sitePassword) { this.sitePassword = sitePassword; }
}
