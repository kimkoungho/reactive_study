# 리액티브 어플리케이션 테스트 하기 

## 리액티브 스트림을 테스트 하기 어려운 이유
리액티브 스트림은 여러 장점이 있는데도 불구하고 비동기 통신을 이용하기 때문에 코드를 테스트하기 어려움이 있음   
앞에서 배웠듯 리액티브 스트림은 Publisher 로 스트림을 publish 하고 Subscriber 로 스트림을 구독하는 구조 전체를 테스트 할 수 있으나   
이런 통합 테스트만 작성하는 것은 너무 어려움   
  
리액터 팀은 리액트 스트림을 테스트하기 위한 솔루션을 제공하고 있음    

## StepVerifier 를 이용한 리액티브 스트림 테스트 
reactor-test 모듈에 포함된 StepVerifier 를 이용하면 모든 Publisher 에 대해서 테스트를 위한 플로우를 작성할 수 있음  


#### StepVerifier 기본 사용 예제 
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

#### StepVerifier 의 expectNextCount 예제
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

#### StepVerifier 에서 Hamcrest 를 이용한 검증 
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
            // consumeRecordedWith 을 사용하기 위해서는 recordWith 이 선행되어야 함 
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            // publisher 보낸 모든 원소에 대해서 검증을 수행 
            .consumeRecordedWith(wallets ->
                    // hamcrest 를 이용한 검증 
                    assertThat(wallets, everyItem(hasProperty("owner", equalTo("admin")))))
            .expectComplete()
            .verify();
}
```
- recordWith(supplier) : supplier 를 이용하여 스트림의 원소를 collection 으로 생성하는 메소드 
```java
Step<T> recordWith(Supplier<? extends Collection<T>> supplier);
```
위 예제에서는 ArrayList 를 사용하고 있지만 만약 스레드 safe 해야 한다면 ConcurrentLinkedQueue 를 사용해야 함  
- consumeRecordedWith(consumer) : recordWith 으로 반환된 결과들에 대해서 consumer 를 이용하여 검증을 수행  
```java
Step<T> consumeRecordedWith(Consumer<? super Collection<T>> consumer);
```

#### StepVerifier 의 expectNextMatches 
```java
@Test
public void expectNextMatches_test() {
    StepVerifier.create(Flux.just("alpha-foo", "betta-bar"))
            .expectSubscription()
            .expectNextMatches(e -> e.startsWith("alpha"))
            .expectNextMatches(e -> e.startsWith("betaa"))
            .expectComplete()
            .verify();
}
``` 
- expectNextMatches(predicate) : predicate 를 이용하여 검증
expectNext() 메소드는 내부적으로 equals() 를 이용하지만 expectNextMatches()는 predicate 를 구현함으로써 유연하게 적용할 수 있음  
```java
Step<T> expectNextMatches(Predicate<? super T> predicate);
```
 
#### StepVerifier 의 assertNext
```java
@Test
public void assertNext_test() {
    Publisher<Wallet> walletPublisher = Flux.just(new Wallet("admin"), new Wallet("user"), new Wallet("manager"))
            .filter(wallet -> wallet.getOwner().equals("admin"));

    StepVerifier.create(walletPublisher)
            .expectSubscription()
            .assertNext(wallet -> assertThat(
                    wallet,
                    hasProperty("owner", equalTo("admin"))
            )).expectComplete()
            .verify();
}
```
- assertNext(assertionConsumer) : assert 를 발생시키는 consumer 를 정의
expectNext() 는 true/false 를 반환하는 반면 assertNext 는 반환 값 없이 assertionConsumer 내부의 검증로직을 작성 
```java
default Step<T> assertNext(Consumer<? super T> assertionConsumer)
```  

#### StepVerifier 의 assertError
```java
@Test
public void assertError_test() {
    // securityService.login
    StepVerifier.create(Flux.error(new BadCredentialsException("error")))
            .expectSubscription()
            .expectError(BadCredentialsException.class)
            .verify();
}
```
- expectError(Throwable) : 에러가 발생할 것을 검증하기 위한 메소드  
ㄴ Flux.error 로 에러를 발생시켜 expectError 로 에러를 검증  

### StepVerifier 를 이용한 고급 테스트 

#### 구독 취소 
Publisher 테스트의 가장 처음 단계는 무한 스트림인지를 검사하는 것  
위에서 언급 했지만 무한 스트림의 경우는 expectComplete 으로 검증할 수 있다  
  
하지만 무한 스트림을 테스트 해야 한다면 ?  
verify 는 blocking 메소드이기 때문에 테스트가 종료되지 않는 문제가 발생  
이를 위해서 구독을 취소하는 메소드를 제공함  
```java
@Test
public void thenCancel_test() {
    StepVerifier.create(Flux.interval(Duration.ofSeconds(5)))
            .expectSubscription()
            .expectNext(0L)
            .expectNext(1L)
            .thenCancel()
            .verify();
}
```
- thenCancel : 해당 메소드를 이용해서 구독을 취소 할 수 있음  
ㄴ interval 로 무한 스트림을 생성하고 thenCancel 로 구독을 취소할 수 있음  
```java
StepVerifier thenCancel();
``` 

#### backpressure 확인 
시스템에서 선택한 backpressure 전략으로 시스템이 잘 동작하는지 검증  
  
웹 소켓을 통한 외부 시스템을 호출한다고 가정해보자  
```java
Flux<String> websocketPublisher = ...
Class<Exception> expectedErrorClass = eactor.core.Exceptions.failWithOverflow().getClass();


