# Itme20. 추상 클래스보다는 인터페이스를 우선하라

## 인터페이스 VS 추상클래스

인터페이스와 추상클래스는 자바가 제공하는 다중 구현 메커니즘으로 자바 8부터 인터페이스도 디폴트 메서드를 제공할 수 있어 둘 다 인스턴스 메서드를
구현 형태로 제공할 수 있다. 
```text
* default method란?
기존의 인터페이스의 경우 추상메서드가 추가된 경우 해당 인터페이스를 구현한 모든 구현체에 추가된 추상메서드를 정의해줘야한다. 이러한 것은
객체지향 설계 5대 원칙 중 하나인 OCP(Open Close Principle; 확장에는 열려 있고, 변경에는 닫혀라)에 위배되는데, 추가될 추상메서드를
default method로 추가할 경우 구현체에 추가하지 않아도 된다.
```

추상 클래스가 정의한 타입을 구현하는 클래스는 반드시 추상 클래스의 하위 클래스가 되어야한다는 것이다.  
자바는 단일 상속만 지원해 추상 클래스 방식은 새로운 타입을 정의하는 데 제약이 있다.  
반면 인터페이스가 선언한 메서드를 모두 정의하고 일반 규약을 잘 지킨 클래스는 다른 클래스를 상속했든 같은 타입으로 취급된다.  

또한 기존 클래스에도 손쉽게 새로운 인터페이스를 구현해넣을 수 있다. 인터페이스가 요구하는 메서드를 추가하고 implements구문을 추가하면 끝이다. 
반면 추상클래스는 계층구조상 확장하고자하는 클래스들의 공통 조상이어야하므로 클래스 계층구조에 혼란을 일으킨다.  

인터페이스는 믹스인(mixin) 정의에 알맞다. 믹스인은 클래스가 구현할 수 있는 타입으로, 믹스인을 구현한 클래스에 원래의 '주된 타입'외에 
특정 행위를 제공한다고 선언하는 효과를 준다. 반면 추상 클래스는 기존 클래스에 덧씌울 수 없고, 두 부모를 섬길수 없기 때문에 믹스인을 정의할 수 없다. 

인터페이스로는 계층구조가 없는 타입 프레임워크를 만들 수 있다. 예를 들어 가수 인터페이스와 작곡가 인터페이스가 있다고 할 경우, 작곡도 하는 가수를 표현하기 위해
두 인터페이스를 모두 구현해도 문제가 되지 않는다. 이러한 구조를 클래스로 만들려면 가능한 조합 전부를 각각의 클래스로 정의한 고도비만 계층 구조가 될 것이다. 

## 인터페이스 구현 방법
구현을 디폴트 메서드로 제공하고 이를 상송하려는 사람을 위한 설명을 @implSpec 자바독 태그를 붙여 문서화해야 한다. 
많은 인터페이스가 equals, hashCode같은 Object의 메서드를 정의하지만 이를 디폴트 메서드로 제공해서는 안된다. 
또한 인스턴스를 가질 수 없고, private 정적 메서드를 제외한 public이 아닌 정적 멤버도 가질 수 없다.

## 추상 골격 구현(skeletal implementation)
인터페이스와 추상 클래스의 장점을 모두 취하는 방법으로 인터페이스로는 타입을 정의하고 필요한 디폴트 메서드 몇 개도 함께 제공한다.  
그리고 골격 구현 클래스는 나머지 메서드들까지 구현한다. 이렇게 하면 단순히 골격 구현을 확장하는 것만으로 이 인터페이스를 구현하는데 필요한 일이 대부분 완료된다. 
이를 **템플릿 메서드 패턴** 이라고 한다.

```java
static List<Integer> intArrayAsList(int[] a) {
    Objects.requireNonNull(a);
    
    return new AbstractList<>() {
        @Override public Integer get(int i) {
            return a[i]
        }
        
        @Override public Integer set(int i, Integer val) {
            int oldVal = a[i];
            a[i] = val;
            return oldVal;
        }
        
        @Override public int size() {
            return a.length;
        }
    }
}
```
위 코드는 int배열을 받아 Integer 인스턴스의 리스트 형태로 보여주는 어댑터이다.  
골격 구현 클래스는 추상 클래스처럼 구현을 도와주는 동시에 추상 클래스로 타입을 정의할 때 따라오는 제약에서 자유롭다.  
인터페이스를 구현한 클래스에서 해당 골격 구현을 확장한 private 내부 클래스를 정의하고 각 메서드 호출을 내부 클래스의 인스턴스에 전달하며 우회적으로 골격 구현 클래스를 이용할 수 있다.  

## 골격 구현 작성
1. 인터페이스를 잘 살펴 다른 메서드들의 구현에 사용되는 기반 메서드를 선정한다.   
2. 기반 메서드를 사용해 직접 구현할 수 있는 메서드를 모두 디폴트 메서드로 제공한다.(단, equals, hashCode 같은 Object의 메서드는 제외)   
인터페이스의 모든 메서드가 기반 메서드와 디폴트 메서드가 된다면 별도의 골격 구현 클래스를 만들 이유는 없다.   
또한 기반/디폴트 메서드로 만들지 못한 메서드가 남아 있다면, 이 인터페이스를 구현하는 골격 구현 클래스를 하나 만들어 남은 메서드를 작성해 넣는다.

```java
import java.util.Map;
import java.util.Objects;

public abstract class AbstractMapEntry<K, V> implements Map.Entry<K, V> {
    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Map.Entry))
            return false;
        Map.Entry<?, ?> e = (Map.Entry) o;
        return Object.equals(e.getKey(), getKey())
                && Objects.equals(e.getValue(), getValue());
    }
    
    @Override public int hashCode() {
        return Object.hashCode(getKey()) ^ Object.hashCode(getValue());
    }
    
    @Override public String toString() {
        return getKey() + "=" + getValue();
    }
}
```
getKey, getValue는 확실한 기반 메서드이며 선택적으로 setValue도 포함할 수 있다. Object메서드들은 디폴드 메서드로 제공해서 안되므로 골격 구현 클래스에 
구현하고 toString도 기반 메서드를 사용해 구현했다. 

골격 구현은 기본적으로 상속해서 사용하는 걸 가정하므로 설계 및 문서화 지침을 따라야한다.

## 단순구현(simple inplements)
골격 구현의 작은 변종으로 상속을 위해 인터페이스를 구현한 것이지만, 추상 클래스가 아니다. 이러한 단수 ㄴ구현은 그래도 써도 되고 필요에 맞게 확장해도 된다.  
AbstractMap.SimpleEntry가 좋은 예이다. 

