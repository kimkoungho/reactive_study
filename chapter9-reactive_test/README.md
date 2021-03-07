# 리액티브 어플리케이션 테스트 하기 

## 리액티브 스트림을 테스트 하기 어려운 이유
리액티브 스트림은 여러 장점이 있는데도 불구하고 비동기 통신을 이용하기 때문에 코드를 테스트하기 어려움이 있음   
앞에서 배웠듯 리액티브 스트림은 Publisher 로 스트림을 publish 하고 Subscriber 로 스트림을 구독하는 구조 전체를 테스트 할 수 있으나   
이런 통합 테스트만 작성하는 것은 너무 어려움   
  
리액터 팀은 리액트 스트림을 테스트하기 위한 솔루션을 제공하고 있음    

## StepVerifier 를 이용한 리액티브 스트림 테스트 
reactor-test 모듈에 포함된 StepVerifier 를 이용하면 모든 Publisher 에 대해서 테스트를 위한 플로우를 작성할 수 있음  


### StepVerifier 기본 사용 예제 
```java
@Test
public void basic_test() {
    StepVerifier
            .create(Flux.just("foo", "bar"))
            .expectSubscription()
            .expectNext("foo")
            .expectNext("bar")
            .expectComplete()
            .verify();
}
```
- create(publisher) : StepVerifier 를 생성하는 factory 메소드  
```java
static <T> FirstStep<T> create(Publisher<? extends T> publisher) {...}
```
- expectSubscription : publisher 의 가장 처음 이벤트는 구독과 관련된 이벤트 여야 하기 때문에 구독이 시작되었다는 것을 검증 
```java
Step<T> expectSubscription();
``` 
- expectNext(value) : 스트릠의 다음 값이 value 와 일치하는지 검증   
```java
Step<T> expectNext(T t);
```
- expectComplete : 스트림이 종료되 었다는 종료 시그널을 검증
```java
StepVerifier expectComplete();
```  
스트림이 종료되지 않았다면 검증에 실패한다  
ex) 위 예제에서 expectNext("bar") 를 호출하기 전에 expectComplete() 을 호출하면 에러 발생 
```log
java.lang.AssertionError: expectation "expectComplete" failed (expected: onComplete(); actual: onNext(bar))
```
- verify : 실제로 검증을 시작하는 메소드 (구독 시작을 의미)
```java
Duration verify() throws AssertionError;
``` 
해당 메소드는 blocking 메소드로 검증이 완료되기 전까지 테스트가 종료되지 않는다  

### StepVerifier 의 expectNextCount 예제
스트림에서 얼마나 많은 원소를 생산했는지 검증하려면 expectNextCount 메소드를 이용할 수 있다 
```java
@Test
public void expectNext_test() {
    StepVerifier.create(Flux.range(0, 100))
            .expectSubscription()
            .expectNext(0) 
            .expectNextCount(98)
            .expectNext(99)
            .expectComplete()
            .verify();
}
```
Flux.range(0, 100) 로 0 - 99 까지의 원소를 생성하는 스트림을 생성하고 expectNext(0) 로 첫번째 요소를 검증한다  
expectNextCount(98) 호출하여 스트림이 98개의 원소를 생성했는지 검증 후에 마지막 원소인 99 를 expectNext(99) 로 검증    
- expectNextCount(count) : 현재 스트림에서 다음에 올 원소의 수를 검증 
```java
Step<T> expectNextCount(long count);
``` 

### StepVerifier 에서 Hamcrest 를 이용한 검증 
expectNextCount 메소드를 이용해서 스트림의 요소 수를 검증할 수 있지만, 충분하지 않을 수 있음  
예를들어 스트림에 필터 조건이 걸려있는 경우 해당 필터가 제대로 동작했는지 검증이 필요할 수 있다  
이를 검증 하기 위해서 StepVerifier + Hamcrest 조합을 이용할 수 있다 

> [Hamcrest](http://hamcrest.org/JavaHamcrest) 는 테스트를 위한 프레임워크로 기존에 사용하던 matchers 보다 가독성 높은 matchers 을 제공한다
> Hamcrest 는 여러 종류의 API 를 제공하고 있지만, 책에서 나온 matcher 에 대해서만 가볍게 알고가자   
```java
public class MyUser {
    private String id;
    public void setId(String id) { this.id = id; }
    public String getId() { return id; }
}
@Test
public void bean_test() {
    MyUser myUser = new MyUser();
    myUser.setId("leo");

    // getter or setter 존재시 true
    assertThat(myUser, hasProperty("id"));
    // getter 존재하지 않으면 오류 
    assertThat(myUser, hasProperty("id", equalTo("leo")));
}
```
- assertThat(actual, matcher) : actual 를 matcher 를 이용해 검증 (assert)
ㄴ actual 은 검증을 수행할 대상 객체   
ㄴ matcher 는 검증을 수행할 matcher
- hasProperty(propertyName) : 해당 객체에 propertyName 에 해당하는 필드가 있는지 검사한다  
ㄴ 실제로 프로퍼티가 아닌 getter OR setter 메소드가 존재하는지 검사 
- hasProperty(propertyName, valueMatcher) : 해당 객체에 propertyName 에 해당하는 필드가 있는지 검사하고 valueMatcher 로 검증을 수행  
ㄴ 책에서 사용하는 matcher 

스트림의 필터가 제대로 동작했는지 검증하는 예제 
ㄴ 책의 예제가 완벽하지 않아서 코드를 약간 추가함  
```java
public class Wallet {
    private String owner;
    public Wallet(String owner) { this.owner = owner; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
}

@Test
public void filter_test() {
    // filter 가 적용된 publisher
    Publisher<Wallet> walletPublisher = Flux.just(new Wallet("admin"), new Wallet("user"), new Wallet("manager"))
            .filter(wallet -> wallet.getOwner().equals("admin"));

    StepVerifier.create(walletPublisher)
            .expectSubscription()
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(wallets ->
                    assertThat(wallets, everyItem(hasProperty("owner", equalTo("admin")))))
            .expectComplete()
            .verify();
}
```

 

### StepVerifier 를 이용한 고급 테스트 

### 가상 시간 다루기 

### 리액티브 컨텍스트 검증하기  

## webflux 테스트 

### WebTestClient 를 이용한 컨트롤러 테스트 

### 웹소켓 테스트 



 


