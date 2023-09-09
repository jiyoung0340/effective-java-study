# Itme11. equals를 재정의하려거든 hashCode도 재정의하라.


## equals를 재정의한 클래스 모두에서 hashCode도 재정의해야 한다.
hashCode를 재정의하지 않을 경우, 클래스의 인스턴스를 HashMap이나 HashSet같은 컬렉션의 원소로 사용할 때 문제가 발생한다.

- equals비교에 사용되는 정보가 변경되지 않았다면, 애플리케이션이 실행되는 동안 그 객체의 hashCode메소드는 여러번 호출해도 일관된 값을 반환해야 한다.
  (단, 애플리케이션을 다시 실행했을 경우, 이 값이 달라져도 상관없다.)
- equals(Object)가 두 객체를 같다고 판단했다면, 두 객체의 hashCode는 똑같은 값을 반환해야 한다. 
- equals(Object)가 두 객체를 다르다고 판단했더라도, 두 객체의 hashCode가 다를 필요는 없다.
  (단, 다른 객체일 때, 다른 hashCode값을 반환해야 해시테이블의 성능이 좋아진다.)

## hashCode를 재정의할 때 주의해야할 규약은 두번째이다.
논리적으로 같은 객체는 같은 해시코드를 반환해야한다.
```java
    Map<PhoneNumber, String> m = new HashMap<>();
    m.put(new PhoneNumber(02, 123, 1234), "제니"); // -- (1)
    m.get(new PhoneNumber(02, 123, 1234)); // -- (2)
```
(2)를 실행할 경우 "제니"가 나올 것 같지만, 실제로는 null을 반환한다. 
왜냐하면 (1)에서의 객체와 (2)에서의 객체는 다른 인스턴스이기 때문이다.  
두 객체는 논리적으로 동치일 뿐, PhoneNumber클래스에서 hashCode를 재정의하지 않았기 때문에 
서로다른 해시코드값을 반환해 두번째 규약을 지키지못한다.

## 또 다른, 최악의 hashCode
```java
  @Override
    public int hashCode() {
        return 42;
    }
```
이와 같이 해시코드를 항상 같은 값으로 반환하면, 모든 객체는 하나의 해시테이블 버킷에 담겨버린다. 따라서 하나의 연결 리스트처럼 동작해버린다.
이는 곧 평균 수행 시간이 O(1)인 해시테이블이 O(n)으로 느려져, 객체가 많아지면 사용하기 어렵다.  
가장 이상적인 해시 함수는 서로 다른 인스턴스들이 32비트 정수 범위에 균일하게 분포해야한다는 것이다.

## 좋은 hashCode를 작성하는 간단한 요령은 다음과 같다.
> 1. int 변수 result를 선언하고 c로 초기화한다.(c는 첫번째 핵심필드를 다음 2.1방식으로 구한 해시코드 값이다.)
> 2. 해당 객체의 나머지 핵심 필드 f 각각에 대해 다음 작업을 수행한다.  
>   1) 해당 필드의 해시 코드 c를 계산한다.  
      - 기본 타입 필드의 경우, Type.hashCode(f)를 수행한다. (Type은 해당 기본 타입의 박싱 클래스)  
      - 참조 타입 필드이면서, 이 클래스의 equals 메서드가 필드의 equals를 재귀적으로 호출해 비교하면, hashCode()또한 재귀적으로 호출한다.  
            (계산이 복잡해질 경우, 이 필드의 표준형을 만들어 표준형의 hashCode를 호출한다.)  
      - 필드가 배열일 경우, 핵심 원소 각각을 별도의 필드처럼 다룬다. 핵심 원소가 하나도 없다면 단순히 상수(0 추천)를 사용하고, 모든 원소가 핵심 원소라면 Arrays.hashCode를 사용한다.
>   2) 단계 2.a에서 계산한 해시코드 c로 result를 갱신한다.  
        result = 31 * result + c
> 3. result를 반환한다.

