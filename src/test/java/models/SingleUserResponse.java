package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO representing a single user response from ReqRes API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleUserResponse {
    private User data;
    private UserListResponse.Support support;

    public SingleUserResponse() {}

    public User getData() {
        return data;
    }

    public void setData(User data) {
        this.data = data;
    }

    public UserListResponse.Support getSupport() {
        return support;
    }

    public void setSupport(UserListResponse.Support support) {
        this.support = support;
    }
}
