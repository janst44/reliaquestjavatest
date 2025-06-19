package com.reliaquest.api.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Response<T> {
    private T data;

}

