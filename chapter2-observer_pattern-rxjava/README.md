
# 2장 리액티브 프로그래밍 기본 개념 

다룰 주제 
- 관찰자 패턴
- 발행-구독(publish-subscribe) 패턴 
- RxJava 의 역사 및 기본 개념 
- Marble(마블) 다이어그램 
- 리액티브 프로그래밍을 적용한 비지니스 사례 
- 리액티브 라이브러리 현황 

* 1장에서 Future 와 CompletableFuture 에 대해서 다룸 
ㄴ 자세히 안나옴 ... 모던 자바 스터디에 있는데 .. 추가할까 ?? 

## 리액티브 프로그래밍 기본 개념 

### Observer(관찰자) 패턴 
리액티브 프로그래밍의 기초라고 할 수 있는 **GoF(Gang of Four) 디자인 패턴** 중 하나인 Observer 패턴에 대해서 공부할 필요가 있다  
- 디자인 패턴에 대한 자세한 내용은 [GoF 디자인 패턴 위키](https://en.wikipedia.org/wiki/Design_Patterns) 에서 확인할 수 있다   
  
Observer 패턴에서는 관찰자라고 불리는 Subject(주제) 를 필요로 함 
주체는 일반적으로 자신의 메서드 중 하나를 호출해서 관찰자에게 상태 변경을 알림
ㄴ 거의 모든 UI 라이브러리가 이러한 패턴을 사용함   
  
Observer 패턴 class 다이어 그램 
[observer pattern class diagram](/assets/images/posts/20201201/observer_pattern_class_diagram.png)  
  
위 클래스 다이어그램에서 볼 수 있듯이 관찰자 패턴에서는 Subject 와 Observer 2개의 인터페이스로 구성된다  
Observer 는 Subject 에 등록되고 Subject 의 notifyObserver() 메소드가 호출을 통해서 Subject 에 등록된 Observer 들이 호출되는 형식이다  
Java 코드로 나타내면 아래와 같다  

```java
public interface Subject<T> {
    void registerObserver(Observer<T> observer);
    void unregisterObserver(Observer<T> observer);
    void notifyObservers(T event);
}

public interface Observer<T> {
    void observe(T event);
}
```
=======


관찰자 패턴을 사용하면 런타임에 객체 사이에 일대다 의존성을 등록할 수 있음 
이를 통해 각 부분이 활발히 상호작용하게 하면서도 응용 프로그램 사이의 결합도를 낮출 수 있음 
이런 유형의 통신은 단방향으로 이루어지며 , 다이어그램에서 나오는 것처럼 시스템을 통해 이벤트를 배포하는데 도움이 된다 


TODO
- 옵저버 패턴은 사실 java util 에도 존재함 -> 자바 스터디 


## 리액티브 프레임 워크 RxJava
[RxJava](https://github.com/ReactiveX/RxJava) 1.x 버전은 Java 진영에서 리액티브 프로그래밍을 위한 표준 라이브러리 였음  
ㄴ RxJava 를 제외하고 리액티브 라이브러리는 AKKa Streams, Reactor(스프링 webflux 의 기반) 라이브러리가 있음  
RxJava 라이브러리는 **Reactive Extensions(ReactiveX)** 의 자바 구현체
[ReactiveX](http://reactivex.io/) 는 명령형 언어를 이용하여 동기식 or 비동기식 데이터 스트림을 조작할 수 있는 도구로 Observer 패턴, Iterator 패턴 및 함수형 프로그래밍의 조합으로 정의된다  
ㄴ 뒤에서 추가 설명이 나오지만 ReactiveX 는 MS 에서 만들었다고 한다  

### 관찰자 + 반복자 = 리액티브 스트림 
관찰자 패턴 요약
```java
public interface Subject<T> {
    void registerObserver(Observer<T> observer);
    void unregisterObserver(Observer<T> observer);
    void notifyObservers(T event);
}

public interface Observer<T> {
    void observe(T event);
}
```
관찰자 패턴을 이용한 접근법은 무한한 데이터 스트림에 대해서는 매력적이지만, 데이터 스트림의 끝을 알리는 기능이 있으면 더욱 매력적일것임  
또한 컨슈머가 준비되기 이전에 프로듀서가 이벤트를 생성하는 것은 우리가 바라는 상황이 아니기 때문에 이를 방지해야한다  

이럴때 동기식 셰계에서는 반복자 패턴을 이용할 수 있음  
```java
public interface Iterator<T> {
    T next();
    boolean hasNext();
}
```
hasNext() 를 이용하여 결과의 끝을 알 수 있음  
  
비동기식 관찰자 패턴에 이런 반복자 패턴을 혼합해보자  
```java
public interface RxObserver<T> {
    void onNext(T next);
    // 이벤트의 끝을 알리는 메소드 
    void onComplete(); 
    // 오류 전파를 위한 메소드 
    void onError(Exception e);
}
```
우리는 위 클래스를 이용하여 리액티브 스트림의 모든 컴포넌트 사이에 데이터가 흐르는 방법을 정의할 수 있다  


[RxJava observer and observable](/assets/images/posts/20201201/reactive_observable_observer.png)  
리액티브 스트림 Observable 클래스 = 관찰자 패턴의 Subject 클래스 이며, 이벤트를 발생 시킬 때 이벤트 소스 역할을 수행  
실제로 Observable 클래스에는 수십 가지의 팩토리 메소드와 수백 가지의 스트림 변환 메소드가 포함되어 있다  
Subscriber 추상 클래스는 Observer 인터페이스를 구현하고 이벤트를 소비한다 
Observable 과 Subscriber 의 관계는 메시지 구독 상태를 확인하고 필요한 경우 이를 취소할 수도 있는 구독에 의해 제어된다 

RxJava 에서 Observable 은 0개 이상의 이벤트를 보낼수 있다  
그런 다음 성공을 알리거나 오류를 발생시켜 실행 종료를 알린다  
따라서 각 구독자에 대한 Observable 은 onNext() 를 여러번 호출한 다음 onComplete() 또는 onError() 를 호출한다  
  
### 스트림의 생산과 소비 
RxJava 의 Observable 로 표현되는 스트림 정의  
```java
Observable<String> observable = Observable.create(
        new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> sub) {
                sub.onNext("Hello, reactive world!");
                sub.onCompleted();
            }
        }
);
```

위에 작성한 코드는 구독자가 나타나자 마자 이벤트 1회 전파후 바로 종료함  

 
RxJava 1.27 부터는 Observable 을 이용하는 방식은 더 이상 사용되지 않음  
이 방식은 생성하는 것들이 너무 많고 구독자에게 과도한 부하를 줄 수 있어 안전하지 않다  
즉, backpressure 를 지원하지 않는다 
자세한 내용은 나중에 다시 다룰 것임. 다만 기본 개념을 익히기 위해서 위의 코드를 작성함  

구독자 코드를 작성해보자 
```java
Subscriber<String> subscriber = new Subscriber<String>() {
    @Override
    public void onNext(String s) {
        System.out.println(s);
    }
    @Override
    public void onCompleted() {
        System.out.println("Done!");
    }
    @Override
    public void onError(Throwable e) {
        System.out.println(e);
    }
};
```
Subscriber 에서 onError 메서드를 구현하지 않으면 오류가 발생하면 rx.exceptions.OnErrorNotImplementedException 을 발생시킨다 
  
실행 결과 
```
Hello, reactive world!
Done!
```

람다식으로 표현해본 코드 
```java
Observable.create(
        sub -> {
            sub.onNext("Hello, reactive world!");
            sub.onCompleted();
        }
).subscribe(
        System.out::println, // onNext
        System.out::println, // onError
        () -> System.out.println("Done!") // onCompleted
);
```

Observable 의 팩토리 메소드 
RxJava 라이브러리는 Observable 및 Subscriber 인스턴스를 생성하기 위해서 많은 유연성을 제공함  
```java
Observable.just("1", "2", "3", "4")
        .subscribe(
                System.out::println, // onNext
                System.out::println, // onError
                () -> System.out.println("Done!") // onCompleted
        );
Observable.from(new String[]{"A", "B", "C"})
        .subscribe(
                System.out::println, // onNext
                System.out::println, // onError
                () -> System.out.println("Done!") // onCompleted
        );
Observable.from(Collections.emptyList())
        .subscribe(
            System.out::println, // onNext
            System.out::println, // onError
            () -> System.out.println("Done!") // onCompleted
        );
```
  
- callable 이용한 방법 
```java
Observable<String> hello = Observable.fromCallable(() -> "Hello ");
Future<String> future = Executors.newCachedThreadPool().submit(() -> "World");
Observable<String> world = Observable.from(future);

// concat 으로 스트림을 연결하여 forEach 로 문자열을 출력 
Observable.concat(hello, world, Observable.just("!"))
        .forEach(System.out::print);

// 출력결과 Hello World!
```
또한 Callable, Future 를 이용해서 생성 가능하며, concat 메소드를 이용해서 입력 스트림을 다운 스트림 Observable 로 다시 보낼수 있다  
입력 스트림은 종료 신호(onCompleted(), onError()) 가 발생할 때까지 처리되며 처리 순서는 concat 의 인수의 순서대로 처리된다  

### 비동기 시퀀스 생성하기  
interval 메소드를 이용해서 주기적으로 비동기 이벤트 시퀀스를 생성할 수 있다  
```java
Observable.interval(1, TimeUnit.SECONDS)
        .subscribe(e -> System.out.println("Received: " + e));

// 이 코드를 제거하면 아무것도 일어나지 않는다
Thread.sleep(5000);
```
Thread.sleep 메소드를 제거하면 아무것도 출력하지 않고 종료되는데, 이것은 이벤트가 생성되는 스레드는 현재 코드가 실행되는 스레드와 다른 스레드이기 때문  
Thread.sleep 을 이용해서 메인 스레드 종료를 지연시킨 것으로 메인 스레드가 종료되지 않는다면 subscribe 는 무한히 호출된다  
  
구독을 취소하기위해서 Subscription 이라는 인터페이스가 존재한다  
```java
public interface Subscription {
    void unsubscribe();
    boolean isUnsubscribed();
}
```
구독자는 unsubscribe() 메소드를 이용해서 구독을 취소할 수 있으며, Observable 은 isUnsubscribed() 메소드를 이용해서 구독자가 여전히 이벤트를 기다리는지 확인한다  
  
구독 취소하는 예제  
```java
// countDown() 3 번 호출되야 종료됨 
CountDownLatch externalSignal = new CountDownLatch(3);

Subscription subscription = Observable
        .interval(100, TimeUnit.MILLISECONDS)
        .subscribe(System.out::println);

// 3초간 대기하도록 ..
Thread thread = new Thread(() -> {
    try {
        while(externalSignal.getCount() > 0) {
            Thread.sleep(1000);
            externalSignal.countDown();
        }
    } catch(InterruptedException e){}
});
thread.start();

// 대기한다 count = 0 일때까지 
externalSignal.await();
subscription.unsubscribe();

// unsubscribe 이후에는 몇 초를 대기하던 출력되지 않음
Thread.sleep(2000);
```
CountDownLatch 는 await() 이 호출되어 count = 0 이 되기전까지 대기하고 count = 0 이 되면 unsubscribe() 가 호출되어 구독을 취소한다  
구독 취소 이후에는 몇 초를 기다리던 아무것도 출력되지 않는다  
ㄴ 책 예제가 불완전하여 코드를 추가함  


### 스트림 변환과 마블 다이어그램 
RxJava 에는 거의 모든 시나리오에서 사용할 수 있는 엄청난 양의 연산자가 있지만, 여기서는 기본 연산자들을 살펴보자  
대부분의 다른 연산자들은 기본 연산자의 조합일 뿐임  
  
마블 다이어그램(marble diagram) 은 리액티브 커뮤니티에서 리액티브 스트림을 시각적으로 표현할 때 사용하는 다이어그램  
- 수평선으로 표시된 리액티브 스트림에 임의의 순서로 구성요소가 기하학적 모형 으로 표시된다  
- 특수 기호는 에러나 종료 신호를 나타냄 
- box 는 해당 요소가 어떻게 변화하는지를 나타낸다 
  
#### Map 연산자 
```java
public final <R> Observable<R> map(Func1<? super T, ? extends R> func)
```
- T 를 R 로 변환하는 func 을 파라미터로 받아서 Observable<T> 를 Observable<R> 로 변환할 수 있음을 나타낸다  
- RxJava 에서 가장 많이 사용하는 연산자 
[marble diagram map](/assets/images/posts/20201201/marble_diagram_map.png)  
위 다이어그램을 보면 map 을 통해서 원소가 하나씩 변환되는 것을 알 수 있다  
ㄴ 사실 자바의 함수형 인터페이스인 Function 을 Observable 로 wrapping 한 것 처럼 보인다  

```java
Observable.interval(100, TimeUnit.MILLISECONDS)
        .map(e -> e * 2)
        .subscribe(System.out::println);

Thread.sleep(500);  
```
  
#### Filter 연산자 
```java
public final Observable<T> filter(Func1<? super T, Boolean> predicate)
```
- 파라미터로 predicate 를 받아서 조건이 만족하는 요소만 filtering 할 수 있는 연산자 
[marble diagram map](/assets/images/posts/20201201/marble_diagram_filter.png)  
위 다이어그램을 보면 조건을 만족한 원소들만 추출된다  
  
### Count 연산자 
```java
public final Observable<Integer> count()
```
- 입력 스트림의 개수를 발행
- 스트림이 완료될때에만 카운트가 발행되기 때문에 스트림이 무한대일 때는 count 연산자가 완료되지 않거나 아무것도 반환하지 않는다  
[marble diagram map](/assets/images/posts/20201201/marble_diagram_count.png)  
```java
Observable.just("1", "2", "3", "4")
        .count()
        .subscribe(System.out::println);

// 무한 스트림은 결과가 없다 
Observable.interval(100, TimeUnit.MILLISECONDS)
        .count()
        .subscribe(System.out::println);

Thread.sleep(500);
```

#### Zip 연산자 
```java
public static <T1, T2, R> Observable<R> zip(Observable<? extends T1> o1, Observable<? extends T2> o2, final Func2<? super T1, ? super T2, ? extends R> zipFunction)
```
- 두 개의 병렬 스트림 값을 결합할 때 이용하는 연산자 
- 아래 다이어그램에서 처럼 예상되는 결과의 일부가 다른 출처에서 발행될 때 데이터를 결합하는데 사용함 
- 2개의 Observable 응 받아서 zipFunction 이 적용된 결과를 반환 
[marble diagram map](/assets/images/posts/20201201/marble_diagram_count.png)  
넷플릭스는 추천 비디오 목록을 스트리밍할 때 zip 연산자를 사용해 영화 설명, 영화 포스터, 영화 등급을 결합한다고 함  
두 개의 문자열 값 스트림을 압축하는 예제  
```java
Observable.zip(
        Observable.just("A", "B", "C"),
        Observable.just("1", "2", "3"),
        (x, y) -> x + y
).forEach(System.out::println);

// 결과 
// A1
// B2
// C3
```
  
RxJava 에서 사용하는 다른 연산자에 대한 마블 다이어그램은 [https://rxmarbles.com/](https://rxmarbles.com/) 에서 확인할 수 있다  
해당 사이트는 [RxJs](https://github.com/ReactiveX/rxjs) 로 작성되었지만 Reactive Js 로 대부분의 연산자는 유사하다고 함  
  
#### 커스텀 연산자 
Observable.Transformer<T, R> 에서 파생된 클래스를 구현하여 사용자 지정 연산자를 작성할 수 있다  
사용자 지정 연산자 로직은 Observable.compose(transformer) 연산자를 적용해 워크 플로에 포함될 수 있다  
```java
Observable.Transformer<String, Integer> toLengthFun = (stringObservable) -> stringObservable.map(String::length);

Observable.just("leo1", "leo@kakao.com", "abcdefg")
        .compose(toLengthFun)
        .subscribe(System.out::println);
```
람다식을 이용하여 문자열의 길이를 계산하는 transformer 사용예제 사실 map 만 써도 된다  
ㄴ 그냥 한번 만들어본것 ...  
  
### RxJava 사용의 전재 조건 및 이점 
RxJava 를 이용하여 리액티브 프로그래밍 기초를 다루어봤다  
다른 리액티브 라이브러리들은 API 도 조금씩 다르고 구현 방식도 다양함  
하지만 구독자가 관찰 가능한 스트림을 구독한 후, 비동기적으로 이벤트를 생성해 프로세스를 시작한다는 핵심 개념은 동일하다  
프로듀서와 구독자 사이에는 프로듀서-컨슈머 관계를 해지할 수 있는 채널이 일반적으로 존재함  
이러한 채널의 개념은 이벤트의 양을 제어할 수 있게 해준다  
  
리액티브 프로그래밍이 리소스를 절약할 수 있다는 것을 입증하기 위해서 간단한 메모리 검색 엔진 서비스를 구현한다고 가정해보자  
검색의 결과로 URL 컬랙션을 반환하는 기능을 구현한다고 해보자  
리액티브 프로그래밍이 아닌 경우 일반적으로 아래와 같이 페이징 제한을 구현한다  
```java
public interface SearchEngine {
    List<URL> search(String query, int limit);
}
```
search() 메소드는 검색을 수행하고 limit 만큼의 결과를 List 에 담아서 반환하도록 구현되어 있다 
위 메소드는 사용자가 두 번째 페이지를 요청하면 첫 번째 페이지와 두 번째 페이지에 해당하는 모든 데이터를 반환하도록 구성된 코드이다 
이러한 방식은 limit 개수가 커지거나 n 번째 페이지를 요청하게되면 자원의 낭비가 심하다는 단점이 있음  

limit 개수를 고정 크기로 제한하고 **커서** 개념을 도입해서 아래와 같이 개선할 수 있다  
자바 클라이언트에서는 보통 Iterator 를 이용하여 커서를 구현할 수 있다  
```java
public interface IterableSearchEngine {
    Iterator<URL> search(String query, int limit);
}
```
Iterator 인터페이스의 단점은 다음 데이터 반환을 기다릴 때 blocking 된디는 점이다  
안드로이드 UI 스레드일 때는 이는 치명적일 수 있음  

blocking 을 방지하기 위해서 CompletableFuture 를 반환하는 비동기 서비스를 만들어보자  
```java
public interface FutureSearchEngine {
    CompletableFuture<List<URL>> search(String query, int limit);
}
```
클라이언트의 스레드는 결과가 도착하자마자 콜백을 호출하기 때문에 검색 요청에 아무런 부담이 없다  
ComputableFuture 가 List 를 반환하더라도 한번에 전체를 반환하거나 아무것도 반환하지 않는 방식으로 동작한다  
  
여기서 우리는 RxJava 솔루션을 이용해 개선해보자 
비동기 처리 이후에 수신하는 각 이벤트에 대응할 수 있는 능력을 갖출수 있다 
클라이언트는 언제든지 구독을 취소할 수 있으며 서비스 검색에 필요한 작업량을 줄일 수 있다  
```java
public interface RxSearchEngine {
    Observable<URL> search(String query, int limit);
}
```
이러한 접근 방식은 응답성(리액티브의 목적)을 크게 높여준다 
클라이언트가 아직 모든 결과를 수신하지 못한 상태에서도 이미 도착한 부분에 대한 처리를 시작할 수 있다  
Time To First Byte(최초 데이터 수신 시간) 과 Critical Rendering Path(주요 랜더링 경로) 매트릭으로 성능을 평가한다   
ㄴ [Time To Fisrt Byte](https://blog.stackpath.com/time-to-first-byte/)
ㄴ [Cricitical Rendering Path](https://developers.google.com/web/fundamentals/performance/critical-rendering-path)
  
RxJava 를 이용하면 훨씬 융통성 있고 유연한 방식으로 비동기 데이터 스트림을 구성 가능   
기존의 동기방식의 코드를 비동기 워크 플로우로 wrapping 할 수 있음  
느린 Callable 에 대한 실행 스레드를 관리하기 위해 susbscribeOn(Scheduler) 연산자를 사용가능 
```java
@Test
public void testSchedulers() throws InterruptedException {
    String query = "query";
    Observable.fromCallable(() -> doSlowSyncRequest(query))
            .subscribeOn(Schedulers.io())
            .subscribe(this::processResult);

    Thread.sleep(1000);
}

private String doSlowSyncRequest(String query) {
    return "result";
}

private void processResult(String result) {
    System.out.println(Thread.currentThread().getName() + ": " + result);
}
```
이러한 접근방식은 한 개의 스레드가 전체 요청을 처리한다는 것을 신뢰할 수 없게 된다  
ㄴ Schedulers 때문인가?

한 스레드에서 시작해 소수의 다른 스레드로 이동하고, 다른 스레드에서 처리가 완료될 수 있기 때문에 객체를 mutate 하는것은 안전하지 않다  
이를 해결하기 위한 합리적은 방법은 immutable object(불변 객체)를 이용하는 방법이다  
불변 객체는 함수형 프로그래밍의 핵심 원리중 하나로써 만들어지면 변하지 않는 객체를 말한다   
불변 객체라는 간단한 규칙이 병렬 프로그래밍에서의 모든 종류의 문제를 예방할 수 있다  
  
>
자바 1.8 이전에는 람다를 지원하지 않았기 때문에 리액티브 프로그래밍이나 함수형 프로그래밍을 구현하는데 어려움이 있었다  
넷플릭스는 느린속도에도 불구하고 오직 람다 지원 때문에 그루비(Groovy) 를 광범위하게 사용했다  
이런 넷플릭스 사례를 보면 알 수 있듯이 리액티브 프로그래밍을 쾌적한 사용을 위해서는 1급 객체로서 함수사용이 필요하다는 결론에 이르렀다고 함 -> 필자 


### RxJava 를 이용해서 어플리케이션 다시 만들기 
이전에 작성했던 온도 감지 어플리케이션을 RxJava 로 다시 만들어보자 

gradle 디펜던시 추가 
```groovy
    compile("io.reactivex:rxjava:1.3.8")
```

### 기존에 사용했던 온도를 나타내는 클래스 
```java
public class Temperature {
    private final double value;
    // constructor & getter
}
```

### 비지니스 로직 구현 
TemperatureSensor 클래스는 앞에서 스프링 ApplicationEventPublisher 에 이벤트를 보냈지만, 이제는 Temperautre 이벤트가 있는 리액티브 스트림을 반환해야 한다  
TemperatureSensor 의 리액티브 구현 
```java
@Component
public class TemperatureSensor {
    private final Random rnd = new Random();

    // data stream 정의 (Observable stream)
    private final Observable<Temperature> dataStream = Observable
            .range(0, Integer.MAX_VALUE) // 스트림 생성
            // 변환 작업 Integer -> Temperature 결합
            // public final <R> Observable<R> concatMap(Func1<? super T, ? extends Observable<? extends R>> func)
            .concatMap(tick ->
                    Observable.just(tick) // Observable 스트림으로 변환
                    .delay(rnd.nextInt(5000), TimeUnit.MILLISECONDS) // random delay
                    .map(tickValue -> this.probe()) // Temperature
            )
            // publish 로 대상 스트림으로 브로드 캐스팅 
            .publish()
            // 스트림에 하나 이상의 구독자가 있을 때에만 스트림이 시작하도록 
            .refCount();

    private Temperature probe() {
        return new Temperature(16 + rnd.nextGaussian() * 10);
    }

    public Observable<Temperature> temperatureStream() {
        return dataStream;
    }
}
```
TemperatureSensor 는 dataStream 을 생성하고 temperatureStream 메소드를 통해서 스트림을 반환  
dataStream 은 0부터 integer max value 까지 시퀀스를 생성하며, concatMap 메소드를 이용해 Temperature 를 Wrapping 한 Observable<Temperature> 를 반환한다  
publish, refCount 가 없으면 클라이언트는 매번 새로운 시퀀스를 생성하며 새로운 구독을 시작한다. 이는 센서 결과를 구독자들이 공유하지 않음을 의미함  
따라서 publish 로 대상 스트림으로 브로드 캐스팅하고 publish 는 ConnectableObservable 라는 특별한 타입의 Observable 를 반환  
ConnectableObservable 는 하나 이상의 구독자가 있을 때만 입력 공유 스트림을 생성하는 refCount() 연산자를 제공  
refCount() 로 구독자가 없을 때에는 센서를 탐색하지 않도록 할 수 있다  


TemperatureSensor 테스트 
```java
@SpringBootTest
class TemperatureSensorTest {

    @Autowired
    private TemperatureSensor temperatureSensor;

    @Test
    void temperatureStream() throws InterruptedException {
        temperatureSensor.temperatureStream()
                .subscribe(System.out::println);

        // 최대 5초간 대기 하기 때문에 5초이상 설정
        Thread.sleep(10000);
    }
}
```
결과 
```
Temperature{value=9.864493092921105}
Temperature{value=6.647432974817892}
Temperature{value=7.804728182329336}
Temperature{value=11.30079931206535}
Temperature{value=18.153529701211674}
Temperature{value=15.837433814726952}
```

### Custom SseEmitter
TemperatureSensor 를 사용해 새로운 SseEmitter 를 Observable 스트림에 구독하고 수신한 이벤트를 SSE 클라이언트에 전송해보자  
```java
public class RxSeeEmitter extends SseEmitter {
    static final long SSE_SESSION_TIMEOUT = 30 * 60 * 1000L;
    private final Subscriber<Temperature> subscriber; // 구독자 캡슐화

    RxSeeEmitter() {
        super(SSE_SESSION_TIMEOUT);
        // 구독자 생성 
        this.subscriber = new Subscriber<Temperature>() {
            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable e) { }

            @Override
            public void onNext(Temperature temperature) {
                try {
                    // Sse 클라이언트로 전송 
                    RxSeeEmitter.this.send(temperature);
                } catch (IOException ioe) {
                    unsubscribe();
                }
            }
        };

        // 세션 완료, 타임 아웃에 대한 처리 
        onCompletion(subscriber::unsubscribe);
        onTimeout(subscriber::unsubscribe);
    }

    Subscriber<Temperature> getSubscriber() {
        return subscriber;
    }
}
```
RxSeeEmitter 는 SseEmitter 를 상속하고 내부적으로 구독자를 생성한다  
구독자는 Temperature 가 도착하면 해당 내용을 Sse 클라이언트로 전송함  
여기서는 onCompleted, onError 를 구현하지 않았지만 실제 상황에서는 꼭 처리를 추가해주어야 한다  

### SSE 엔드 포인트 노출 시키기 
RxSeeEmitter 를 노출시키기 위해서 TemperatureSensor 인스턴스를 함께 구성할 REST 컨트롤러를 작성하자  
```java
@RestController
public class TemperatureController {
    private final TemperatureSensor temperatureSensor;
    
    @Autowired
    public TemperatureController(TemperatureSensor temperatureSensor) {
        this.temperatureSensor = temperatureSensor;
    }
    
    @RequestMapping(
            value = "/temperature-stream",
            method = RequestMethod.GET)
    public SseEmitter events(HttpServletRequest request) {
        RxSeeEmitter emitter = new RxSeeEmitter();
        
        temperatureSensor.temperatureStream()
                .subscribe(emitter.getSubscriber());
        return emitter;
    }
}
```
위 코드에서 볼 수 있듯이 REST 컨트롤러는 적은 로직을 유지하고 구독이 해제된 SseEmitter 인스턴스를 관리하지 않으며 동기화에 신경쓰지 않는다  
TemperatureSensor 는 온도 값 측정 및 이벤트 발행을 관리  
RxSeeEmitter 는 리액티브 스트림을 출력용 SSE 메시지로 변환하고 TemperatureController 는 새로운 SSE 세션을 온도 측정 스트림을 구독한 새로운 RxSseEmitter 에만 바인딩한다  
  
### 스프링 부트 어플리케이션 설정 
이러한 접근법으로 @EventListener 어노테이션을 사용하지 않으므로 Async 에 대한 의존성이 없어져 어플리케이션 구성이 간단해진다  
```java
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```

RxJava 기반 구현으로 **아무도 구독하지 않을때에는 온도센서를 탐색하는 오버헤드가 없음** 에 주목하자  
이것은 **리액티브 프로그래밍이 가지는 능동적 구독이라는 개념** 의 자연스러운 결과다  
일반적인 발행-구독의 구현에서는 이러한 속성이 없으며 더 제한적임  

# 리액티브 라이브러리의 간단한 역사 
RxJava 를 포함한 오늘날 우리가 알고 있는 리액티브 프로그래밍은 MS 내부에서 시작되었음  
2005년 에릭 마이어(Erik Meijer)와 Cloud Programmability Team은 대규모 비동기 및 데이터 집약적 인터넷 서비스 아키텍쳐를 구축하는데 적합한 프로그래밍 모델을 실험하고 있었음  
2007년 Rx 라이브러리의 첫 번째 버전이 탄생  
2009년 멀티 스레딩과 협업 스케줄링을 포함한 라이브러리를 추가후 MS Rx.NET 의 첫번째 공개버전을 출시함  
그 이후 MS 는 Rx 라이브러리를 자바스크립트, C++, 루비, 오브젝트-C 같은 언어와 윈도우 폰 플랫폼에 이식했고 Rx 가 인기를 얻자 오픈소스로 공개함  
  
당시의 넷플릭스는 엄청난 양의 인터넷 트래픽을 처리하는 매우 복잡한 문제에 직면해 있었고 RxJava 를 도입한다
그 이후 넷플릭스는 상당부분의 트래픽을 RxJava 를 이용해서 처리하고 있다 
넷플릭스는 엄청난 부하를 견디기 위해서 새로운 아키텍쳐 패턴을 만들어 라이브러리를 구현했고 대표적인 것들은 아래와 같다  
- [Hystrix](https://github.com/Netflix/Hystrix): 격벽(bulkhead) 패턴 서비스를 위한 결함 내성 라이브러리
- [Ribbon](https://github.com/Netflix/ribbon): 로드밸런서를 지원하는 RPC 라이브러리
- [Zuul](https://github.com/Netflix/zuul): 동적 라우팅, 보안, 복원력, 모니터링 기능을 제공하는 게이트웨이 서비스 
- [RxNetty](https://github.com/ReactiveX/RxNetty): NIO 클리언트-서버 프레임워크인 네티의 리액티브 어뎁터 
  
오늘날 Rxjava 는 Couchbase 및 MongoDB 와 같은 일부 NoSQL 자바드라이버에서 사용된다  


# 리액티브의 전망 
이번 장에서는 스프링과 Rxjava 를 결합해서 사용하는 것을 배웠다
하지만 다른 조합도 가능하다는것을 잊어서는 안된다  

잘 알려진 리액티브 서버인 [Ratpack](https://ratpack.io/manual/current/all.html) 도 Rxjava 를 채택하기로 결정했다  
콜백 및 약속 기반의 Ratpack 은 Ratpack Promise 와 RxJava Observable 을 쉽게 양방향으로 변환해주는 모듈인 RxRatpack 을 제공한다  
```java
Promise<String> promise = get(() -> "hello world");
RxRatpack
    .observe(promise)
    .map(String::toUpperCase)
    .subscribe(context::render);
```

안드로이드에서 또 다른 유명한 예는 HTTP 클라이언트 Retrofit  
Retrofit 은 Futures 와 Callback 의 구현을 위주로 RxJava 래퍼를 생성한다 

JVM 세계에는 리액티브 라이브러리를 구현한 리액티브 서버 Vert.x 가 있다 
[Vert.x](https://vertx.io/docs/) 는 일정 기간 동안 콜백 기반 통신만 사용했지만, stream 기반 자체 솔루션을 만들어냈다 
- ReadStream<T> : 읽을 수 있는 스트림
- WrtieStream<T> : 쓸 수 있는 스트림 
- Pump: ReadStream 에서 WriteStream 으로 데이터를 이동하고 흐름 제어를 수행
```java
public void vertexExample(HttpClientRequest request, AsyncFile file) {
    request.setChuncked(true);
    Pump pump = Pump.pump(file, request);
    file.endHandler(v -> request.end());
    pump.start();
}
```
이클립스 Vert.x 는 Node.js 와 디자인 면에서 비슷한 이벤트 기반 어플리케이션 프레임워크  
단순한 동시성 모델과 비동기 프로그래밍의 기본 요소, 브라우저 내 자바스크립트에 직접 접근하는 분산 이벤트 버스를 제공한다  


리액티브 라이브러리의 동작은 일반적으로 비슷하지만, 세부 구현은 조금씩 다르다 
이러한 상황은 수정하기 어려운 숨겨진 버그로 프로젝트에 문제를 초래할 수 있기 때문에, 하나의 어플리케이션에서 여러가지 리액티브 라이브러리를 동시에 사용하는 것은 바람직하지 않다  
이러한 문제 때문에 **리액티브 스트림** 이라는 표준이 이미 설계되어 있으며, 다음장에서 다룰것이다  
