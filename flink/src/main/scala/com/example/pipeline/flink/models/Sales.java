package com.example.pipeline.flink.models;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigInteger;

//Has to match the exact order of columns in the CSV file
@JsonPropertyOrder({"product_id","customer_id","qty"})
public class Sales {
    public BigInteger productId;
    public BigInteger customerId;
    public BigInteger qty;

    @Override
    public String toString() {
        return "Sales[product_id="+ productId + ",customer_id=" + customerId + ",qty=" + qty + "]";
    }
}
