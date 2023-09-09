# Itme10. equals는 일반 규약을 지켜 재정의하라  


## equals는 재정의하지 말아라
________________________
다음은 equals를 재정의하지 않는 것이 최선인 상황이다.
- 각 인스턴스가 본질적으로 고유하다
- 인스턴스의 '논리적 동치성'을 검사할 일이 없다
- 상위 클래스에서 재정의한 equals가 하위 클래스에도 딱 들어맞는다
- 클래스가 private이거나 package-private이고 equals 메서드를 호출할 일이 없다.

> **논리적 동치란?**  
> 문장 P와 Q가 있을 때, 두 문장이 서로의 논리적인 결과로 이끌어질 때 논리적 동치라고 한다  
>  *내가 생각했을 때는 일반적으로는 값이 같은 두 객체라고 보면 될 것같다.*

## 그럼에도 equals를 재정의해야 할 때는 언제인가
______________________________________
논리적 동치성을 확인해야 하는데 상위 클래스의 equals에 논리적 동치성을 비교하도록 안되어 있을 때이다. 
일반적으로 '값 클래스'들이 해당된다.
예를 들어 String a 와 String b에는 각각 '사과'라는 값이 있다. 이때 프로그래머는 equals를 통해 a와 b가 같은 객체인지 비교하기보다 
각 객체가 가지고 있는 '사과'라는 값이 같은지에 더 관심이 있을 것이다.  
단, 값 클래스여도 값이 같은 인스턴스가 여러개 만들어지지 않는 상황을 보장하는 인스턴스 통제 클래스(item01-추후 추가 예정)라면 equals를 재정의 하지 않아도 된다.


## equals 메서드를 재정의할 때는 반드시 다음의 5가지 규약을 따라야한다
______________________________________________________
1. 반사성 : null이 아닌 모든 참조 값 x에 대해, x.equals(x)는 true이다.
2. 대칭성 : null이 아닌 모든 참조 값 x, y에 대해, x.equals(y)가 true이면 y.equals(x)도 true이다.
3. 추이성 : null이 아닌 모든 참조 값 x, y, z에 대해, x.equals(y)가 true이고 y.equals(z)가 true이면 x.equals(z)도 true이다.
4. 일관성 : null이 아닌 모든 참조 값 x, y에 대해, x.equals(y)를 반복해서 호출해도 항상 true를 반환하거나 항상 false를 반환한다.
5. null-이 아님 : null이 아닌 모든 참조 값 x에 대해, x.equals(null)은 false이다.

### 반사성
반사성은 단순히 말하면 객체는 자기 자신과 같아야 한다는 뜻이다. 
즉 반사성을 충족하지 못하는 인스턴스를 컬렉션에 넣고 contains 메서드를 호출하면 방금 넣은 인스턴스가 없다고 할 것이다.  

### 대칭성
대칭성은 두 객체가 서로에 대한 동치 여부를 똑같이 해야 한다는 것이다.

```java
import java.util.Objects;

public final class CaseInsensitiveString {
    
    public CaseInsensitiveString(String s) {
        this.s = Objects.requireNonNull(s);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CaseInsensitiveString)
            return s.equalsIgnoreCase(((CaseInsensitiveString) o).s);
        if (o instanceof String) 
            return s.equalsIgnoreCase((String) o);
        return false;
    }
}
```
위와 같은 클래스가 있다고 하자. CaseInsensitiveString 객체로 "Apple"과 "apple"로 하나씩 만들고 두 객체를 equals를 비교하면 어떻게 될까?
```java
    CaseInsensitiveString s1 = new CaseInsensitiveString("Apple");
    String s2 = "apple";
    
    s1.equals(s2); // -- (1)
    s2.equals(s1); // -- (2)
```
(1)의 경우 true를 반환하지만 (2)의 경우 false를 반환한다. 왜냐하면 (1)의 경우는 CaseInsensitiveString 객체의 equals()를 호출했다면,
(2)의 경우 String의 equals()가 호출되어 false를 반환해버린다.  
**따라서 위의 예제는 대칭성을 위반했으므로 잘못된 equals이다.**

CaseInsensitiveString의 equals()는 다음과 같이 재정의한다.
```java
    @Override
    public boolean equals(Object o) {
            return o instanceof CaseInsensitiveString && 
            ((CaseInsensitiveString) o).s.equalsIgnoreCase(s);
    }
```