* 파생 필드는 해시코드 계산에서 제외해도 된다. 즉, 다른 필드로부터 계산해낼 수 있는 필드는 모두 무시해도 된다.
* equals 비교에 사용되지 않은 필드는 **반드시** 제외해야한다. 그렇지 않으면 또 다시 두번째 규약을 어기게 될 것이다.
* 2.2의 곱셈 31 * result는 필드를 곱하는 순서에 따라 result값이 달라지게 한다. 이는 비슷한 필드가 여러 개일 때, 해시 효과를 높여준다.  
  - hashCode를 곱셈 없이 구현한다면, 예를 들어 String의 경우 아나그램(anagram; 구성 철자는 같지만 순서가 다른 문자열)의 해시코드가 같아진다.
  ```java
    // 곱셈이 없이 구현된다면 다음과 같을 것이다.
    public int hashCode() {
        int result = Short.hashCode(areaCode);
        result = result + Short.hashCode(prefix);
        result = result + Short.hashCode(lineNum);
        return result;
    }
  ```
  따라서 곱셈이 없을 경우, 순서가 달라져도 동일한 해시코드값을 반환하게 된다.
* 31로 정한 이유는 31이 홀수이면서 소수이기 때문이다. 이 숫자가 짝수이고, 오버플로우가 발생하면 정보를 잃는다.
  2로 정한다면 이는 시프트연산과 같은 결과를 낸다.  
* 소수를 곱하는 이유는 전통적으로 그리 해왔다.   
  따라서 31을 이용하면, 이 곱셈을 시프트 연산과 뺄셈으로 대체해 최적화할 수 있다.   
    **31 * i 는 ( i << 5) - i 와 같다**

다음은 전형적인 hashCode 메서드이다.
```java
    @Override
    public int hashCode() {
        int result = Short.hashCode(areaCode);
        result = 31 * result + Short.hashCode(prefix);
        result = 31 * result + Short.hashCode(lineNum);
        return result;
    }
```

## Object 클래스에서 제공하는 hash 메서드
Object 클래스는 임의의 개수만큼 객체를 받아 해시코드를 계산해주는 정적 메서드 hash를 제공한다. 
이 메서드를 활용하면 앞 요령대로 구현한 코드와 비슷한 수준의 hashCode함수를 단 한 줄로 작성할 수 있다.  
하지만 속도가 더 느리다. 왜냐하면 입력 인수를 담기 위한 배열이 만들어지고, 입력 중 기본 타입이 있다면 (언)박싱을 거쳐야하기 때문이다.
그러므로 성능에 민감하지 않은 경우에 사용하자!

```java
    @Override
    public int hashCode(){
        return Objects.hash(lineNum, prefix, areaCode);
    }
```
## 그리고 더 고려해야 할 것들
- 클래스가 불변이고 해시코드를 계산하는 비용이 크면, 캐싱하는 방식을 고려해야한다.  
- 이 타입의 객체가 주로 해시의 키로 사용될 것 같으면 인스턴스가 만들어질 대 해시코드를 게산해둬야한다.  
- 해시의 키로 사용되지 않는다면 hashCode가 처음 불릴 때 계산한다. 이를 지연 초기화 전략이라고한다.   
    지연 초기화하려면 그 클래스를 스레드 안전하게 만들도록 신경써야한다.(Item83)
    ```java
        private int hashCode; // 자동으로 0으로 초기화한다.
  
        @Override
        public int hashCode() {
            int result = hashCode;
            if (result == 0) {
                int result = Short.hashCode(areaCode);
                result = 31 * result + Short.hashCode(prefix);
                result = 31 * result + Short.hashCode(lineNum);
                return result;
            }
        } 
    ```
- 성능을 높이기위해 해시코드를 계산할 때 핵심 필드를 생략하면 안된다.
  자바 2 전의 String 객체는 최대 16개의 문자만으로 해시코드를 계산했다. 문자열이 길면 균일하게 나눠 16문자만 뽑아내 사용한 것이다. 
  URI처럼 계층적인 이름을 대량으로 사용하면 이 해시함수는 (몇개의 해시 코드로 집중되어 해시 테이블의 속도가 느려지는)심각한 문제가 발생할 것이다.
- hashCode가 반환하는 값의 생성 규칙을 API 사용자에게 자세히 공표하지 않는다.
  클라이언트가 이 값에 의지하지 않게 되고, 추후 계산 방식을 바꿀 수도 있다.
