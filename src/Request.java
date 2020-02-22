package src;

public class Request{

    private String header;
    private String message;

    public Request(String header, String message){
        this.header = header;
        this.message = message;
    }

    public String getHeader(){
        return header;
    }

    public String getMessage(){
        return message;
    }
}