StepVerifier.create(websocketPublisher.onBackpressureBuffer(5), 0)
        .expectSubscription()
        .thenRequest(1)
        .expectNext("Connected")
        .thenRequest(1)
        .expectNext("Price: $12.00")
        .expectError(expectedErrorClass)
        .verify();
    
```  
- onBackpressureBuffer 로 다운 스트림을 보호할 수 있음
- create(publisher, n) : 초기 구독자가 요청하는 item 개수 n 을 지정할 수 있음 
- thenRequest(n) : 구독자의 요청 수량을 제어   
ㄴ thenRequest 를 사용할 경우 overflow 가 발생하여 에러를 만날 수 있음
```java
// Flux
public final Flux<T> onBackpressureBuffer(int maxSize) {...}

// StepVerifier.Step
static <T> FirstStep<T> create(Publisher<? extends T> publisher, long n) 
Step<T> thenRequest(long n)
```  

#### TestPublisher 사용하기 
TestPublisher 는 Publisher 의 구현체로 테스트를 위해서 onNext(), onComplete(), onError() 를 직접 구동할 수 있음  
  
walletRepository.findAllById() 를 검증하기 위한 테스트  
```java
TestPublisher<String> idsPublisher = TestPublisher.create();

StepVerifier.create(walletRepository.findAllById(idsPublisher))
        .expectSubscription()
        .then(() -> idsPublisher.next("1"))
        .assertNext(w -> assertThat(w, hasProperty("id", equalTo("1"))))
        .then(() -> idsPublisher.next("2"))
        .assertNext(w -> assertThat(w, hasProperty("id", equalTo("2"))))
        .then(idsPublisher::complete)
        .expectComplete()
        .verify();
```
- StepVerifier.then(Runnable) 에 TestPublisher.next() 를 이용하여 item 을 전달  
- StepVerifier.then(Runnable): 이전 step 이 확인 된 후에만 요청을 보냄  
- TestPublisher.next(value): value 를 publish
```java
// StepVerifier
Step<T> then(Runnable task);
// TestPublisher
public abstract TestPublisher<T> next(@Nullable T value);
```

### 가상 시간 다루기 
시간을 다루는 리액티브 스트림이 있다고 가정해보자  
```java
public Flux<String> sendWithInterval() {
    return Flux.interval(Duration.ofMinutes(1))
            .zipWith(Flux.just("a", "b", "c"))
            .map(Tuple2::getT2);
}

