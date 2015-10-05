package org.eclipse.jdt.core.examples;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        MyInteger i = new MyInteger(1);
        MyInteger j = new MyInteger(3);
        
        MyInteger sum = i+j;
        MyInteger sum2 = i+4;
        System.out.println(sum);
        System.out.println(sum2);
    }
}
