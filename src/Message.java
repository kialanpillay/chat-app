/***
 * This class is encapsulates a simple message into a cohesive object that is sent between a client and server.
 * It consists of two data fields, a header and body.
 * The body is of type object, to allow for any type of data to constitute a message, enabling resusability. 
 * There are two accessor methods for the relevant private data members.
 * @version 1.00
 */

package src;
/**Constructor for message class
 * 
 */
public class Message{

    private String header;
    private Object body;
/**Constructing a message 
 * 
 */
    public Message(String header, Object body){
        this.header = header;
        this.body = body;
    }
    
/** Getting the header of a message
 * 
 * @return the header of the message as a string
 */
    public String getHeader(){
        return header;
    }
/** Getting the body of a message
 * 
 * @return the body of a message as an object
 */
    public Object getBody(){
        return body;
    }
}