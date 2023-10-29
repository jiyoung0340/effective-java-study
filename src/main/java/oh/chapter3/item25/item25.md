# Itme25. 톱레벨 클래스는 한 파일에 하나만 담으라

## 한 파일안에 톱레벨 클래스가 여러개 있다면?
컴파일 상으로는 문제가 없지만, 한 클래스를 여러가지로 정의할 수 있고 어느 것을 사용할지는 어느 소스 파일을 먼저 컴파일하느냐에 따라 달라지기 때문에 심각한 문제를 일으킬 수 있다.
```java
public class Main{
    public static void main(String[] args) {
        System.out.println(Utensil.NAME + Dessert.NAME);
    }
}
```
다음은 Utensil.java라는 파일이다.
```java
class Utensil {
    static final String NAME = "pan";
}

class Dessert {
    static final String NAME = "cake";
}
```
다음은 Dessert.java라는 파일이다.
```java
class Utensil {
    static final String NAME = "pot";
}

class Dessert {
    static final String NAME = "pie";
}
```
만약, 다음과 같은 명령어를 실행한다면
```text
$ javac Main.java Dessert.java
```
컴파일 오류가 나며 Utensil과 Dessert 클래스의 중복 정의를 알려줄 것이다. 
하지만, 다음의 명령어를 실행한다면
```text
$ javac Main.java
$ javac Main.java Utensil.java
```
결과는 pancake를 출력할 것이고
다음의 명령어를 실행한다면
```text
javac Dessert.java Main.java
```
결과는 potpie를 출력할 것이다.
이렇게 어느 소스 파일이 먼저 컴파일 되느냐에 따라 동작이 달라지므로 한 파일안에 여러 클래스를 두는 것은 상당히 위험하다.

## 해결방법
톱레벨 클래스들을 서로 다른 소스 파일로 분리하되 굳이 여러 톱레벨 클래스를 한 파일에 담고 싶다면 정적 멤버 클래스 사용을 고려해본다. 
다른 클래스에 딸린 부차적인 클래스라면 정적 멤버 클래스로 만드는 것이 읽기 좋고, private으로 선언하면 접근 범위도 최소로 관리할 수 있어 더욱 좋다.
앞선 예를 정적 멤버 클래스로 바꾼다면 다음과 같다.
```java
public class Test {
    public static void main(String[] args) {
        System.out.println(Utensil.NAME + Dessert.NAME);
    }

    private static class Utensil {
        static final String NAME = "pan";
    }
    
    private static class Dessert {
        static final String NAME = "cake";
    }
}
```