package com.example.pipeline.flink.models;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigInteger;

//Has to match the exact order of columns in the CSV file
@JsonPropertyOrder({"id","name"})
public class Product {
    public BigInteger id;
    public String name;

    @Override
    public String toString() {
        return "Product[id="+ id + ",name=" + name + "]";
    }
}
