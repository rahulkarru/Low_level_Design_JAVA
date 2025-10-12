/******************************************************************************

                            Online Java Compiler.
                Code, Compile, Run and Debug java program online.
Write your code in this editor and press "Run" button to execute it.

*******************************************************************************/
import java.util.*;
interface Subscriber{
     void update(String message);
}
class EmailSubscriber implements Subscriber{
     public void update(String message){
 System.out.println("Received message through Email:"+ message);
     }
}
class SMSSubscriber implements Subscriber{
     public void update(String message){
         System.out.println("received message through sms:"+message);
     }
}
class Publisher{
    List<Subscriber> subscribers=new ArrayList<>();
    private String message;
    public void subscribe(Subscriber s){
        subscribers.add(s);
    }
    public void unsubcribe(Subscriber s){
        subscribers.remove(s);
    }
    public void notifys(){
        for(Subscriber s:subscribers){
            s.update(message);
        }
    }
    public void publishmessage(String msg){
        this.message=msg;
        notifys();
    }
}
public class Main
{
	public static void main(String[] args) {
	   Publisher pub=new Publisher();
	   EmailSubscriber em=new EmailSubscriber();
	   SMSSubscriber sm=new SMSSubscriber();
	   pub.subscribe(em);
	   pub.subscribe(sm);
	   pub.publishmessage("Hello everyOne!");
	   pub.unsubcribe(sm);
	   pub.publishmessage("Good Morning");
	}
}
