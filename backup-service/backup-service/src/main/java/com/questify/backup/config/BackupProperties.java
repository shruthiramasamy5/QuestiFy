package com.questify.backup.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "questify.backup")
public class BackupProperties {

    /** Prefix used to derive a storage reference when the client does not supply one. */
    private String storagePrefix = "questify-backups";

    public String getStoragePrefix() {
        return storagePrefix;
    }

    public void setStoragePrefix(String storagePrefix) {
        this.storagePrefix = storagePrefix;
    }
}