// test
@Test
public void sendWithInterval_test() {
    StepVerifier.create(sendWithInterval())
            .expectSubscription()
            .expectNext("a", "b", "c")
            .expectComplete()
            .verify();
}
```
- sendWithInterval() 은 1분 마다 publish 하기 때문에 테스트에 3분이나 걸리는 문제점이 있음  
- 이러한 문제를 해결하기 위해서 리액터 테스트 모듈은 가상 시간이라는 개념을 제공  

#### VirtualTimeScheduler 예제 
```java
@Test
public void sendWithInterval_virtual_time_test() {
    StepVerifier.withVirtualTime(() -> sendWithInterval())
            .expectSubscription()
            .then(() ->
                    VirtualTimeScheduler.get().advanceTimeBy(Duration.ofMinutes(3))
            )
            .expectNext("a", "b", "c")
            .expectComplete()
            .verify();
}
```  
- StepVerifier.withVirtualTime(scenarioSupplier) 를 이용하여 리액터 테스트가 가상 스케줄러(VirtualTimeScheduler)로 모든 스케줄러를 대체  
- then 메소드에서 VirtualTimeScheduler 를 이용한 시간 제어를 할 수 있음 
- VirtualTimeScheduler.get() : 현재 등록된 가상 스케줄러 반환 
- advanceTimeBy(Duration) : Duration 에 제공된 시간만큼 가상 시간을 이동시킴  
```java
// StepVerifier
static <T> FirstStep<T> withVirtualTime(Supplier<? extends Publisher<? extends T>> scenarioSupplier)
// VirtualTimeScheduler
public static VirtualTimeScheduler get()
public void advanceTimeBy(Duration delayTime) 
```

#### thenAwait() 을 이용하여 코드 깔끔하게 수정
```java
@Test
public void sendWithInterval_thenAwait_test() {
    Duration took = StepVerifier.withVirtualTime(() -> sendWithInterval())
            .expectSubscription()
            .thenAwait(Duration.ofMinutes(3))
            .expectNext("a", "b", "c")
            .expectComplete()
            .verify();
    
    System.out.println("Verification time: " + took);
}
```
- thenAwait(Duration) 을 이용하여 VirtualTimeScheduler 호출 내용을 StepVerifier 에게 위임
- verify() 로 실제로 검증에 사용된 시간을 반환 받을 수 있음 
```java
Step<T> thenAwait(Duration timeshift)
``` 

thenAwait() : 파라미터 없는 메소드를 이용하면 현재 가상 시간 이전에 실행하도록 예약된 모든 작업을 실행 
```java
@Test
public void thenAwait_test() {
    StepVerifier.withVirtualTime(() ->
            Flux.interval(Duration.ofMillis(0), Duration.ofMillis(1000))
                .zipWith(Flux.just("a", "b", "c")).map(Tuple2::getT2)
            ).expectSubscription()
            .thenAwait() // 바로 실행
            .expectNext("a")
            .thenAwait(Duration.ofMillis(1000))
            .expectNext("b")
            .thenAwait(Duration.ofMillis(1000))
            .expectNext("c")
            .expectComplete()
            .verify();
}
```

#### expectNoEvent
지정된 시간동안 이벤트가 일어나지 않음을 테스트 하기위해서 expectNoEvent() 이용  
```java
@Test
public void sendWithInterval_expectNoEvents_test() {
    StepVerifier.withVirtualTime(() -> sendWithInterval())
            .expectSubscription()
            .expectNoEvent(Duration.ofMinutes(1))
            .expectNext("a")
            .expectNoEvent(Duration.ofMinutes(1))
            .expectNext("b")
            .expectNoEvent(Duration.ofMinutes(1))
            .expectNext("c")
            .expectComplete()
            .verify();
}
```

### 리액티브 컨텍스트 검증하기  
인증 서비스의 리액티브 API 를 검증한다고 가정해보자   
securityService 의 login 을 호출하면 클라이언트는 인증 관련된 context 를 반환할 것이라고 기대한다  
ㄴ context: 비동기로 동작하는 리액티브 프로그래밍에서는 스레드 상의 공용 데이터 관리가 어렵기 때문에, ThreadLocal 과 유사한 Context API 를 제공함 
ㄴ [참고 링크](https://godekdls.github.io/Reactor%20Core/advancedfeaturesandconcepts/#98-adding-a-context-to-a-reactive-sequence)
```java
StepVerifier.create(securityService.login("admin", "admin"))
    .expectAccessibleContext()
    .hasKey("security")
    .then()
    .expectComplete()
    .verify();
