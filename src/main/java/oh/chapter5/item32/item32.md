# Item32. 제네릭과 가변인수를 함께 쓸 때는 신중하라. 

## 가변인수에 제네릭이나 매개변수화 타입이 포함되었다. 
가변인수는 메서드에 넘기는 인수의 개수를 클라이언트가 조절할 수 있게 한다.   
하지만, 가변인수 메서드를 호출하면 가변인수를 담기 위한 배열이 자동으로 하나 만들어지면서 클라이언트에 노출된다. 
그 결과 varargs매개변수에 제네릭이나 매개변수화 타입이 포함되면 컴파일 경고가 발생한다.  

## 힙오염
매개변수화 타입의 변수가 타입이 다른 객체를 참조하면 힙 오염이 발생한다. 
```java
static void dangerous(List<String>... stringLists) {
    List<Integer> intList = List.of(42);    
    Object[] objects = stringLists;
    objects[0] = intList; // 힙 오염 발생
    String s = stringLists[0].get(0); // ClassCastException
}
```
이렇게 타입안전성이 깨지니 제네릭 varargs 배열 매개변수에 값을 저장하는 것은 안전하지 않다.

## @SafeVarargs

### 모순을 허용한다

제네릭 배열을 직접 생성하는 건([item28](https://github.com/jiyoung0340/effective-java-study/blob/master/src/main/java/oh/chapter5/item28/item28.md#%EC%A0%9C%EB%84%A4%EB%A6%AD-%EB%B0%B0%EC%97%B4%EC%9D%80-%ED%83%80%EC%9E%85%EC%9D%B4-%EC%95%88%EC%A0%84%ED%95%98%EC%A7%80-%EC%95%8A%EB%8B%A4)) 
허용하지 않지만 제네릭 varargs 매개변수를 받는 메서드를 선언할 수 있게한 것은 제네릭이나 매개변수화 타입의 varargs매개변수를 받는 메서드가 실무에서 매우 유용하기 때문이다.   
실제로 자바 라이브러리에서도 Arrays.asList(T... a), Collections.addAll(Collection&#60;? super T&#62; c, T... elements), EnumSet.of(E first, E... rest)등을 제공한다.  
하지만 이러한 메소드들은 타입 안전하다. 

### 해결방법
자바 7전에는 메서드의 작성자가 호출자 쪽에서 발생하는 경고에 대해 할 수 있는 일이 없었는데 자바 7버전 이후에는 @SafeVaras애너테이션이 추가되며 
작성자가 그 메서드가 타입안전함을 보장하는 장치로 사용한다. 물론 메서드가 안전한 게 확실하지 않다면 절대 애너테이션을 달아선 안된다. 

## 메서드가 안전한지 어떻게 알까?
가변인수 메서드를 호출할 때 varargs매개변수를 담는 제네릭 배열이 만들어진다. 따라서 메서드가 이 배열에 아무것도 저장하지 않고(=그 매개변수들을 덮어쓰지 않고), 
참조가 밖으로 노출되지 않고(=신뢰할 수 없는 코드가 배열에 접근할 수 없고) 순수하게 인수들을 전달하기만 한다면 타입 안전하다.

### varargs 매개변수 배열에 아무것도 저장하지 않고도 타입 안정성을 깰수도 있음을 주의해야한다.

```java
static <T> T[] toArray(T... args) { return args; }
```
이 메서드가 반환하는 배열의 타입은 이 메서드에 인수를 넘기는 컴파일 타임에 결정되는데, 그 시점에는 컴파일러에게 충분한 정보가 주어지지 않아 타입을 잘못 판단할 수 있다. 
따라서 자신의 varargs매개변수 배열을 그대로 반환하면 힙 오염을 이 메서드를 호출한 쪽의 콜스택으로까지 전이할 가능성이 있다. 

```java
static <T> T[] pickTwo(T a, T b, T c) {
    switch(ThreadLocalRandom.current().nextInt(3)) {
        case 0: return toArray(a, b); 
        case 1: return toArray(a, c);
        case 2: return toArray(b, c); // toArray는 Object[]를 반환 따라서 pickTwo도 Object[]를 반환
    }
    throw new AssertionError(); // 도달할 수 없다. 
}

public static void main(String[] args){
    String[] attributes = pickTwo("좋은", "빠른", "저렴한"); // (String[])pickTwo 불가능
}
```
이 메서드의 컴파일러는 toArray에 넘길 T 인스턴스 2개를 담을 varargs 매개변수 배열을 만드는 코드를 생성하는데 이 배열의 타입은 Object[]이다.  
pickTwo에 어떤 타입의 객체를 넘기더라도 담을 수 있는 가장 구체적인 타입이기 때문이다. 그러므로 pickTwo는 항상 Object[]타입을 반환한다. 

컴파일은 문제가 없지만 main메서드를 실행하는 순간 ClassCastException을 던진다. 왜냐하면 pickTwo의 반환값을 attribute에 저장하기 위해 
String[]로 형변환하는 코드를 컴파일러가 자동생성한다는 것을 놓쳤기 때문이다. Object[]는 String[]의 하위 타입이 아니기 때문이다.

제네릭 varargs 매개변수 배열에 다른 메서드가 접근하도록 허용하면 안전하지 않다!

### varargs를 안전하게 사용하는 상황

- @SafeVarargs로 제대로 애노테이트된 또 다른 varargs메서드에 넘기는 것은 안전하다
- 이 배열 내용의 일부 함수를 호출만 하는 (varargs를 받지 않는) 일반 메서드에 넘기는 것도 안전하다.

EX
```java
@SafeVarargs
static <T> List<T> flatten(List<? extends T>... lists) {
    List<T> result = new ArrayList<>();
    for (List<? extends T> list : lists)
        result.addAll(list);
    return result;
}
```

## @SafeVarargs 애너테이션 사용 규칙

### 제네릭이나 매개변수화 타입의 varargs 매개변수를 받는 모든 메서드에 @SafeVarargs를 달아라.

그러면 사용자를 헷갈리게 하는 컴파일러 경고를 없앨 수 있다. 즉 안전하지 않은 varargs는 작성하면 안된다.  
또, 통제가능한 메서드 중 제네릭 varargs 매개변수를 사용하며 힙 오염 경고가 뜨는 메서드가 있다면, 그 메서드가 진짜 안전한지 점검해야한다.

@SafeVarargs 애너테이션은 재정의를 할 수 없는 메서드에만 달아야한다. 재정의한 메서드가 안전할지 보장할 수 없기때문이다. 따라서 자바8에서는
정적 메서드와 final 인스턴스 메서드에만, 자바9부터는 private 인스턴스 메서드까지 허용가능하다. 


## varargs 매개변수를 List 매개변수로
@SafeVarargs 애너테이션만이 유일한 정답이 아니다. varargs매개변수를 List매개변수로 바꿀 수 있다([Item28](https://github.com/jiyoung0340/effective-java-study/blob/master/src/main/java/oh/chapter5/item28/item28.md#itme28-%EB%B0%B0%EC%97%B4%EB%B3%B4%EB%8B%A4%EB%8A%94-%EB%A6%AC%EC%8A%A4%ED%8A%B8%EB%A5%BC-%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC))

```java
static <T> List<T> flatten(List<List<? extends T>> lists) {
    List<T> result = new ArrayList<>();
    for (List<? extends T> list : lists) 
        result.addAll(list);
    return result;
}
```
이때 정적 팩터리 메서드인 List.of를 활용하면 
```java
audience = flatten(List.of(friends, romans, countrymen));
```
과 같이 인수를 임의의 갯수로 넘길 수 있다. 왜냐하면 List.of에도 @SafeVarargs 애너테이션이 달려있기 때문이다. 

이 방식의 장점은 컴파일러가 이 메서드의 타입 안전성을 검증할 수 있다는 데 있다. 따라서 애너테이션을 직접 달지 않아도 되고, 안전성 판단을 하지 않아도 된다.   
단, 클라이언트 코드가 지저분해지고 속도가 느려질 수 있다. 

toArray처럼 varargs 메서드를 안전하게 작성하기 불가능한 상황에서도 이 방식은 사용가능하다. toArray의 List버전이 List.of이다. 따라서 이 방식을 pickTwo에 적용하면 다음과 같다.

```java
static <T> List<T> pickTwo(T a, T b, T c) {
        switch(ThreadLocalRandom.current().nextInt(3)) {
        case 0: return List.of(a, b);
        case 1: return List.of(a, c);
        case 2: return List.of(b, c);
        }
        throw new AssertionError(); // 도달할 수 없다. 
}

public static void main(String[] args) {
    List<String> attributes = pickTwo("좋은", "빠른", "저렴한");
}
```


