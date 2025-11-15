package org.example;

import jakarta.persistence.EntityManager;
import org.example.config.JPAUtil;

public class Main {
    public static void main(String[] args) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        em.close();

        System.out.println("Tables created successfully!");
    }
}