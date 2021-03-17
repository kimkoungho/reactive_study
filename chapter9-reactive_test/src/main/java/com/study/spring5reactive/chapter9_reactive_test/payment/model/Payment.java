package com.study.spring5reactive.chapter9_reactive_test.payment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;


public class Payment {
    @Id
    private String id;

    private String user;

    public Payment() {
    }

    public Payment(String id, String user) {
        this.id = id;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Payment withUser(String user) {
        return this.user.equals(user) ? this : new Payment(this.id, user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id) &&
                Objects.equals(user, payment.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user);
    }
}
