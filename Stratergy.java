/******************************************************************************

                            Online Java Compiler.
                Code, Compile, Run and Debug java program online.
Write your code in this editor and press "Run" button to execute it.

*******************************************************************************/

interface PaymentStratergy{
    void pay(int amount);
}
class UpiPayment implements PaymentStratergy{
    @Override
    public void pay(int amount){
        System.out.println("Paid amount"+amount+"using UPI");
    }
}
class NetBanking implements PaymentStratergy{
    @Override
    public void pay(int amount){
        System.out.println("Paid amount:"+amount+" using NetBanking");
    }
}
class Payment{
    private PaymentStratergy payment;
    public void setpayment(PaymentStratergy p){
        payment=p;
    }
    public void checkout(int amout){
        payment.pay( amout);
    }
}
public class Main
{
	public static void main(String[] args) {
	   Payment payr=new Payment();
	   payr.setpayment(new NetBanking());
	   payr.checkout(500);
	}
}
