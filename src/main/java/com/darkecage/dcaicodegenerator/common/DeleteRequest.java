package com.darkecage.dcaicodegenerator.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
