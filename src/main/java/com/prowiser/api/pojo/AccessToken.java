package com.prowiser.api.pojo;

public class AccessToken {
    private String accessToken;
    private Integer expiresin;
    private Long pastAt;

    @Override
    public String toString() {
        return "AccessToken{" +
                "accessToken='" + accessToken + '\'' +
                ", expiresin=" + expiresin +
                ", pastAt=" + pastAt +
                '}';
    }

    public Long getPastAt() {
        return pastAt;
    }

    public void setPastAt(Long pastAt) {
        this.pastAt = pastAt;
    }

    public Integer getExpiresin() {
        return expiresin;
    }

    public void setExpiresin(Integer expiresin) {
        this.expiresin = expiresin;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
