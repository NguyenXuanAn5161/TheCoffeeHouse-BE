package com.example.Coffee.dto.filter;

import lombok.Data;

@Data
public class ProductFilterDto {
    private String name;
    private String category;
    private Boolean isNew;
}
