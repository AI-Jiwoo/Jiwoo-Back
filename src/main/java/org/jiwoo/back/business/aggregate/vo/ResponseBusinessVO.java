package org.jiwoo.back.business.aggregate.vo;

import org.jiwoo.back.business.dto.BusinessDTO;

import java.util.List;

public class ResponseBusinessVO {
    private String message;
    List<BusinessDTO> business;

    public ResponseBusinessVO() {
    }

    public ResponseBusinessVO(String message, List<BusinessDTO> business) {
        this.message = message;
        this.business = business;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<BusinessDTO> getBusiness() {
        return business;
    }

    public void setBusiness(List<BusinessDTO> business) {
        this.business = business;
    }

    @Override
    public String toString() {
        return "ResponseBusinessVO{" +
                "message='" + message + '\'' +
                ", business=" + business +
                '}';
    }
}