``` 
- expectAccessibleContext() 는 ContextExpectations 을 반환하는데 Context 관련 검증 시작할때 사용함   
ㄴ 해당 메소드가 실패하는 경우는 publisher 가 Reactor 타입(Mono, Flux) 을 반환하지 않는 경우  
- ContextExpectations.hasKey("security") : context 에 해당 key 가 있는지 검증 
- ContextExpectations.then() : context 검증을 종료하고 원래 시퀀스 검증으로 돌아감 

## webflux 테스트 
웹 플럭스 어플리케이션 검증을 위해서 도입된 추가기능 설명  
- 모듈의 호환성, 어플리케이션 무결성, 통신 프로토콜, 외부 API 및 클라이언트 라이브리러 검증
- 컴포넌트 단위의 통합 테스트라고 생각하면됨 

### WebTestClient 를 이용한 컨트롤러 테스트 
결제 서비스를 테스트한다고 가정해본다  
결제 서비스 구조 
- PaymentController 
- PaymentService 
- PaymentRepository

#### PaymentController  
```java
@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService service) {
        paymentService = service;
    }

    @GetMapping("")
    public Flux<Payment> list() {
        return paymentService.list();
    }

    @PostMapping("")
    public Mono<String> send(Mono<Payment> payment) {
        return paymentService.send(payment);
    }
}
```
spring-test 모듈에는 MockMvc 와 유사한 WebTestClient 클래스가 새롭게 추가됨  
WebTestClient 를 이용해서 web flux 엔드 포인트를 테스트 할 수 있음  

```java
@Test
public void verifyRespondWithExpectedPayment() {
    PaymentService paymentService = Mockito.mock(PaymentService.class);
    PaymentController paymentController = new PaymentController(paymentService);
    
    Payment[] payments = new Payment[]{new Payment("1", "a"), new Payment("2", "b"), new Payment("3", "c")};
    BDDMockito.given(paymentService.list())
            .willReturn(Flux.just(payments));

    WebTestClient
            .bindToController(paymentController)
            .build()
            .get()// HTTP method
            .uri("/payments")
            .exchange()// API call
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectStatus().is2xxSuccessful()
            .returnResult(Payment.class) // result class
            .getResponseBody() // Flux<Payment> 반환  
            .as(StepVerifier::create) // StepVerifier 를 이용한 검증 시작 
            .expectNextCount(3)
            .expectComplete()
            .verify();
}
```  
- 위 예제에서는 PaymentController 를 테스트 하기 위해서 PaymentService 를 Mock 객체로 만들어 list 메소드를 stubbing 함  
ㄴ Mock 을 이용한 단위 테스트를 작성한 이유는 WebTestClient 사용방법을 간단히 보여주기 위함인듯  
- WebTestClient 로 모든 HTTP 요청을 수행 가능 
- WebTestClient 는 MockMvc 방식의 테스트에서 TestRestTemplate 과 같은 역할 수행  

통합 테스트는 어떻게 할까?  
우선 PaymentService 로직을 살펴보자 

### PaymentService
```java
@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final WebClient client;


    public PaymentService(PaymentRepository paymentRepository, WebClient.Builder builder) {
        this.paymentRepository = paymentRepository;
        // fake url
        this.client = builder.baseUrl("http://api.bank.com/submit")
                .build();
    }

    public Mono<String> send(Mono<Payment> payment) {
        return payment.zipWith(
                ReactiveSecurityContextHolder.getContext(),
                (p, c) -> p.withUser(c.getAuthentication().getName())
        )
                .flatMap(p -> client.post()
                        .syncBody(p)
                        .retrieve()
                        .bodyToMono(String.class)
                        .then(paymentRepository.save(p)))
                .map(Payment::getId);
    }


    public Flux<Payment> list() {
        return ReactiveSecurityContextHolder
                .getContext()
                .map(SecurityContext::getAuthentication)
                .map(Principal::getName)
                .flatMapMany(paymentRepository::findAllByUser);
    }
}
```
- send() : 결제를 처리하는 로직 
- list() : 유저의 결제 목록을 반환하는 로직
send() 메소드를 테스트한다고 가정할때, DB 부분은 임베디드 Mongo DB 를 사용하여 실제 데이터가 저장되는 것을 방지 할 수 있다  
하지만, 외부 시스템을 호출하는 부분은 그럴 수 없기 때문에 mocking 을 이용해야 함    
이를 위해서 [WireMock](http://wiremock.org) 와 같은 도구로 외부 서비스를 mocking 할 수 있음  
ㄴ WireMock : 일종의 mock 웹 서버로 지정한 규칙에 매칭된 요청이 들어오면 지정한 응답을 줄 수 있음  
 
스프링 부트 2.0 과 스프링 프레임 워크에서는 WebClient 의 HTTP 요청에 대한 mocking 은 지원하지 않음  
강제로 mocking 하는 트릭이 있는데, WebClient.Builder 를 사용해서 ExchangeFunction 을 mocking 하는 것  
```java
@TestConfiguration
public class TestWebClientBuilderConfiguration {

