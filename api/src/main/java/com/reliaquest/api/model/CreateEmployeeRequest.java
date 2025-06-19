package com.reliaquest.api.model;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateEmployeeRequest {
    @NotBlank
    private String name;

    @Positive
    @NotNull
    private Integer salary;

    @Min(0)
    @NotNull
    private Integer age;

    @NotBlank
    private String title;
}