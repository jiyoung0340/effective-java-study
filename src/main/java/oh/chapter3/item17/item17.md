# Itme17. 변경 가능성을 최소화하라

## 불변클래스
불변클래스란 그 인스턴스의 내부 값을 수정할 수 없는 클래스이다. 따라서 불변클래스의 인스턴스에 저장된 정보는 그 객체가 파괴되는 순간까지 절대 달라지지않는다.   
ex. String, 기본 타입의 박싱된 클래스(Integer, Long ...), BigInteger, BigDecimal   
불면클래스의 장점은 가변클래스보다 설계, 구현, 사용이 쉽고, 오류가 생길 여지가 적어 안전하다. 

**<불변클래스를 만드는 규칙>**  
- 객체의 상태를 변경하는 메서드(변경자)를 제공하지 않는다.  
- 클래스를 확장할 수 없도록 한다.   
  하위클래스에서 객체의 상태를 변하게 만드는 사태를 막아준다(클래스를 final로 선언하거나 추후 다른 방법이 더 있다.)
- 모든 필드를 final로 선언한다.  
  설계자의 의도를 명확히 드러내기 위함으로 새로 생성된 인스턴스를 동기화 없이 다른 스레드로 건네도 문제없이 동작하게끔 보장하는 데도 필요하다.
- 모든 필드를 private으로 선언한다.  
  클라이언트에서 직접 필드가 참조하는 가변 객체를 수정하는 일을 막는다. 기본 타입 필드나 불변 객체를 참조하는 필드는 public final로 선언해도
  불변 객체가 되지만 이러면 내부 표현을 바꾸지 못하기때문에 권하는 방법이 아니다.
- 자신 외에는 내부의 가변 컴포넌트에 접근할 수 없도록 한다.
  클래스에 가변 객체를 참조하는 필드가 하나라도 있다면 클라이언트에서 그 객체의 참조를 얻을 수 없도록 해야 하고, 접근자 메서드가 그 필드를 그대로 반환해서도 안된다.
  따라서 생성자, 접근자, readObject메서드 모두에서 방어적 복사를 수행해야한다.

```java
public final class Complex {
    private final double re;
    private final double im;

    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public double realPart() {
        return re;
    }

    public double imaginaryPart() {
        return im;
    }

    public Complex plus(Complex c) {
        return new Complex(re + c.re, im + c.im); // --(1)
    }
    
    // ...
    
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Complex))
            return false;  
        Complex c = (Complex) o;
        return Double.compare(c.re, re) == 0 && Double.compare(c.im, im) == 0;  
    }

    @Override
    public int hashCode() {
        return 31*Double.hashCode(re)+Double.hashCode(im);
    }

    // ...
}
```
(1) : 사칙연산 메서드들이 인스턴스 자신을 수정하는 것이 아니라 새로운 Complex연산자를 만들어 반환한다.

> 함수형 프로그래밍 : 피연산자와 함수를 적용해 그 결과를 반환하지만 피연사 자체는 그대로인 프로그래밍 패턴(plus)  
> 절차형(명령형) 프로그래밍 : 피연산자인 자신을 수정해 자신의 상태가 변하는 프로그래밍 패턴(add)  

함수형 프로그래밍은 불변 영역의 비율이 높아질수록 장점을 누릴수 있다.  
첫번째로 불변 객체는 단순하다. 처음 생성될 때의 상태를 파괴될 때까지 그래도 간직한다. 따라서 모든 생성자가 클래스 불변식을 보장하면 
사용되는 모든 클래스는 영원히 불변으로 남는다.   
두번째로 불변 객체는 근본적으로 스레드 안전해 동기화가 필요없다. 불변객체는 여러 스레드에서 동시에 사용하더라도 절대 훼손되지않아 안심하고 공유할 수 있다.  
따라서 불변 객체의 인스턴스는 최대한 재활용하는 것이 좋다. 

## 불변클래스 재활용하기
1.자주 쓰이는 값들은 상수로 제공한다. 
```java
public static final Complex ZERO = new Complex(0, 0);
public static final Complex ONE = new Complex(1, 0);
public static final Complex I = new Complex(0, 1);
```

2.자주 사용되는 인스턴스를 캐싱하여 같은 인스턴스를 중복 생성하지 않게 해주는 정적 팩터리를 제공한다.  
이 정적 팩토리를 사용하면 여러 클라이언트가 인스턴스를 공유해 메모리사용량과 가비지 컬렉션 비용이 줄어든다. 

