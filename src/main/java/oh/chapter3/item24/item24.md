# Itme24. 멤버 클래스는 되도록 static으로 만들라

## 중첩클래스
중첩 클래스란 다른 클래스 안에 정의된 클래스이다. 중첩 클래스는 자신을 감싼 바깥 클래스에서만 쓰여야 하며, 그 외의 쓰임새가 있다면 톱레벨 클래스로 만들어야 한다.
중첩 클래스의 종류는 정적 멤버 클래스 , (비정적) 멤버 클래스, 익명 클래스, 지역 클래스가 있다.

## 정적 멤버 클래스
다른 클래스 안에 선언되고, 바깥 클래스의 private 멤버에도 접근할 수 있다는 것을 제외하면 일반 클래스와 똑같다.  

흔히 바깥 클래스와 함께 쓰일 때만 유용한 public 도우미 클래스로 쓰인다.
```java
class Calculator {
    // ...
    public static enum Operation {
        PLUS, MINUS
    }
}
```
Operation 열거 타입은 Calculator 클래스의 public 정적 멤버 클래스가 되어야 한다. 그러면 Calculator 클라이언트에서 
```text
Calculator.Operation.PLUS
Calculator.Operation.MINUS
```
등의 형태로 원하는 연산을 참조할 수 있다.   

> **정적 멤버 클래스와 (비정적) 멤버 클래스의 차이**  

**<구문상의 차이>**  
정적 멤버 클래스 : static이 붙는다.   
비정적 멤버 클래스 : static이 안붙는다.  

**<의미상의 차이>**  
비정적 멤버 클래스의 인스턴스는 바깥 클래스의 인스턴스와 암묵적으로 연결된다. 그래서 비정적 멤버 클래스의 인스턴스 메서드에서 정규화된 this를 사용해 
바깥 인스턴스의 메서드를 호출하거나 바깥 인스턴스의 참조를 가져올 수 있다. 
```text
정규화된 this란?
[클래스명.this] 의 형태로 바깥 클래스의 이름을 명시하는 용법
```
따라서 비정적 멤버 클래스는 바깥 인스턴스 없이는 생성할 수 없다.  
반면, 정적 멤버클래스는 중첩 클래스의 인스턴스가 바깥 인스턴스와 독립적으로 존재할 수 있다.

## 비정적 멤버 클래스 

비정적 멤버 클래스가 인스턴스화될 때 비정적 멤버 클래스의 인스턴스와 바깥 인스턴스 사이의 관계가 확립되어 변경할 수 없다.
이 관계는 보통 바깥 클래스의 인스턴스 메서드에서 비정적 멤버 클래스의 생성자를 호출할 때 자동으로 만들어 지지만,
가끔 _바깥 인스턴스의 클래스.new MemberClass(args)_ 를 호출해 수동으로 만들기도 한다.  
이러한 관계 정보는 비정적 멤버 클래스의 인스턴스 안에 만들어져 메모리 공간을 차지하고 생성 시간도 더 걸린다.

비정적 멤버 클래스는 어댑터를 정의할 때 자주 쓰인다. 즉, 어떤 클래스의 인스턴스를 감싸 마치 다른 클래스의 인스턴스처럼 보이게 하는 뷰로 사용하는 것이다.
```text
어댑터(패턴)란?
호환되지 않는 인터페이스들을 연결하는 디자인패턴이다. 즉, 기존의 클래스를 수정하지 않고도, 특정 인터페이스를 필요로하는 코드에서 사용할 수 있게 해준다.
이를 통해 서로 다른 인터페이스를 가진 클래스들이 상호 작용할 수 있도록 해서 코드의 재사용성을 증대시킨다. 
다음 코드의 예시는 220v와 110v를 변환해주는 어댑터이다.
```
```java
// 220v 인터페이스
public interface Electronic220v {
    void connect();
}
public class AirConditioner implements Electronic220v{
    @Override public void connect() {System.out.println("에어컨 220v on");}
}
public class Cleaner implements Electronic220v{
    @Override public void connect() {System.out.println("청소기 220v on");}
}

// 110v 인터페이스
public interface Electronic110v {
    void powerOn();
}
public class HairDryer implements Electronic110v{
    @Override public void powerOn() {System.out.println("헤어 드라이기 110v");}
}

// Electronic110v를 implements한다.
public class ElectronicAdapter implements Electronic110v{
    //Electronic220v를 비정적 멤버 클래스로 갖는다. 
    private Electronic220v electronic220v;

    //맨처음 생성자에 Electronic220v를 넣어 가지고 있는다 
    public ElectronicAdapter(Electronic220v electronic220v) {
        this.electronic220v = electronic220v;
    }

    //Electronic110v를 실행하면 같은 기능을 하는 
    //connect가 실행되도록하여 PowerOn로 구현된 것처럼 맞춰준다.
    @Override
    public void powerOn() {
        electronic220v.connect();
    }
}

public class AdapterTest {
    public static void main(String[] args) {
        Electronic110v hairDryer = new HairDryer(); //헤어 드라이기 110v
        Electronic110v cleaner = new ElectronicAdapter(new Cleaner()); //청소기 220v on
        Electronic110v airConditioner = new ElectronicAdapter(new AirConditioner()); //에어컨 220v on
    }
}
```
또 다른 예시는 다음과 같다.
```java
import java.util.AbstractSet;
import java.util.Iterator;

public class MySet<E> extends AbstractSet<E> {
    //...
    @Override public Iterator<E> iterator() { return new MyIterator(); }
    
    // 비정적 멤버 클래스
    private class MyIterator implements Iterator<E> {}
}
```
Map 인터페이스 구현체들은 보통 자신의 컬렉션 뷰를 구현할 때 비정적 멤버 클래스를 사용한다. 비슷하게 Set, List같은 다른 컬렉션 인터페이스 구현들도
자신의 반복자를 구현할 때 비정적 멤버 클래스를 주로 사용한다. 

