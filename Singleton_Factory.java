/******************************************************************************

                            Online Java Compiler.
                Code, Compile, Run and Debug java program online.
Write your code in this editor and press "Run" button to execute it.

*******************************************************************************/
// class Singleton{
//     private static Singleton instance;
//     private Singleton(){
        
//     }
//     public static Singleton getInstance(){
//         if(instance==null){
//             instance=new Singleton();
//         }
//         return instance;
//     }
// }
interface Shape{
    void draw();
}
class Rectangle implements Shape{
    @Override
    public void draw(){
        System.out.println("Rectangle is Drawing..");
    }
}
class Square implements Shape{
    @Override
    public void draw(){
        System.out.println("Square is Drawing...");
    }
}
class ShapeFactory{
    public static Shape create_shape(String k){
        if(k.equalsIgnoreCase("rectangle")){
            return new Rectangle();
        }
        else if(k.equalsIgnoreCase("square")){
            return new Square();
        }
        throw new IllegalArgumentException("Unknow shape Error");
    }
}
public class Main
{
	public static void main(String[] args) {
	   //Singleton s=Singleton.getInstance();
	   Shape shape1=ShapeFactory.create_shape("square");
	   shape1.draw();
	   Shape shape2=ShapeFactory.create_shape("rectangle");
	   shape2.draw();
	}
}
