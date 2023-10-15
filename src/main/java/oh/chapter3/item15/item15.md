# Itme15. 클래스와 멤버의 접근 권한을 최소화하라. 

## 캡슐화
잘 설계된 컴포넌트란 ***클래스 내부 데이터와 내부 구현 정보를 외부 컴포넌트로부터 잘 숨긴 컴포넌트***이다.
오직 API를 통해서만 다른 컴포넌트와 소통하고, 서로 내부 동작방식에는 전혀 개의치 않는다. 
이를 정보은닉, **캡슐화**라고 한다.

**캡슐화의 장점**
1. 여러 컴포넌트를 병렬로 개발할 수 있기 때문에 시스템 개발 속도를 높일 수 있다.
2. 각 컴포넌트를 빨리 파악해 디버깅할 수 있고, 다른 컴포넌트로 교체하는 부담이 적어 시스템 관리 비용을 낮출 수 있다.
3. 정보은닉 그 자체로 성능을 높여주는 것은 아니지만, 성능 최적화에 도움이 된다.  
   완성된 시스템을 프로파일링해 최적화할 컴포넌트를 정하고, 다른 컴포넌트에 영향을 주지 않고 해당 컴포넌트만 최적화할 수 있기 때문이다.
4. 소프트웨어의 재사용성을 높인다.   
    외부에 의존하지 않고 독자적으로 당작하는 컴포넌트라면 함께 개발되지 않은 낯선 환경에서도 유용하게 쓰일 가능성이 크다.
5. 큰 시스템을 제작하는 난이도를 낮춰준다.  
    시스템이 완성되지 않아도 개별 컴포넌트의 동작을 검증할 수 있기 때문이다.

## 접근 제어 메커니즘
정보 은닉을 위한 다양한 장치중 하나로 클래스, 인터페이스, 멤버의 접근성을 명시한다.  
각 요소의 접근성은 요소가 선언된 위치와 접근 제한자(private, protected, public)으로 정해진다.  
기본 원칙은 **모든 클래스와 멤버의 접근성을 가능한 한 좁혀야 한다.**

톱레벨(가장 바깥의) 클래스와 인터페이스에 부여할 수 있는 접근 수준은 package-private / public 두가지이다.   
public으로 선언할 경우 공개 API가 되어 하위 호환을 위해 영원히 관리해야 하고, 
package-private으로 선언하면 해당 패키지 안에서만 이용 가능하므로 패키지 외부에서 쓸 이유가 없다면 package-private으로 선언하도록 한다.

package-private 톱레벨 클래스나 인터페이스를 한 클래스에서만 사용한다면, 이를 사용하는 클래스 안에 private static으로 중첩시키자.
톱레벨로 두면 같은 패키지의 모든 클래스가 접근할 수 있지만, private static으로 중첩시키면 바깥 클래스 하나에서만 접근이 가능하다.

하지만 무엇보다 public일 필요가 없는 클래스의 접근 수준을 package-private 톱레벨 클래스로 줄이는 것이 훨씬 더 중요하다.
왜냐하면 public 클래스는 그 패키지의 API인 반면에 package-private 톱레벨 클래스는 내부 구현에 속하기 때문이다. 

## 멤버의 접근 수준
*_멤버란?_ 필드, 메서드, 중첩 클래스, 중첩 인터페이스를 말한다.  

- private : 멤버를 선언한 톱레벨 클래스에서만 접근 가능하다
- package-private : 멤버가 소속된 패키지 안의 모든 클래스에서 접근 가능하다. (default-단, 인터페이스의 멤버는 public이 default)
- protected : package-private의 접근 범위를 포함하고, 이 멤버를 선언한 클래스의 하위 클래스에서도 접근할 수 있다. 
- public : 모든 곳에서 접근할 수 있다.

클래스의 공개 API를 설계한 후, 그 외의 모든 멤버는 private으로 만든다. 그 후, 같은 패키지, 다른 클래스가 접근해야 하는 멤버에 한해
package-private으로 권한을 풀어준다.  
이때 권한을 자주 풀어줘야한다면 시스템에서 컴포넌트를 더 분해할 것을 고려해본다.  
private, package-private 멤버는 모두 해당 클래스의 구현에 해당되므로 보통 공개 API에 영향을 주지 않지만,
Serializable을 구현한 클래스에서는 그 필드들도 의도치 않게 공개 API가 될 수도 있다.

public클래스에서 멤버의 접근 수준을 package-private에서 protected로 바꾸는 순간 그 멤버에 접근 가능한 범위가 엄청나게 넓어진다.
따라서 public 클래스의 protected멤버는 공개 API이므로 영원히 지원되어야 한다. 
또한 내부 동작 방식을 API문서에 적어 사용자에게 공개해야 할 수도 있다.

