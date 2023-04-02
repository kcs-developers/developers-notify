package com.developers.notify.developers.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class Subscribes implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String queueName;
}

