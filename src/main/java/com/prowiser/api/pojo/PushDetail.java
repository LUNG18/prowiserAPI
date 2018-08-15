package com.prowiser.api.pojo;

public class PushDetail {
    private Integer id;
    private String userInfo;
    private String tempId;
    private Integer retry;
    private Integer status;
    private Integer pushAt;
    private Integer createdAt;

    @Override
    public String toString() {
        return "PushDetail{" +
                "id=" + id +
                ", userInfo='" + userInfo + '\'' +
                ", tempId='" + tempId + '\'' +
                ", retry=" + retry +
                ", status=" + status +
                ", pushAt=" + pushAt +
                ", createdAt=" + createdAt +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getTempId() {
        return tempId;
    }

    public void setTempId(String tempId) {
        this.tempId = tempId;
    }

    public Integer getRetry() {
        return retry;
    }

    public void setRetry(Integer retry) {
        this.retry = retry;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPushAt() {
        return pushAt;
    }

    public void setPushAt(Integer pushAt) {
        this.pushAt = pushAt;
    }

    public Integer getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Integer createdAt) {
        this.createdAt = createdAt;
    }
}