### 추이성
추이성이란 객체 a와 객체 b가 같고, b와 객체 c가 같으면 a와 c는 같다는 것이다. 추이성은 하위 클래스에서 새로운 필드를 추가된 상황에서 어기기 쉽다.

```java
public class Point {
    private final int x;
    private final int y;

    pulbic Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point)) {
            return false;
        }
        Point p = (Point) o;
        return (p.x == x && p.y == y);
    }
}

public class ColorPoint extends Point {
    private final Color color;

    public ColorPoint(int x, int y, Color color) {
        super(x, y);
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Point)){
            return false;
        }
        // o가 Point객체일 때는 x, y 만 비교한다.
        if(!(o instanceof ColorPoint)) { // -- (1)
            return o.equals(this);
        }
        // o가 ColorPoint객체이면 색상도 비교한다.
        return super.equals(o) && ((ColorPoint) o).color == color;
    }
}
```
위 코드는 하나의 점을 나타내는 Point라는 클래스와 이를 확장해 color라는 필드를 추가한 ColorPoint클래스이다.

```java
    ColorPoint p1 = new ColorPoint(1, 2, Color.RED);
    Point p2 = new Point(1, 2);
    ColorPoint p3 = new ColorPoint(1,2, Color.BLUE);
```
위의 p1, p2, p3 세 인스턴스를 equals로 비교하면, 위의 equals는 추이성에 위배되는 재정의임을 알 수 있다.
p1과 p2 / p2와 p3 는 true를 반환하지만 p1과 p3는 색상이 달라 false를 반환하기 때문이다.
특히, Point를 확장시킨 ColorPoint외에 SmellPoint 클래스 객체를 더 만들어 ColorPoint와 SmellPoint를 equals비교하면
무한 재귀에 빠져 StackOverflowError가 발생한다.  
*왜냐하면 (1)의 비교문을 통해 서로의 equals를 계속적으로 호출하기때문이다.*  

실제 자바 라이브러리에도 이러한 문제를 갖고있는 클래스가 종종 있다. java.sql.Timestamp는 java.util.Date를 확장한 후 nanoseconds 필드를 추가했다.
그 결과 Timestamp의 equals는 대칭성을 위배해 Date객체와 한 컬렉션에 넣거나 섞어 사용하면 엉뚱하게 동작할 수 있다.


> **사실 구체 클래스를 확장해 새로운 값을 추가하면서 equals 규약을 만족시킬 방법은 존재하지않는다.**

_equals안에 instanceof검사를 getClass 검사로 바꾼다면?_  
A의 하위 클래스는 정의상 여전히 A이므로 어디서든 A로써 활용될 수 있어야하는 **리스코프 치환 원칙**에 위배가 된다.
즉, getClass로 비교하게 되면 Point를 확장시킨 ColorPoint클래스의 인스턴스는 어떤 Point와도 같을 수 없다.

**ITEM18을 이용하자!(추후 링크 추가예정)**  
Point를 상속하는 것이 아니라 ColorPoint의 private 필드를 두고 ColorPoint와 같은 위치의 일반 Point를 반환하는 뷰(view)메서드를 public으로 추가하는 것이다.
즉 다음과 같이 ColorPoint를 정의하는 것이다.
```java
public class ColorPoint {
    private final Point point;
    private final Color color;

    public ColorPoint(int x, int y, Color color) {
        point = new Point(x, y);
        this.color = color;
    }

    public Point asPoint() {
        return point;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ColorPoint)) {
            return false;
        }
        ColorPoint cp = (ColorPoint) o;
        return cp.point.equals(point) && cp.color.equals(color);
    }
}
```

**추상클래스의 하위클래스라면 걱정하지 않아도된다!**  
추상클래스의 하위 클래스라면 equals의 규약을 지키면서도 필드를 추가할 수 있다. 추상클래스처럼 직접 인스턴스로 만드는게 불가능하면 지금까지의 문제는 일어나지않는다.
[item23 참조]


### 일관성
일관성은 두 객체가 같다면 (수정하지 않는 한) 앞으로도 영원히 같아야 한다는 뜻이다. 즉, equals의 판단에 신뢰할 수 없는 자원이 끼어들게 해서는 안된다.

