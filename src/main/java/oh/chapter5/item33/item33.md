# Item33. 타입 안전 이종 컨테이너를 고려하라

## 타입 안전 이종 컨테이너 패턴

```text
* 컨테이너란? *
자바 컨테이너는 웹 애플리케이션을 실행하기 위해 필요한 런타임 환경을 제공하는 서버 사이드 구성요소이다. 
- 웹 컨테이너 : 자바 서블릿과 JSP들을 처리하는 데 사용된다. 웹 컨테이너는 클라이언트 요청을 받아 서블릿이나 JSP를 실행하고, 그 결과를 클라이언트에게 반환한다. ex. 톰캣
- EJB 컨테이너 : 엔터프라이즈 자바 빈을 처리하는데 사용되며, EJB컨테이너는 비즈니스 로직을 처리하고, 트랜잭션 관리, 보안, 원격 접근 등과 같은 서비스를 제공한다.
```
제네릭은 단일원소 컨테이너에도 흔히 쓰인다. 이런 모든 쓰임에서 매개변수화 되는 대상은 원소가 아닌 컨테이너 자신이다. 따라서 하나의 컨테이너에서 매개변수화할 수 있는 타입의 수가 제한된다. 
유연한 수단이 필요할 때는 컨테이너 대신 키를 매개변수화한 다음, 컨테이너에 값을 넣거나 뺄 때 매개변수화한 키를 함께 제공하면 된다. 이러한 설계 방식을 **타입 안전 이종 컨테이너 패턴**이라 한다.

```java
import java.util.HashMap;
import java.util.Objects;

public class Favorite {
    private Map<Class<?>, Object> favorite = new HashMap<>(); // --(1)

    public <T> void putFavorite(Class<T> type, T instance) { // type : 매개변수화한 키
        favorite.put(Objects.requireNonNull(type), type.cast(instance)); // -- (2)
    }
    public <T> T getFavorite(Class<T> type) {
        return type.cast(favorite.get(type));
    }
}

public class main {
    public static void main(String[] args) {
        Favorite f = new Favorite();

        f.putFavorite(String.class, "Java"); // class타입의 리터럴 타입은 Class가 아니라 Class<T>이다
        f.putFavorite(Integer.class, 0xcafebabe);
        f.putFavorite(Class.class, Favorite.class);

        String favoriteString = f.getFavorite(String.class);
        int favoriteInt = f.getFavorite(Integer.class);
        Class<?> favoriteClass = f.getFavorite(Class.class);

        system.out.printf("%s %x %s%n", favoriteString, favoriteInt, favoriteClass.getName()); // Java cafebabe Favorite
    }
}
```
**(1) 다양한 타입을 지원할 수 있는 이유.**  
private 맵 변수인 favorite은 Map&#60;Class&#60;?&#62;, Object&#62; 타입으로 비한정적 와일드카드타입이라 이 맵 안에 아무것도 넣을 수 없다고 생각할 수 있지만,
와일드카드 타입은 중첩되어있다. 즉, 맵이 와일드카드 타입이아니라 키가 와일드카드 타입인 것이다. 이는 모든 키가 서로 다른 매개변수화 타입일 수 있다는 뜻으로, 다양한 타입을 지원할 수 있다. 

또한, favorite의 값의 타입은 Object이다. 따라서 키와 값사이의 타입 관계를 보증하지 않는다. 즉, 값이 키에서 명시된 타입임을 보증하지 않는다.  
-->?어떤 이점이 있는건지 모르겠다.

**putFavorite**  
주어진 Class 객체와 즐겨찾기 인스턴스를 favorite에 추가해 관계를 짓기만 하면 된다. 키와 값 사이의 '타입 링크'정보는 버려진다. 타입링크란 그 값이 그 키 타입의
인스턴스라는 정보가 사라진다. 하지만 getFavorite메서드에서 관계를 되살릴 수 있다. 

**getFavorite**  
주어진 Class 객체에 해당하는 값을 favorite 맵에서 꺼낸다. 이때 그 객체는 Object타입으로 잘못된 컴파일타임 타입을 가지고 있다. 따라서 이를 T로 바꿔 변환해야한다.  
따라서 Class의 cast메서드를 사용해 이 객체 참조를 Class 객체가 가리키는 타입으로 동적 형변환한다. 

cast메서드는 주어진 인수가 Class객체가 알려주는 타입의 인스터스인지를 검사한 다음, 맞다면 그 인수를 그대로 반환하고, 아니면 ClassCastException을 던진다. 
클라이언트 코드가 컴파일 된다면 favorite 맵 안의 값은 항상 키의 타입과 일치하기때문에 getFavorite이 호출하는 cast는 ClassCastException을 던지지 않을 것이다. 

