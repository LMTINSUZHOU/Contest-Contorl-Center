package org.example.contest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 应用级配置集中入口，便于后续把本地文件存储替换为对象存储或云服务。
 */
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private final Security security = new Security();
    private final Storage storage = new Storage();
    private final Admin admin = new Admin();

    public Security getSecurity() {
        return security;
    }

    public Storage getStorage() {
        return storage;
    }

    public Admin getAdmin() {
        return admin;
    }

    public static class Security {
        private String jwtSecret;
        private long jwtExpirationMinutes;

        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        public long getJwtExpirationMinutes() {
            return jwtExpirationMinutes;
        }

        public void setJwtExpirationMinutes(long jwtExpirationMinutes) {
            this.jwtExpirationMinutes = jwtExpirationMinutes;
        }
    }

    public static class Storage {
        private String certificateDir;

        public String getCertificateDir() {
            return certificateDir;
        }

        public void setCertificateDir(String certificateDir) {
            this.certificateDir = certificateDir;
        }
    }

    public static class Admin {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
