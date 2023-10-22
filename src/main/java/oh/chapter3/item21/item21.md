# Itme21. 인터페이스는 구현하는 쪽을 생각해 설계하라

## 디폴트 메서드 
디폴트 메서드가 나오기 전에는 인터페이스에 메서드를 추가하면 기존 구현체에 해당 메서드가 우연히 있을 가능성이 매우 낮기에 높은 확률로 컴파일 오류가 발생한다.  
따라서 기존 인터페이스에 새로운 메서드를 추가하기는 쉽지 않았는데 이러한 것을 가능하게 한 것이 디폴트 메서드이다. 
하지만 위험성이 완전히 사라진 것은 아니다.  

디폴트 메서드를 선언하면 기존 구현 클래스에서 재정의하지 않는 한 디폴트 구현이 쓰인다. 하지만 기존 구현체와 매끄럽게 연동될거란 보장이 없다.
즉, 생각할 수 있는 모든 상황에서 불변식을 해치지 않는 디폴트 메서드를 작성하기란 어렵다.  
```java
default boolean removeIf(Predicate<? super E> filter) {
    Objects.requireNonNull(filter);
    boolean result = false;
    for (Iterator<E> it = iterator(); it.hasNext();) {
        if (filter.test(it.next())) {
            it.remove();
            result = true;
        }
    }
    return result;
}
```
이 메서드는 true를 반환하는 모든 원소를 제거하는 함수로 디폴트 구현은 반복자를 이용해 순회하면서 각 원소를 인수로 넣어 프레디키트를 호출하고 
프레디키트가 true를 반환하면 반복자의 remove를 호출해 원소를 제거한다.  
아파치의 SynchronizedCollection클래스는 클라이언트가 제공한 객체로 각을 거는 능력이 추가로 제공되는데, removeIf를 재정의하지않아 디폴트 구현을 물려받는다. 
따라서 디폴트 구현의 removeIf는 동기화에 관해 아무것도 모르므로 락 객체를 사용할 수 없어 이를 호출하면 ConcurrentModificationException이 발생하거나 다른 결과를 낳는다. 

이에 자바 플랫폼 라이브러리에서는 이러한 문제에 대한 조치를 취했는데, Collections.synchronizedCollection이 반환하는 package-private클래스들은
removeIf를 재정의하고, 이를 호출하는 다른 메서드들은 디폴트 구현을 호출하기 전에 동기화를 한다. 

디폴트 메서드는 기존 구현체에 런타임 오류를 일으킬 수 있다. 디폴트 메서드를 새로 추가할 때는 기존 구현체들과의 충돌 여부를 심사숙고해야 한다.

디폴트 메서드는 인터페이스로부터 메서드를 제거하거나 기존 메서드의 시그니처를 수정하는 용도가 아니다. 