**멤버 접근성을 좁히지 못하게 방해하는 제약(리스코프 치환 원칙)**  
리스코프 치환 원칙 : 상위 클래스의 인스턴스는 하위 클래스의 인스턴스로 대체해 사용할 수 있어야한다는 규칙  
리스코프 치환 원칙을 지키기 위해 상위 클래스의 메서드를 재정의할 때는 그 접근 수준을 상위 클래스에서보다 좁게 설정할 수 없다.
(이를 어기면 하위 클래스를 컴파일할때 컴파일 오류가 난다.)
따라서 인터페이스를 구현한 클래스의 경우, 인터페이스가 정의한 모든 메서드를 public으로 선언해야한다.

코드를 테스트하기 위해 클래스, 인터페이스, 멤버의 접근 범위를 넓힐 때가 있다. 이때 public클래스의 private멤버를 package-private까지 풀어주는 정도의
적당한 수준은 허용가능하지만 그 이상은 안된다. 즉, 테스트만을 위해 클래스, 인터페이스, 멤버를 공개 API로 만들면 안된다. 
테스트 코드와 테스트 대상을 같은 패키지에 두면 package-private 요소에 접근 가능하므로 굳이 공개 API로 할 이유가 없다. 

## public클래스의 인스턴스 필드
public클래스의 인스턴스 필드는 되도록 public이 아니어야한다. 필드가 가변 객체를 참조하거나, final이 아닌 필드를 public으로 선언하면
그 필드에 담을 수 있는 값을 제한할 수 없다. 더욱이 필드가 수정될 때 다른 작업(ex.락 획득)을 할 수 없게 되므로 public 가변 필드를 갖는 클래스는
일반적으로 스레드 안전하지 않다. 심지어 필드가 불변 객체를 참조하더라도 내부 구현을 바꿀때 public 필드를 없애는 방식으로 리팩터링할 수 없다는 점에서
여전히 문제가 남아있다.   
이때 예외가 하나 있는데, 이는 클래스가 표현하는 추상 개념을 완성하는데 꼭 필요한 구성요소로써의 상수라면 public static final 필드로 공개해도 좋다. 
일반적으로 이 상수에는 대문자 알파벳으로 쓰고, 각 단어 사이에 _을 넣는다. 이때 이 필드는 반드시 기본 타입 값이나 불변 객체를 참조해야한다. 

**길이가 0이 아닌 배열은 모두 변경 가능하니 주의한다.**  
클래스에서 public static final 배열 필드를 두거나 이 필드를 반환하는 접근자 메서드를 제공하면 안된다. 또한 어떤 IDE에서는 private 배열 필드의 참조를 반환해
똑같은 문제를 일으키기도 하는데 이에대한 해결방법은 다음 두 가지가 있다. 

**M1. public 배열을 private으로 만들고 public 불변 리스트를 추가한다.**  
```java
// 보안상 허점이 있는 배열 필드
public static final Thing[] VALUES = {};

// 해당 배열을 private으로 만들고 public 불변 리스트를 추가한다.
private static final Thing[] PRIVATE_VALUES = {};
public static final List<Thing> VALUES = Collections.unmodifiableList(Arrays.asList(PRIVATE_VALUES));
```
**M2. 배열을 private으로 만들고 그 복사본을 반환하는 public메서드를 추가한다.(방어적 복사)**
```java
private static final Thing[] PRIVATE_VALUE = {};
public static final Thing[] values() {
    return PRIVATE_VALUE.clone();
}
```

## 자바 9버전 이후
자바 9버전 이후 모듈 시스템이라는 개념이 도입되었다. 이로 인해 암묵적으로 두가지 접근 수준이 추가되었다.  
_**(자바에서의 모듈은 외부에서 재사용 할 수 있는 패키지들을 묶은 것을 말한다)**_  
모듈은 자신에 속하는 패키지 중 공개할 것들을 (일반적으로 module-info.java 파일에) 선언한다. 따라서 protected나 public멤버라도
해당 패키지를 공개하지 않았다면 그 모듈의 외부에서는 접근할 수 없다. 
모듈 시스템을 활용하면 클래스를 외부에 공개하지 않으면서 같은 모듈 내 패키지 사이에서는 자유롭게 공유가 가능하다. 
public 클래스의 public / protected 멤버는 각각 public / protected수준과 같지만 그 효과가 모듈 내부로 한정되어 있다.

해당 이 두 접근은 사용에 주의해야한다. 모듈의 JAR파일을 애플리케이션의 클래스패스에 두면 그 모듈 내의 모든 패키지는 모듈이 없는 것처럼 행동한다.
즉, 모듈의 공개 여부와 상관없이, public클래스가 선언한 모든 public혹은 protected멤버를 모듈 밖에서도 접근 가능하다.   
JDK : 이러한 접근 수준을 적극 이용한 예로, 자바 라이브러리가 공개하지 않은 패키지는 모듈 밖에서 절대 접근할 수 없다. 

**모듈을 사용하려면?**  
패키지들을 모듈 단위로 묶고, 모듈 선언에 패키지들의 모든 의존성을 명시해야한다.  
그 후, 소스 트리를 재배치하고, 모듈 안으로부터 모듈 시스템을 적용하지 않는 일반 패키지로의 모든 접근에 조치를 취해야한다.   