# Itme22. 인터페이스는 타입을 정의하는 용도로만 사용하라

## 인터페이스의 용도
인터페이스는 자신을 구현한 클래스의 인스턴스를 참조할 수 있는 타입 역할을 한다. 
즉 클래스가 어떤 인터페이스를 구현한다는 것은 자신의 인터페이스로 무엇을 할 수 있는지를 클라이언트에 얘기해주는 것이다.

## 상수 인터페이스
메서드 없이 상수를 뜻하는 static final 필드로만 구성된 인터페이스로 이는 인터페이스의 용도에 맞지 않다. 
```java
public interface ExampleConstants {
    static final double BNUMBER = 2.21;
    static final double ANUMBER = 1.1;
}
```
클래스 내부에서 사용하는 상수는 내부 구현해 해당되므로 상수 인터페이스를 구현하는 것은 내부 구현을 클래스의 API로 노출하는 행위이다. 

## 상수를 공개하고 싶다면
1. 특정 클래스나 인터페이스와 강하게 연관되어있다면 그 클래스나 인터페이스 자체에 추가해야한다.  
Integer, Double에 선언된 MIN_VALUE, MAX_VALUE가 그 예이다. 
2. 열거타입으로 만들어 공개한다.
3. 인스턴스화 할 수 없는 유틸리티 클래스에 담는다.
```java
public class ExampleConstants {
    private ExampleConstants() {}

    public static final double BNUMBER = 2.21;
    public static final double ANUMBER = 1.1;
}
```
위의 코드는 앞선 예의 유틸리티 클래스 버전으로 해당상수를 사용하려면 클래스 이름까지 함께 명시하거나 import를 통해 클래스이름은 생략 가능하다. 

