/***
 * This class is encapsulates a simple message into a cohesive object that is sent between a client and server.
 * It consists of two data fields, a header and body.
 * The body is of type object, to allow for any type of data to constitute a message, enabling resusability. 
 * There are two accessor methods for the relevant private data members.
 * @version 1.00
 */

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