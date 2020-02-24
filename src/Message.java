package src;

public class Message{

    private String header;
    private Object body;

    public Message(String header, Object body){
        this.header = header;
        this.body = body;
    }

    public String getHeader(){
        return header;
    }

    public Object getBody(){
        return body;
    }
}