# Itme18. 상속보다는 컴포지션을 사용하라

_**이번 챕터에서의 '상속'은 인터페이스를 구현하거나 다른 인터페이스를 확장하는 '인터페이스상속'은 제외한다._**
## 다른 패키지의 클래스를 상속하는 것은 위험하다. 
상속은 캡슐화를 깨뜨린다. 매 릴리즈때마다 상위클래스에서 바뀌는 내부 구현으로 하위 클래스에서 오작동할 수 있다.  
따라서 이러한 부분은 상위 클래스 설계자가 확장을 충분히 고려하고 문서화도 제대로 해야한다. 
HashSet을 상속하는 다음 클래스를 한번 보자.

```java
import java.util.Collection;
import java.util.HashSet;

public class InstrumentedHashSet<E> extends HashSet<E> {
    private int addCount = 0;

    public InstrumentedHashSet() {
    }

    public InstrumentedHashSet(int initCap, float loadFactor) {
        super(initCap, loadFactor);
    }

    @Override
    public boolean add(E e) {
        addCount++; // -- (2)
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size(); // -- (1)
        return super.addAll(c);
    }

    public int getAddCount() {
        return addCount;
    }
}
```
이 클래스의 인스턴스에 addAll메서드로 원소 3개를 더할 경우 getAddCount메서드를 호출하면 3이 아닌 6을 반환한다.
그 이유는 HashSet의 addAll메서드가 add메서드를 사용해 구현되었기때문이다. 
```java
public boolean addAll(Collection<? extends E> c) {
    boolean modified = false;
    for (E e : c)
        if (add(e))
            modified = true;
    return modified;
}
```
따라서 이미 addCount가 (1)으로 추가되었지만 (2)에서 또 더해지기때문에 3이아닌 6이 반환된다. 

## 이러한 문제를 해결하기 위해서는?
1.하위 클래스에서 addAll 메서드를 재정의하지 않는다. (X)   
이는 addAll이 add를 이용해 구현함을 가정했기 때문에 가능한 해법이라는 한계를 갖는다. 
2.addAll메서드를 다른 식으로 재정의한다. (X)   
예를들어 컬렉션을 순회해 원소 하나당 add를 호출하는 방식으로 재정의한다. 하지만 이는 오류를 내거나 성능을 떨어뜨릴 수도 있다.

## 컴포지션(Composition:구성)
상속했을 때의 여러 문제를 피하기 위한 방법으로 기존의 클래스를 확장하는 것이 아니라 새로운 클래스를 만들고 private필드로 기존 클래스의 
인스턴스를 참조하는 방법이다.  
새 클래스의 인스턴스 메서드들은 기존 클래스의 대응하는 매서드를 호출하며 이 방식을 전달이라고하고 새 클래스의 메서드들을 전달 메서드라 부른다.  

```java
public class InstrumentedSet<E> extends ForwardingSet<E> {
    // ...
}

public class ForwardingSet<E> implements Set<E> {
    private final Set<E> e;
    public ForwardingSet(Set<E> s) { this.s = s; }
    
    public void clear() { s.clear(); }
    // ...
}
```

InstrumentedSet과 같이 다른 인스턴스를 감싸고 있는 클래스를 래퍼 클래스라고 하고. 다른 Set에 계측 기능을 덧씌운다는 뜻에서
데코레이터 패턴이라고도 한다. 컴포지션과 전달의 조합은 (래퍼 객체가 내부 객체에 자기 자신의 참조를 넘기는 경우에만) 위임이라고도한다. 

## 래퍼클래스
래퍼 클래스가 콜백 프레임워크와는 어울리지 않는다. 콜백 프레임워크는 자기 자신의 참조를 다른 객체에 넘겨 다음 호출때 사용하도록 한다. 
내부 객체는 자신을 감싸는 래퍼 존재를 몰라 자신(this)의 참조를 넘기고, 콜백 때는 래퍼가 아닌 내부 객체를 호출한다. 
이를 SFLF 문제라고 한다. 

## 상속하기 위해서는
상속은 반드시 하위 클래스가 상위 클래스와 is-a 관계일 때만 해야한다. 그러한 관계가 아니라면 상위클래스를 private인스턴스로 두고, 
다른 API를 제공해야한다.  
컴포지션을 사용해야하는 상황에서 상속을 사용하면, API가 내부 구현에 묶이고 클래스의 성능이 제한된다는 점과 클라이언트가 노출된 내부에
직접 접근이 가능해 수정할 수 있다는 것이다. 이는 하위 클래스의 불변식을 해칠 수 있다. 