    @Bean
    public WebClientCustomizer testWebClientCustomizer(ExchangeFunction exchangeFunction) {
        return builder -> builder.exchangeFunction(exchangeFunction);
    }
}
``` 
- 테스트 설정을 이용하여 ExchangeFunction 에 대한 stubbing 이 가능

완성된 테스트 코드 
```java
@ImportAutoConfiguration({
        TestSecurityConfiguration.class, // 스프링 시큐리티 관련 test 설정 
        TestWebClientBuilderConfiguration.class // WebClient 테스트 설정
})
@RunWith(SpringRunner.class)
@WebFluxTest // 자동 설정 비활성화 
@AutoConfigureWebTestClient
class PaymentControllerTest {

    @Autowired
    WebTestClient client;
    
    @MockBean
    ExchangeFunction exchangeFunction;

    @Test
    public void verifyPaymentsWasSentAndStored() {
        Mockito.when(exchangeFunction.exchange(Mockito.any()))
                .thenReturn(Mono.just(MockClientResponse.create(201, Mono.empty())));

        client.post()
                .uri("/payments/")
                .syncBody(new Payment())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .returnResult(String.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNextCount(1)
                .expectComplete()
                .verify();

        Mockito.verify(exchangeFunction).exchange(Mockito.any());
    }

}
```
- @MockBean 으로 ExchangeFunction 을 mock 으로 IOC 컨테이너에 DI 
- ExchangeFunction 의 결과를 mockito 를 이용해서 결과를 stubbing 함  
ㄴ 자세히 설명되어 있지는 않지만 트릭을 쓰기 위해서 WebClient 의 반환 값인 ClientResponse 을 implements 한 MockClientResponse 을 구현했음 
ㄴ 테스트 하기위해서 너무 번거로워 보인다 ..  
- 결과적으로 **WireMock** 을 이용하라고 함   

### 웹소켓 테스트 



 


