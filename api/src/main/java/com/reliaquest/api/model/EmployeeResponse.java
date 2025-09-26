package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse<T> {

    @JsonProperty("data")
    private T data;

    @JsonProperty("status")
    private String status;
}