```java
public class Class<T> {
    T cast(Object object);
}
```
cast메서드는 Class 클래스가 제네릭이라는 점을 완벽히 활용했다. cast의 반환타입은 Class 객체의 타입 매개변수와 같다. 이는 T로 비검사 형변환을
손실 없이도 Favorite을 타입 안전하게 만드는 비결이다. 

## 제약사항

**1. Class객체를 제네릭이 아닌 로타입으로 넘기면 Favorite의 타입안정성이 깨진다.**  
하지만 이러한 코드는 컴파일시 비검사 경고가 뜰 것이다. 이는 HashSet과 HashMap에도 똑같은 문제가 있다. 
```java
HashSet<Integer> set = new HashSet<>();
((HashSet)set).add("String");
```
따라서 Favorite이 타입 불변식을 어기는 일이 없도록 보장하려면 putFavorite 메서드에서 (2)와 같이 동적 형변환을 사용해 instance타입이 type으로 명시한 타입과 같은지
확인하면 된다. 

이와 같은 방식을 사용한 컬렉션 래퍼는 java.util.Collections의 checkedSet, checkedList, checkedMap이 있다.
```java
// java.util.Collections
public static <E> Set<E> checkedSet(Set<E> s, Class<E> type) {
    return new CheckedSet<>(s, type);
}

public static <E> List<E> checkedList(List<E> list, Class<E> type) {
    return (list instanceof RandomAccess ?
            new CheckedRandomAccessList<>(list, type) :
            new CheckedList<>(list, type));
}

public static <K, V> Map<K, V> checkedMap(Map<K, V> m,
        Class<K> keyType,
        Class<V> valueType) {
    return new CheckedMap<>(m, keyType, valueType);
}
```
이 정적 팩터리들은 모두 제네릭이라 Class 객체와 컬렉션들을 실체화한다. 

**2. Favorite클래스는 실체화 불가 타입에는 사용 불가능하다.**  
따라서 String, String[]은 저장가능하지만 List&#60;String&#62;은 불가능하다. 왜냐하면 List&#60;String&#62;의 Class객체를 얻을 수 없기 때문이다. 
List&#60;String&#62; 또는 List&#60;Integer&#62; 는 List.class라는 Class객체를 공유하기때문에 둘을 허용해서 똑같은 객체 참조를
반환하면 객체 내부는 아수라장이 될 것이다. 

```java
// 해결방법 : 슈퍼 타입 토큰
Favorite f = new Favorite();
List<String> pets = Arrays.asList("dog", "cat", "turtle");

f.putFavorite(new TypeRef<List<String>>(){}, pets);
List<String> listofstrings = f.getFavorite(new typeRef<List<String>>(){});
```

## 한정적 타입 토큰 

Favorite가 사용하는 타입 토큰은 한정적이다. 즉, getFavorite과 putFavorite은 어떤 Class 객체든 받아들인다.  
허용 타입을 제한하고 싶을때는 한정적 타입 토큰을 활용한다. 한정적 타입 토큰이란 단순히 한정적 타입 매개변수나 한정적 와일드카드를 사용하여
표현 가능한 타입을 제한하는 타입 토큰이다.

```java
import java.lang.annotation.Annotation;

public<T extends Annotation> T getAnnotation(Class<T> annotationType);
```
한정적 타입 토큰을 사용한 예시이다. AnnotatedElement인터페이스에 선언된 메서드로 대상 요소에 달려 있는 애너테이션을 런타임에 읽어오는 기능이다. 
이때 annotationType 인수는 애너테이션 타입을 뜻하는 한정적 타입 토큰이다.  
따라서 토큰으로 명시한 타입의 애너케이션이 대상 요소에 달려 있다면 그 애너테이션을 반환, 없으면 null을 반환한다. 즉, 애너테이션된 요소는
그 키가 애너테이션 타입인 타입 안전 이종 컨테이너인 것이다. 

Class&#60;?&#62; 타입의 객체를 한정적 타입 토큰을 받는 메서드에 넘기려면 타입을 Class&#60;? extends Annotation&#62;로 형변환할 수 있지만
이는 비검사이므로 경고가 뜰것이다. 따라서 Class 클래스가 제공하는 인스턴스 메서드 **asSubclass** 를 사용한다.  
asSubclass는 호출된 인스턴스 자신의 Class객체를 인수가 명시한 클래스로 형변환한다. 실패하면 ClassCastException을 던진다. 

```java
static Annotation getAnnotation(AnnotatedElement element, String AnnotationTypeName) {
    Class<?> annotationType = null;
    try {
        annotationType = Class.forName(annotationTypeName);
    } catch (Exception e) {
        throw new IllegalArgumentException(ex);    
    }
    
    return element.getAnnotation(annotationType.asSubclass(Annotation.class));
}
```
이는 컴파일 시점에는 타입을 알수 없는 애너테이션을 asSubclass메서드를 사용해 런타임에 읽어내는 예시이다. 