# Item31. 한정적 와일드카드를 사용해 API 유연성을 높이라.

## 매개변수화 타입은 불공변이다.

```text
* 불공변이란?
서로 다른 타입 Type1, Type2가 있을 때, List<Type1>은 List<Type2>의 하위타입도 상위 타입도 아니다.

예를 들어, String은 Object의 하위 타입이지만, List<String>은 List<Object>의 하위타입이 아니다. 
List<Object>에는 어떤 객체도 넣을 수 있지만, List<String>에는 String객체만 넣을 수 있다.(리스코프 치환 원칙에 어긋남)
```

## 한정적 와일드카드 - extends
불공변 방식보다 유연한 방법이다. 
```java
public class Stack<E> {
    public Stack();
    public void push(E e);
    public E pop();
    public void pushAll(Iterable<E> src) {
        for (E e: src)
            push(e);
    }
}
```

위 예시는 Stack클래스와 pushAll메서드 이다. 이때 Stack<Number>로 선언 후, pushAll(intVal)을 호출하면,
(Integer는 Number의 하위 타입 이므로) 논리적으로는 가능할 것 같지만, 실제로는 오류가 뜬다. 매개변수화 타입이 불공변이기 때문이다. 
이러한 상황에 대처할 수 있는 것이 **한정적 와일드카드**이다.

```java
public void pushAll(Iterable<? extends E> src) {
    for (E e: src)
        push(e);
}
```

## 한정적 와일드카드 - super
pushAll과 짝을 이루는 popAll 메서드를 살펴보자
```java
public void popAll(Collections<E> dst) {
    while (!isEmpty())
        dst.add(pop());
}
```
이때 Number를 원소로 갖는 Collection을 Object를 원소로 갖는 Collection에 옮긴다고 할 때, Number는 Object의 하위타입 이므로 논리적으로 가능할 것 같지만 
이 역시, 불공변의 이유로 오류가 발생한다. 이때 popAll의 매개변수 타입은 E Collection이 아닌 E의 상위타입 Collection이어야한다. 
따라서 다음과 같이 수정한다.
```java
public void popAll(Collections<? super E> dst) {
    while (!isEmpty())
        dst.add(pop());
}
```

따라서, 유연성을 극대화하려면 원소의 생산자나 소비자용 입력 매개변수에 와일드카드 타입을 사용하라.
반면, 입력 매개변수가 생산자와 소비자 역할을 동시에 한다면 와일드카드 타입을 써도 좋을 게 없다. 이때는 타입을 정확히 지정해야 하는 상황으로,
이때는 와일드카드 타입을 쓰지 말아야 한다. 

**PECS펙스 : producer-extends, consumer-super**

즉, 매개변수화 타입 T가 생산자라면 &#60;? extends T&#62; 를 사용하고, 소비자라면 &#60;? super T&#62;를 사용하라. 

EX1.
```java
public Chooser(Collections<? extends T> choice)
```
생성자로 넘겨지는 choices 컬렉션은 T타입의 값을 생산하기만 하니 T를 확장(extends)하는 와일드카드 타입을 사용해
선언해야한다. 이렇게 수정하면 Chooser&#60;Number&#62;의 생성자에 List&#60;Integer&#62;를 넘길 수 있다. 

EX2.
```java
public static <E> Set<E> union(Set<? extends E> s1, Set<? extends E> s2) 
```
s1과 s2는 생성자 이므로 위와 같이 선언한다. 반환 타입에서도 한정적 와일드카드 타입을 사용하면 유연성을 높여주기보다 클라이언트 코드에서도
와일드카드 타입을 써야해 유연성이 오히려 떨어진다. 

union함수를 위와 같이 수정하면 다음과 같은 코드가 가능해 진다.

```java
Set<Integer> integers = Set.of(1, 4, 5);
Set<Double> doubles = Set.of(1.0, 4.0, 5.0);
Set<Number> numbers = union(integers, doubles);
```

다만 이 코드는 자바8부터 제대로 컴파일된다. 자바7까지는 타입 추론 능력이 충분히 강력하지 못해 문맥에 맞는 반환 타입(또는 목표 타입)을 명시해야한다. 
union함수 의 목표 타입은 Set&#60;Number&#62;이다. 따라서 다음과 같이 명시적 타입 인수를 추가한다. 

