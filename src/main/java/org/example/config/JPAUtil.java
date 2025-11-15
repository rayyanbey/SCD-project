package org.example.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class JPAUtil {

    private static final EntityManagerFactory emf;

    static {
        Map<String, String> config = new HashMap<>();

        config.put("jakarta.persistence.jdbc.url", System.getenv("DB_URL"));
        config.put("jakarta.persistence.jdbc.user", System.getenv("DB_USER"));
        config.put("jakarta.persistence.jdbc.password", System.getenv("DB_PASSWORD"));

        emf = Persistence.createEntityManagerFactory("logisimPU", config);
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }
}