## 불변클래스 특징
1.방어적 복사가 필요없다.  
불변 객체는 아무리 복사해도 원본과 똑같아 복사의 의미가 없다. 따라서 clone 메서드나 복사생성자를 제공하지 않는 것이 좋고,
String 객체의 경우 복사 생성자가 초창기에 만들어진 것으로 사용하지 않도록한다.  
2.불변 객체는 자유롭게 공유할 수 있고, 불변 객체끼리는 내부 데이터도 공유할 수 있다.  
BigInteger의 negate 메서드는 다음과 같다. 
```java
public class BigInteger extends Number implements Comparable<BigInteger> {
    final int signum; // 값의 부호를 저장
    final int[] mag; // 크기(절대값) 저장

    public BigInteger negate() {
        return new BigInteger(this.mag, -this.signum);
    }
}
```
negate함수는 크기값을 저장하는 mag가 가변이지만 이를 복사하지 않고 원본 인스턴스와 공유한다. 따라서 그 결과는 새로 만든 BigInteger인스턴스도
원본 인스턴스가 가리키는 내부 배열을 그대로 기리킨다. 

3.객체를 만들 때 다른 불변 객체들을 구성요소로 사용하면 이점이 많다.  
객체의 구조가 복잡하더라도 불변 객체로 이루어졌다면 그 불변식을 유지하기 수월하기 때문이다. 특히 불변 객체는 맵의 키, 집합의 원소로 쓰기 좋다.
맵이나 집합은 안에 담긴 값이 바뀌면 불변식이 허물어지는데 그러한 걱정을 하지 않아도 되기 때문이다. 

4.실패 원자성을 제공한다.  
_실패 원자성 : 메서드에서 예외가 발생한 후에도 그 객체는 여전히 메서드 호출 전과 똑같은 상태여야 한다._   
불변객체는 상태가 변하지 않으니 잠깐이라도 전, 후가 다를 가능성이 없다.  

### 불변클래스의 단점  
1.값이 다르면 반드시 독립된 객체로 만들어야한다.  
이때 그 값의 가짓수가 많다면 각 객체를 모두 만들어야해 큰 비용이 든다.   
불변 클래스 BigInteger와 가변 클래스 BitSet의 flip() 메서드는 큰 차이가 있다. 원본데이터가 백만 비트짜리일 경우 BigIntegerdml flip()은 단 한 비트를 
바꾸기위해 백만 비트의 새로운 인스턴스를 만들어야하지만, BitSet은 원하는 비트 하나만 바꿔준다.

2.원하는 객체를 완성하기까지의 단계가 많고, 중간 단계에서 만들어진 객체를 모두 버린다면 성능의 문제가 있다.   
이를 해결하기 위해서는 다단계 연산들을 예측해 기본 기능으로 제공한다. BigInteger는 모듈러 지수같은 다단계 연산 속도를 높여주는 가변 동반 클래스를 package-private으로 둔다.  
따라서 클라이언트가 원하는 복잡한 연산이 예측가능하다면 package-private의 가변 동반 클래스만으로 충분하다. 그렇지 않다면 클래스를 public으로 제공해야한다.  
대표적인 예로 String클래스이고 String의 가변 동반 클래스는 StringBuilder와 StringBuffer이다.  

## 불변 클래스를 상속하지 못하게 하는 방법
1. final클래스로 선언한다. 
2. 모든 생성자를 private이나 package-private으로 만들고 public 정적 팩토리를 제공한다.  
public이나 protected 생성자가 없으니 다른 패키지에서 이 클래스를 확장할 수 없기때문에 사실상 final이다.  
정적 팩토리 방식은 다수의 구현 클래스를 활용한 유연성을 제공하고 객체 캐싱 기능을 추가해 성능을 올릴 수도 있다.  
```text
BigInteger, BigDecimal의 설계 당시에는 불변 객체를 final로 사용해야한다는 생각이 널리 알려지기 전이었다. 
그래서 이 두 클래스의 메서드들은 모두 재정의할 수 있게되었고 이로인한 문제가 많다. 
따라서 클라이언트로부터 해당 타입의 인스턴스를 인수로 받으면 진짜 BigInteger/BigDecimal인지 확인해야하고,
신뢰할 수 없는 인스턴스라면 가변이라는 가정하에 방어적으로 복사해 사용해야 한다. 
```

## 모든 필드는 final이고 어떤 메서드도 그 객체를 수정할 수 없어야한다.   
이 규칙은 '어떤 메서드도 객체의 상태 중 외부에 비치는 값을 변경할 수 없다.'로 완화할 수 있다.  
어떤 불변 클래스는 계산 비용이 큰 값을 나중에 계산해 final이 아닌 필드에 캐시해 똑같은 값을 요청하면 해당 값을 반환해 계산 비용을 절감한다.  

