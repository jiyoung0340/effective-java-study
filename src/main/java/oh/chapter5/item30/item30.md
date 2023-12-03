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

## 재귀적 타입 한정

드물게, 자기 자신이 들어간 표현식을 사용하여 타입 매개변수의 허용 범위를 한정할 수 있는데 이를 재귀적 타입 한정이라한다.   
재귀적 타입 한정은 주로 타입의 자연적 순서를 정하는 Comparable 인터페이스와 함께 쓰인다. 

```java
public interface Comparable<T> {
	int compareTo(T o);
}
```

여기서 T는 Comparable&#60;T&#62;를 구현한 타입이 비교할 수 있는 원소의 타입을 정의한다. 실제로 거의 모든 타입은 자신과 같은 타입의 원소와만 비교할 수 있다. 
즉, String은 Comparable&#60;String&#62;을 구현하고 Integer는 Comparable&#60;Integer&#62;를 구현한다는 것이다. 

Comparable을 구현한 원소의 컬렉션을 입력받는 메서드들은 주로 정렬, 검색, 최댓/솟값을 구하는 식으로 사용되기때문에, 컬렉션에 담긴 모든 원소가 상호 비교될 수 있어야 한다. 

```java
public static <E extends Comparable<E>> E max(Collections<E> c);
```

&#60;E extends Comparable&#60;E&#62;&#62;는 "모든 타입 E는 자신과 비교할 수 있다"라는 뜻이다. 즉 상호 비교 가능하다는 것이다. 

```java
public static <E extends Comparable<E>> E max(Collections<E> c) {
	if (c.isEmpty())
		throw new IllegalArgumentException("컬렉션이 비어 있습니다");
		
	E result = null;
	for(E e : c)
		if(result == null || e.compareTo(result) > 0 )
			result = Objects.requireNonNull(e);
			
	return result;
}
```

재귀적 타입 한정은 훨씬 복잡해질 가능성이 있긴 하지만, 이번 아이템에 설명한 관용구, 여기에 와일드카드를 사용한 변형(item31)시뮬레이트한 셀프 타입 관용구(item2)를 
이해하고 나면 실전에서 마주치는 대부분의 재귀적 타입 한정을 무리 없이 다룰 수 있을 것이다. 
