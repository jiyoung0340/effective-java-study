# Itme23. 태그 달린 클래스보다는 클래스 계층구조를 활용하라

## 태그 달린 클래스란?
태그 달린 클래스란 두 가지 이상의 의미를 표현할 수 있으며, 그중 현재 표현하는 의미를 태그 값으로 알려주는 클래스이다. 
```java
class Figure {
    enum Shape { RECTANGLE, CIRCLE }
    
    // 태그 필드 - 현재 모양을 나타낸다
    final Shape shape;
    // 다음 필드들은 모양이 사격형일 때만 쓰인다    
    double length;
    double width;
    // 다음 필드는 모양이 원일 때만 쓰인다.
    double radius;
    // 원용 생성자
    Figure(double radius) {
        shape = Shape.CIRCLE;
        this.radius = radius;
    }
    // 사각형용 생성자
    Figure(double length, double width){
        shape = Shape.RECTANGLE;
        this.length = length;
        this.width = width;
    }
    // 넓이구하는 함수
    double area() {
        switch(shape) {
            case RECTANGLE:
                return length * width;
            case CIRCLE:
                return Math.PI * (radius * radius);
            default:
                throw new AssertionError(shape);
        }
    }
}
```
위의 코드는 태그달린 클래스의 대표적인 예이다. 이러한 코드의 단점은 다음과 같다.
1. 열거 타입 선언, 태그 필드, switch문 등 쓸데없는 코드가 많다.
2. 한 클래스 안에 여러 구현이 있어 가독성이 떨어진다.
3. 다른 의미의 코드도 함께 있어 메모리를 많이 사용한다.
4. 필드를 final로 선언하려면 불필요한 필드까지 생성자에서 초기화해야 한다.(=불필요한 코드가 늘어난다.)
5. 필드 초기화 시 컴파일단계에서 오류를 찾을 수 없다. (=엉뚱한 필드를 초기화해도 런타임에서 오류가 발생한다.)
6. 새로운 의미를 추가할 때 코드를 수정해야 한다.
7. 인스턴스의 타입만으로는 현재 나타내는 의미를 알 수 없다.

> **계층구조의 클래스**

태그달린 클래스를 클래스 계층구조로 바꾸는 방법
1. 계층구조의 루트(root)가 될 추상 클래스를 정의한다.
2. 태그 값에 따라 동작이 달라지는 메서드들을 루트 클래스의 추상 메서드로 선언한다.
3. 태그 값과 상관없이 동작이 일정한 메서드들은 루트 클래스의 일반 메서드로 선언한다.
4. 하위 클래스에서 공통으로 사용하는 데이터필드들은 루트 클래스에서 선언한다.
5. 루트 클래스를 확장한 구체 클래스를 의미별로 하나씩 정의하고 루트 클래스의 추상 메서드를 각자 의미에 맞게 구현한다.

```java
import ka.chapter2.item1.framework.Rectangle;

abstract class Figure {
    abstract double area();
}

class Circle extends Figure {
    final double radius;
    Circle(double radius) {
        this.radius = radius;
    }
    @Override double area() { return Math.PI * (radius * radius); }
}

class Rectagle extends Figure {
    final double length;
    final double width;
    Rectangle(double length, double width) {
        this.length = length;
        this.width = width;
    }
    @Override double area() { return length * width; }
}

// 새로운 의미를 추가할 때 계층 관계를 반영할 수 있다.
class Square extends Rectangle {
    Sqaure(double side) {
        super(side, side);
    }
} 
```
