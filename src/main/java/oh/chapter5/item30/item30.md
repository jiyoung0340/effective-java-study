# Item30. 이왕이면 제네릭 메서드로 만들라

## 제네릭 메서드

매개변수화 타입을 받는 정적 유틸리티 메서드는 보통 제네릭이다. 예를 들어 Collections의 '알고리즘' 메서드(binarySearch, sort 등)는 모두 제네릭이다. 

```java
// 변경 전 코드 - 컴파일은 가능하나 경고 발생
public static Set union(Set s1, Set s2) {
    Set result = new HashSet(s1);
    result.addAll(s2);
    return result;
}
```
위의 코드는 두 집합을 매개변수로 받아 합집합을 반환하는 메서드인데, 타입 안전하지 않아 경고가 발생한다.  
메서드 선언에서 (입력 2개, 반환 1개 총) 3개의 집합의 원소 타입을 타입 매개변수로 명시하고, 
메서드 안에서도 이 타입 매개변수만 사용하게 수정하면 된다.  
**타입 매개변수 목록은 메서드의 제한자(_public static_)와 반환 타입(*Set*) 사이에 온다.** 
```java
// 변경 후 코드
public static <E> Set<E> union(Set<E> s1, Set<E> s2) {
    Set<E> result = new HashSet(s1);
    result.addAll(s2);
    return result;
}
```
변경 후의 코드는 3개의 집합이 모두 같은 타입이어야한다. 이는 한정적 와일드카드 타입(Item31)을 사용하여 더 유연하게 개선할 수 있다. 

## 제네릭 싱글턴 팩터리

불변 객체를 여러 타입으로 활용할 수 있게 만들어야 할 때가 있다. 제네릭은 하나의 객체를 어떤 타입으로든 매개변수화할 수 있지만, 이렇게 하려면
요청한 타입 매개변수에 맞게 매번 그 객체의 타입을 바꿔주는 정적 팩터리를 만들어야한다.  
이러한 패턴을 제네릭 싱글턴 팩터리라 하며, Collections.reverseOrder 같은 함수 객체나 Collections.emptySet같은 컬렉션용으로 사용한다. 

다음은 항등함수를 담은 클래스이다. (항등함수 : 자기 자신과 같은 값을 대응시키는 함수)
```java
private static UnaryOperator<Object> IDENTITY_FN = (t) -> t;

@SuppressWarnings("unchecked")
public static <T> UnaryOperator<T> identityFunction() {
    return (UnaryOperator<T>) IDENTITY_FN;
}
```
IDENTITY_FN을 UnaryOperator&#60;T&#62;로 형변환하면 비검사 형변환 경고가 발생한다. 왜냐하면 UnaryOperator&#60;Object&#62;는 UnaryOperator&#60;T&#62;가 아니기 때문이다.  
하지만 입력 값을 수정 없이 그대로 반호나하는 함수이기 때문에 T가 어떤 타입이든 UnaryOperator&#60;T&#62;를 사용해도 타입 안전하므로 @SuppressWarning 애너테이션을 추가한다. 

```java
// 제네릭 싱글턴의 사용 예시
public static void main(String[] args) {
    String[] strings = {"삼베", "대마", "나일론"};
    UnarayOperator<String> sameString = identityFunction();
    for (String s : strings) 
        System.out.println(sameString.apply(s));
    
    Number[] numbers = {1, 2.0, 3L};
    UnaryOperator<Number> sameNumber = identityFunction();
    for (Number n : numbers)
        System.out.println(sameNumber.apply(n));
}
```
