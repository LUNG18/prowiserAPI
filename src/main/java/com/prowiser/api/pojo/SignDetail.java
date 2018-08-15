package com.prowiser.api.pojo;

public class SignDetail {
    private Integer id;
    private String sign;
    private Integer createdAt;

    public SignDetail(Integer id, String sign, Integer createdAt) {
        this.id = id;
        this.sign = sign;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public Integer getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Integer createdAt) {
        this.createdAt = createdAt;
    }
}