java.net.URL의 equals는 주어진 URL과 매핑된 호스트의 IP 주소를 이용해 비교한다. 이때 호스트의 이름을 IP주소로 바꾸려면 네트워크를 통해야하는데, 
그 결과가 항상 같다고 보장할 수 없다. 따라서 equals는 항시 메모리에 존재하는 객체만을 사용한 결정적 계산만 수행해야한다.


### null-아님
null-아님은 모든 객체가 null과 같지 않아야 한다는 뜻이다. 즉, o.equals(null) 의 반환값이 true가 되면 안된다는 것이다. 더불어 NullPointerException조차 허용하지 않는다.
이를 위해 null인 경우 return false를 하는 것이 아니라 전달받은 객체를 instanceof 연산자로 해당 객체가 올바른 타입인지 검사하고 아니면 false를 반환하는 편이 더 좋다.


## equals 메서드의 구현 방법은 다음과 같다.
________________________
### 1. == 연산자를 사용해 입력이 자기 자신의 참조인지 확인한다.
자기 자신이면 true를 반환한다. 이는 단순한 성능 최적화용이다.  

### 2. instanceof 연산자로 입력이 올바른 타입인지 확인한다.
올바른 타입은 일반적으로 equals가 정의된 클래스이지만 때로는 그 클래스가 구현한 특정 인터페이스가 될 수도 있다. 
예를 들어 Set, List, Map 등의 컬렉션 인터페이스들은 자신을 구현한 서로 다른 클래스끼리 비교할 수 있도록 equals를 수정했다.

### 3. 입력을 올바른 타입으로 형변환한다.
2번에서 instanceof 검사를 했기 때문에 이 단계는 100% 성공한다.

### 4. 입력 객체와 자기 자신의 대응되는 '핵심'필드들이 모두 일치하는지 하나씩 검사한다.
핵심필드들이 모두 일치해야 true를 반환하고 하나라도 다르면 false를 반환해야한다. 이때 2단계에서 인터페이스를 사용했다면,
입력의 필드 값을 가져올 때도 그 인터페이스의 메서드를 사용해야한다.
또한 타입이 클래스라면 접근 권한에 따라 직접 필드에 접근할 수도 있다.
- float, double을 제외한 기본 타입 필드는 == 연산자를 사용한다. 
- 참조 타입필드는 각각의 equals메소드를 사용한다.
- float와 double은 Float.compare(float, float)와 Double.compare(double, double)로 비교한다.  
float와 double은 NaN, -0.0f, 특수한 부동 소수 값 등을 다뤄야하기 때문이다.  
  (_이때 Float.equals와 Double.equals는 오토박싱을 수반할 수 있어 성능상 좋지 않다._)  
`오토박싱이란? JDK1.5부터 박싱과 언박싱에대해 컴파일러가 자동으로 처리해준다(박싱:기본타입->참조타입 / 언박싱:참조타입->기본타입)`
- 배열 필드는 원소 각각을 앞선의 지침대로 비교하되, 배열의 모든 원소가 핵심필드라면 Arrays.equals메서드들 중 하나를 사용하자.
- null도 정상 값으로 취급하는 참조 타입 필드의 경우 Objects.equals(Object, Object)로 비교한다.
NullPointerException을 예방할 수 있다.
- 비교하기 복잡한 필드는 표준형을 저장한 후 표준형끼리 비교하는 것이 훨씬 경제적이다. (=> ? 예시가 있을까.. 감이 잘 오지 않는다.)  
*[표준형과 관련된 링크를 추가한다.(2023.09.09)](https://stackoverflow.com/questions/280107/what-does-the-term-canonical-form-or-canonical-representation-in-java-mean)*
- 필드를 비교할 때는 다를 가능성이 크거나 비교 비용이 싼 필드를 먼저 비교하자. 


## equals 메서드를 구현할 때, 마지막 주의사항이다.
________________________
equals를 재정의할 땐 hashCode도 반드시 재정의하자.
너무 복잡하게 해결하지 말자
Object 이외의 타입을 매개변수로 받는 equals는 선언하지 말자.


## AutoValue 프레임워크?
________________________
구글이 만든 프레임워크로 클래스에 어노테이션을 추가하면 AutoValue가 알아서 equals 메서드의 필드들을 비교하는 테스트 코드를 작성해줄 것이다.