```java
Set<Number> numbers = Union.<Number>union(integers, doubles);
```

EX3.
```java
public static <E extends Comparable<? super E>> E max (
        List<? extends E> list
)
```

입력 매개변수에서는 E인스턴스를 생성하므로 원래의 List&#60;E&#62;를 List&#60;? extends E&#62;로 수정했다.  
타입 매개변수 E의 경우, 원래 선언에서는 E가 Comparable&#60;E&#62;를 확장한다고 정의했는데, 이때 Comparable&#60;E&#62;는 E인스턴스를 소비하고 선후 관계를 뜻하는 정수를 생산한다.  
그래서 매개변수화 타입 Comparable&#60;E&#62;를 한정적 와일드카드 타입인 Comparable&#60;? super E&#62;로 대체했다.  
Comparable은 언제나 소비자이고, 일반적으로 Comparable&#60;E&#62;보다는 Comparable&#60;? super E&#62; 를 사용하는 편이 낫다. 

이렇게 고친 max함수는 매우 복잡해 보이지만 다음의 예시는 이 복잡한 max함수로만 처리가 가능하다.
```java
List<ScheduledFuture<?>> scheduledFutures = ...;
```
왜냐하면 ScheduledFuter가 Comparable&#60;ScheduledFuture&#62;를 구현하지 않았기 때문이다. ScheduledFuture는 Delayed의 하위 인터페이스이고, 
Delayed는 Comparable&#60;Delayed&#62;를 확장했다. 다시말해, ScheduledFuture의 인스턴스는 다른 ScheduledFuture 인스턴스뿐 아니라 Delayed 인스턴스와도 비교할 수 있어서
수정 전 max가 이 리스트를 거부하는 것이다.
더 일반화 해서 말하면, Comparable(혹은 Comparator)을 직접 구현하지 않고, 직접 구현한 다른 타입을 확장한 타입을 지원하기 위해 와일드카드가 필요하다. 

## 비한정적 타입 매개변수와 비한정적 와일드카드

```java
public static <E> void swap(List<E> list, int i, int j);
public static void swap(List<?> list, int i, int j);
```

명시한 두 인덱스의 아이템을 교환(swap)하는 정적 메서드로 첫 번째는 비한정적 타입 매개변수를 사용했고, 두 번째는 비한정적 와일드카드를 사용했다.
public API의 경우 어떤 리스트든 넘기면 교환가능하고, 신경 써야 할 타입 매개변수가 없기 때문에 두번째가 낫다.  
기본 규칙은 메서드 선언에 타입 매개변수가 한 번만 나오면 와일드 카드로 대체하라이다. 비한정적 타입 매개변수는 비한정적 와일드카드로 바꾸고, 한정적 타입 매개변수라면
한정적 와일드카드로 바꾼다.  
하지만 두 번째 경우 문제가 하나 있는데 다음의 코드가 컴파일 되지 않는다. 

```java
public static void swap(List<?> list, int i, int j) {
    list.set(i, list.set(j, list.get(i)));
}
```
리스트 타입이 List&#60;?&#62;인데, List&#60;?&#62;에는 null외에는 어떤 값도 넣을 수 없기때문에 발생하는 오류이다.  
이때 형변환이나 리스트의 로 타입을 사용하지 않고도 해결할 길이 있다. 바로 와일드카드 타입의 실제 타입을 알려주는 메서드를 private도우미 메서드로 따로
작성하여 활용하는 것이다. 실제 타입을 알아내려면 이 도우미 메서드는 제네릭 메서드여야한다. 

```java
public static void swap(List<?> list, int i, int j) {
    swapHelper(list, i , j);
}

// 와일드카드 타입을 실제 타입으로 바꿔주는 private 도우미 메서드
private static <E> void swapHelper(List<E> list, int i, int j) {
    list.set(i, list.set(j, list.get(i)));
}
```

swapHelper메서드는 리스트가 List<E>임을 알고 있다. 즉, 이 리스트에서 꺼낸 값의 타입은 항상 E이고, E타입의 값이라면 이 리스트에 넣어도 안전함을 알고있다. 
즉, swap메서드를 호출하는 클라이언트는 복잡한 swapHelper의 존재를 모른 채 그 혜택을 누리는 것이다. 
