package com.example.pipeline.flink.models;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigInteger;

//Has to match the exact order of columns in the CSV file
@JsonPropertyOrder({"id","name","country"})
public class Customer {
//    @JsonDeserialize(using = NumberDeserializers.BigDecimalDeserializer.class)
    public BigInteger id;
    public String name;
    public String country;

    @Override
    public String toString() {
        return "Customer[id="+ id + ",name=" + name + ",country=" + country + "]";
    }
}
