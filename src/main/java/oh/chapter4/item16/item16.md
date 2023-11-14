# Itme16. public클래스에서는 public 필드가 아닌 접근자 메서드를 사용하라.

## 퇴보한 클래스
퇴보한 클래스란 인스턴스 필드들을 모아놓는 일 외에는 아무 목적도 없는 클래스이다.
```java
// 퇴보한 클래스의 예시
class Point {
    public double x;
    public double y;
}
```
이러한 퇴보한 클래스의 경우 데이터 필드에 직접적으로 접근할 수 있어 캡슐화의 이점을 활용하지 못한다.  
따라서 필드들은 private으로 바꾸고 public 접근자(getter)를 추가한다.

```java
class Point {
    private double x;
    private double y;
    
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
}
```
public클래스라면 위와 같은 방식을 사용해 접근자를 제공하도록 한다. 이를 통해 내부 표현 방식을 언제든 바꿀수 있는 유연성을 얻게 된다.

## package-private / private 중첩 클래스
package-private 클래스나 private 중첩 클래스는 데이터 필드를 노출한다 해도 문제가 없다. 
노출한다면 클라이언트 코드가 이 클래스 내부 표현에 묶이긴 하지만, 어차피 이 클래스를 포함하는 패키지 안에서만 동작하기때문이다.  
private 중첩 클래스의 경우 수정 범위가 더 좁아져서 이 클래스를 포함하는 외부 클래스까지 제한된다. 

## 예외
public 클래스의 필드를 직접 노출시킨 예시가 있다. 바로 java.awt.package의 **Point**와 **Dimension**클래스이다.  
```java
public class Dimension {
    public int width;
    public int height;
}

public abstract class Component {
    // ...
    public Dimension getSize() {
        return size();
    }
    
    public Dimension size() {
        return new Dimension(width, height);
    }
}
```
Dimension이 가변으로 설정되어 있기 때문에 getSize()를 호출하는 모든 곳에서는 Dimension 인스턴스를 매번 새로 생성해야해 성능에 문제가 있다. 

public 클래스의 불변인 필드를 직접 노출할때, 불변식은 보장하더라도, API를 변경하지 않고는 표현 방식을 바꿀 수 없고, 필드를 읽을 때 부수 작업을 수행할 수 없다는 단점은 여전하다.
```java
public final class Time {
    private static final int HOURS_PER_DAY = 24;
    private static final int MINUTES_PER_HOUR = 60;
    
    public final int hour;
    public final int minute;
    
    public Time(int hour, int minute) {
        if (hour < 0 || hour >= HOURS_PER_DAY)
            throw new IllegalArgumentException("시간: " + hour);
        if (minute < 0 || minute >= MINUTES_PER_HOUR)
            throw new IllegalArgumentException("분: " + minute);
        this.hour = hour;
        this.minute = minute;
    }
    // 코드 생략
}
```
