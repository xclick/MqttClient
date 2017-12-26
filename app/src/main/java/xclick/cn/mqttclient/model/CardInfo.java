package xclick.cn.mqttclient.model;

/**
 * Created by Huwei on 2017/12/26.
 */

public class CardInfo {
    private String id ;
    private String name ;
    private String status ;

    public String getId(){
        return this.id ;
    }

    public void setId(String id){
        this.id = id ;
    }

    public String getName(){
        return this.name ;
    }

    public void setName(String name){
        this.name = name ;
    }

    public String getStatus(){
        return this.status ;
    }

    public void setStatus(String status){
        this.status = status ;
    }
}
