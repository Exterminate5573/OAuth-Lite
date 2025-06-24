package dev.exterminate.oauthlite.data;

import lombok.Data;

@Data
public class AuthCodeResponse {
    private String code;
    private String state;
    private boolean error = false;
}
