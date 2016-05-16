package ro.pub.cs.chatclient.responseApi;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by stancioi on 4/24/2016.
 */
public class Response {

    @JsonProperty
    private List<Data> data;

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }
}