따라서 멤버클래스에서 바깥 인스턴스에 접근할 일이 없다면 무조건 static을 붙여서 정적 멤버 클래스로 만들도록 한다. static을 생략할 경우 바깥 인스턴스로의 
숨은 외부 참조를 갖게 된다. 이 참조를 저장하기 위해 시간과 공간이 낭비될 뿐 아니라 가비지 컬렉션이 바깥 클래스의 인스턴스를 수거하지 못해 메모리 누수가 생길 수도 있다. 

> **private 정적 멤버 클래스**  

바깥 클래스가 표현하는 객체의 한 부분을 나타낼 때 쓴다. Map 인스턴스를 예시로 들 수 있다. 
Map인스턴스는 키와 값을 매핑시키는데 많은 Map구현체는 각각의 키-값 쌍을 표현하는 엔트리(Entry) 객체들을 가지고 있다. 모든 엔트리가
맵과 연관되어 있지만, 엔트리의 메서드들(getKey, getValue, setValue)는 맵을 직접 사용하지 않는다. 따라서 엔트리를 비정적 멤버 클래스로 
표현하는 것은 낭비이고, private 정적 멤버 클래스가 가장 알맞다.  
엔트리를 비정적 멤버 클래스로 사용해도 동작에는 문제가 없지만, 모든 엔트리가 바깥 맵으로의 참조를 가져 많은 시간과 공간을 낭비할 것이다. 

> **public, protected멤버 클래스**

멤버 클래스가 공개된 public, protected의 경우에는 정적/비정적이 더욱 중요하다. 멤버 클래스가 공개 API가 되니 향후 static을 붙이게 되면 하위 호환성이 깨진다. 

## 익명 클래스 

익명 클래스는 이름이 없는 클래스로 바깥 클래스의 멤버도 아니다. 멤버와 달리 쓰이는 시점에 선언과 동시에 인스턴스가 만들어진다.  
코드 어디서든 만들 수 있고, 오직 비정적인 문맥에서 사용될 때만 바깥 클래스의 인스턴스를 참조할 수 있다.  
정적 문맥에서라도 상수 변수 이외의 정적 멤버는 가질 수 없다. 즉 상수표현을 위해 초기화된 final 기본 타입과 문자열 필드만 가질 수 있다.  
람다 지원 전에는 작은 함수 객체나 처리 객체를 만드는데 주로 사용했지만 현재는 람다가 대체하고 있다.  
정적 팩터리 메서드를 구현할때 주로 사용된다. 

> **익명 클래스의 한계**
1. 선언한 시점에서만 인스턴스를 만들 수 있다. 
2. instanceof 검사나 클래스의 이름이 필요한 작업은 불가능하다.
3. 여러 인터페이스를 구현할 수 없고, 인터페이스를 구현하는 동시에 다른 클래스를 상속할 수도 없다. 
4. 익명 클래스를 사용하는 클라이언트는 그 익명 클래스가 상위 타입에서 상속한 멤버 외에는 호출할 수 없다. 
5. 길수록 가독성이 떨어진다.

## 지역 클래스

가장 드물게 사용되는 중첩 클래스로 지역클래스는 지역변수를 선언할 수 있는 곳이면 어디서든 선언 가능하고, 유효범위도 지역변수와 같다.
멤버 클래스처럼 이름이 있고, 반복해서 사용할 수 있다.  
익명클래스처럼 비정적 문맥에서 사용될 때만 바깥 인스턴스를 참조할 수 있으며, 정적 멤버는 가질수 없고, 가독성을 위해 짧게 작성해야한다. 

## 마무리
메서드 밖에서도 사용해야하고, 정의하기에 코드 길이가 길다면? 멤버 클래스
멤버 클래스의 인스턴스 각각이 바깥 인스턴스를 참조한다면? 비정적 / 그렇지 않으면? 정적
한 메서드 안에서만 중첩 클래스가 쓰이고 그 인스턴스를 생성하는 지점이 한곳이며 해당 타입으로 쓰기에 적합한 클래스나 인스턴스가 이미 있다면? 익명 클래스 / 그렇지 않으면? 지역 클래스