# Itme29. 이왕이면 제네릭 타입으로 만들라

## 제네릭을 사용하지 않는 Stack

```java
import java.util.Arrays;
import java.util.EmptyStackException;

public class Stack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        elements = new Obejct[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop() {
        if (size == 0)
            throw new EmptyStackException();
        Object result = elements[--size];
        elements[size] - null; // 다 쓴 참조 해제
        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity() {
        if (elements.lenth == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }
}
```

현재 버전의 스택 클래스는 클라이언트에서 스택에서 꺼낸 객체를 형변환해야 하는데, 이 과정에서 런타임 오류가 날 위험이 있다.  
다음 과정을 통해 일반 클래스를 제네릭 클래스로 바꿔보자.

## 1. 클래스 선언에 타입 매개변수를 추가한다. 

이때 타입 이름으로는 보통 E를 사용한다. 

```java
public class Stack<E> {
    private E[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    @SuppressWarnings("unchecked") // -- (2)
    public Stack() {
        elements = new E[DEFAULT_INITIAL_CAPACITY]; // -- (1)
    }

    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public E pop() {
        if (size == 0)
            throw new EmptyStackException();
        E result = elements[--size];
        elements[size] - null; // 다 쓴 참조 해제
        return result;
    }
    // ...
}
```
(1)에서는 실체화 불가 타입(E)으로는 배열을 만들 수 없으므로 해당 코드에서는 오류가 발생한다. 이때 두가지 해결책이 있다. 

### 1. 제네릭 배열 생성을 금지하는 제약을 대놓고 우회하는 방법
```java
elements = (E[])new Object[DEFAULT_INITIAL_CAPACITY];
```
처럼 Object배열을 생성하고 제네릭 배열로 형변환하는 것이다. 이러면 컴파일러는 오류 대신 경고를 내보내지만 이는 일반적으로 타입 안전하지 않다.   
컴파일러는 타입 안전한지 증명할 수 없지만, elements는 private 필드에 저장되고, 클라이언트로 반환되거나 다른 메서드에 전달되는 일이 없다.  
또한 push메서드를 통해 배열에 저자오디는 원소의 타입은 항상 E이므로 이 비검사 형변환은 확실히 안전하므로 범위를 좁혀 @SuppressWarnings 애너테이션으로 
경고를 숨길 수 있다.  
생성자가 비검사 배열 생성 말고는 하는 일이 없으니 생성자 전체에서 경고를 숨겨도 좋다. (2)

### 2. elements필드의 타입을 E[]에서 Object[]로 바꾸는 방법
```java
elements = new Object[DEFAULT_INITIAL_CAPACITY];
```
하면 배열이 반환한 원소를 E로 형변환하면 오류 대신 경고가 뜬다. E는 실체화 불가 타입이므로 컴파일러는 런타임에 이뤄지는 형변환이 안전한지 증명할 방법이 없다.  
이때는 pop 메서드 전체에서 경고를 숨기지 말고, 비검사 형변환을 수행하는 할당문에서 숨기도록 한다.(item27)  
```java
public E pop() {
        if (size == 0)
            throw new EmptyStackException();
        
        // push에서 E타입만 허용하므로 이 형변환은 안전하다. 
        @SuppressWarnings("unchecked") E result = (E) elements[--size];
        
        elements[size] - null; // 다 쓴 참조 해제
        return result;
    }
```

### M1과 M2 비교
1번의 경우 코드가 짧고 가독성이 좋으며 배열의 타입을 E[]로 선언하여 E 타입 인스턴스만 받음을 확실히 어필한다.
또한 1번의 경우 형변환을 배열 생성시 한번만 해주면 되지만, 2번의 경우 배열에서 원소를 읽을 때 마다 해주어야한다. 
따라서 현업에서는 1번의 방식을 선호하지만, 배열의 런타임 타입이 컴파일타임 타입과 달라 힙 오염을 일으킨다. 

> **힙오염**  
> JVM의 힙(Heap) 메모리 영역에 저장되어있는 특정 변수(객체)가 불량 데이터를 참조함으로써, 만일 힙에서 데이터를 가져오려고 할때 얘기치 못한 런타임 에러가 발생할 수 있는 오염 상태 _(item32)_

### 제네릭을 사용한 Stack 사용 예시

```java
public static void main(String[] args) {
    Stack<String> stack = new Stack<>();
    for (String arg : args)
        stack.push(arg);
    while(!stack.isEmpty())
        System.out.println(stack.pop().toUpperCase());
}
```
Stack에서 꺼낸 원소에서 String.toUpperCase()를 호출할 때 명시적 형변환을 수행하지 않으며, 이 형변환이 항상 성공함을 보장한다. 

## 마무리

제네릭 타입 안에서 리스트를 사용하는 게 항상 가능하지도, 꼭 더 좋은 것도 아니다. 자바가 리스트를 기본 타입으로 제공하지 않으므로 ArrayList같은 제네릭 타입도 결국
기본 타입인 배열을 사용해 구현해야한다.  
제네릭 타입은 타입 매개변수에 아무런 제약을 두지 않지만 기본 타입은 사용할 수 없다. 이는 제네릭 타입 시스템의 근본적인 문제이나, 박싱된 기본 타입을
사용해 우회할 수 있다.  

타입 매개변수에 제약을 두는 제네릭 타입도 있다.

```java
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Delayed;

class DelayQueue<E extends Delayed> implements BlockingQueue
```
<E extends Delayed>는 java.util.concurrent.Delayed의 하위 타입만 받는다는 뜻이다. 
이렇게 하여 DelayQueue자신과 DelayQueue를 사용하는 클라이언트는 DelayQueue의 원소에서 (형변환 없이) 곧바로
Delayed클래스의 메서드를 호출할 수 있다. 물론 ClassCastException을 걱정할 필요가 없다. 
이러한 타입 매개변수 E를 **한정적 타입 매개변수**라 한다. 물론 모든 타입은 자기 자신의 하위 타입이므로 DelayQueue<Delayed>도 가능하다.