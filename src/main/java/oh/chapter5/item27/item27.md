# Itme27. 비검사 경고를 제거하라

## 제네릭을 사용할 땐, 수많은 컴파일러 경고가 발생한다. 

비검사형변환 경고, 비검사 메서드 호출 경고, 비검사 매개변수화 가변인수 타입 경고, 비검사 변환 경고 등이 있다. 

## 비검사 경고를 제거하면 타입 안전성이 보장된다.

**경고를 제거할 수는 없지만 타입 안전하다고 확신할 수 있다면 @SuppressWarnings("unchecked")를 달아 경고를 숨긴다.**  
단, 타입이 안전함을 검증하지 않은 채 경고를 숨기면 경고 없이 컴파일되겠지만, 런타임에서는 ClassCastException을 던진다. 반대로, 안전하다고
검증된 비검사 경고를 그대로 두면, 진짜 문제를 알리는 새로운 경고가 나와도 눈치채지 못할 수 있다.   
@SuppressWarnings 애너테이션은 개별 기역변수부터 클래스 전체까지 어떤 선언에도 달 수 있지만, 가능한 좁은 범위에 적용하도록 한다. 왜냐하면 자칫 심각한 
경고를 놓칠 수 있기 때문이다. 따라서 한 줄이 넘는 메서드나 생성자에 달린 애너테이션은 지역변수 선언 쪽으로 옮기도록 한다. 

```java
public <T> T[] toArray(T[] a) {
    if (a.length < size)
        return (T[]) Arrays.copyOf(elements, size, a.getClass()); // --- (1)
    System.arraycopy(elements, 0, a, 0, size);
    if (a.length > size) 
        a[size] = null;
    return a;
}
```
toArray를 컴파일하면 (1)에서 경고가 발생한다. 이때 이를 위해 애너테이션을 달아야하는 데, 애너테이션은 선언에만 달 수 있기 때문에 return문에는 불가능하다. 
이때 메서드 전체에 달게 되면 필요 이상으로 범위가 넓어지니, 반환값을 담을 지역변수를 하나 선언하고 그 변수에 애너테이션을 달도록 한다. 

```java
public <T> T[] toArray(T[] a) {
    if (a.length < size){
        @SuppressWarnings("unchecked") 
        T[]result=(T[])Arrays.copyOf(elements,size,a.getClass());
        return result;
    }
    System.arraycopy(elements, 0, a, 0, size);
    if (a.length > size) 
        a[size] = null;
    return a;
}
```

@SuppressWarnings 애너테이션을 사용할 땐 그 경고를 무시해도 안전한 이유를 주석으로 남겨야 한다. 그래야 다른 사람의 코드 이해도를 높이고, 또 다른 사람이 코드를 
잘못 수정해 타입 안정성을 잃는 상황을 줄여줄 수 있다. 
