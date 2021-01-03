# 왜 리액티브 스프링인가 ? 

## 왜 리액티브인가 ?
웹 기반 온라인 상점을 만든다고 가정해보자.  
시간당 약 1,000 명의 사용자가 방문한다는 요구 사항에 따라서 톰캣 웹 서버에 500 개의 thread 로 thread pool 을 구성하면  
초당 평균 응답시간은 250 ms 로 초당 약 2,000 명의 사용자 요청을 처리 가능하다  
그런데 블랙 프라이데이에 고객의 요청이 폭발적으로 증가한다면 .. ? -> 트래픽을 감당하지 못해서 고객들은 다른 서비스를 이용하게 될 것이다 ..  
  
이러한 문제를 어떻게 해결할 것인가 ?  
문제를 해결하는 첫번째 방법은 Elasticity (탄력성) 을 갖는 서비스를 구축하는 것  
- Elasticity 은 다양한 작업의 부하에서 응답성을 유지하는 것을 의미

즉, 서비스는 더 많은 사용자가 요청할 때 시스템 처리량이 자동으로 증가하고 요청이 감소하면 자동으로 감소해야 한다 
Elasticity 가 없는 시스템에는 요청이 늘어나게 되면 자연적으로 Latency 가 증가하게 된다 
  
단순히 처리량을 늘리기 위해서 추가 자원, 인스턴스 등을 추가할 수도 있을 것이다.  
하지만 이 방법은 요청이 감소하면 모두 리소스 낭비가 된다  

> 수평적 수직적 확장을 통해서 탄력성을 달성할 수 있지만, 이것은 일반적으로 분산 시스템의 시스템 병목 지점 또는 동기화 지점을 확장하는것에 그친다  
> 이러한 문제는 암달의 법칙과 건터의 보편적 확장 모델로 설명할 수 있는데, 6장에서 소개함
> 암달의 법칙(Amdahl's Law) : 이 법칙은 병렬 처리가 가능한 시스템에서는 병렬 처리가 가능한 부분과 불가능한 순차 처리 되어야 하는 부분으로 나뉘기 때문에 프로세스 코어를 증가시켜서 시스템 성능 향상에는 한계가 있음을 알려주는 법칙 
> 건터의 보편적 확장 모델(Gunther Universal Scalability Model) : 암달의 법칙을 확장한 모델로 프로세스간 통신으로 인한 오버헤드를 설명함  
> [관련 블로그](https://wso2.com/blog/research/scalability-modeling-using-universal-scalability-law)
  
TODO:
연동한 결제 시스템이 장애가 났다고 가정해보자. 이런 경우    
시스템 실패에도 반응성을 유지할 수 있는 Resilience(복원력)을 유지해야한다 
이는 시스템의 기능 요소들을 격리해서 독립성을 확보해야 가능 
ㄴ 뒤에 나올 듯 


## 메시지 기반 통신 
분산 시스템에서 컴포넌트들을 연결 방법, 낮은 결합도, 시스템 격리 및 확장성 유지를 위해서 어떻게 해야 할까 ?  
  
우선 spring 에서 HTTP 를 이용한 통신 방법을 살펴보자  
```java
@RequestMapping("/resource")
public Object processRequest() {
    // spring 4 에서 사용되는 HTTP 요청을 위한 클라이언트 
    RestTemplate template = new RestTemplate();
    // 요청을 호출한다 
    ExampleCollection result  = template.getForObject("http://example.com/api/resource2", ExampleCollection.class);
    ...
    processResultFurther(result);
}
```
/resource 요청을 받으면 다른 서비스(http://example.com/api/resource2) 를 호출하여 해당 결과를 이용해서 응답을 생성하는 메소드를 선언했다  
위 코드에는 약간의 문제가 있는데, 아래 그림을 보자  
![rest template timeline](images/rest_template_timeline.png)  
위 그림에서 볼 수 있듯이 서비스1 은 서비스2 에서 응답이 오기 전까지는 해당 스레드가 blocking 되기 때문에 해당 스레드는 다른 요청을 처리 할 수 없다  
이것은 리소스 낭비이기 때문에 이런 문제를 해결하기 위해서 비동기 non-blocking 모델을 사용해야 한다  
      
이러한 비동기 non-blocking 은 일상 생활에서 쉽게 찾아 볼 수 있는데, 대표적으로 이메일, 문자 메시지 전송이다
message-driven 통신 원칙을 이용해서 구현 할 수 있으며, 이는 분산 시스템에서 서비스 간에 통신을 할 때 자원을 효율적으로 사용하기 위해서 필요하다     
분산 시스템에서 구성 요소들은 메시지 도착을 기다리고 이에 반응하며, 나머지 시간에는 휴식을 취하고 non-blocking 방식으로 메시지를 보낼 수 있어야 한다  
이러한 접근 방식은 각 구성 요소들의 위치 투명성을 제공하여 시스템의 확장성을 향샹 시키며, 특정 장비에 장애가 발생하더라도 수신자는 다른 장치를 통해 읽을 수 있기 때문에 복원력이 향상된다  
  
이런 message-driven 통신을 수행하는 방법 중에 message broker (메시지 브로커) 를 이용하는 방법이 있음  
ㄴ RabbitMQ, Kafka 등이 대표적임  

## 리액티브 시스템의 기본 원칙 
[리액티브 선언문](https://www.reactivemanifesto.org/en)는 리액티브 애플리케이션과 시스템 개발의 핵심 원칙을 공식적으로 정의한다  
![reactive](images/reactive-traits.svg)  
- Responsive (응답성) : 시스템이 가능한 한 사용자에게 즉시 응답하고 문제를 신속하게 탐지하고 대처할 수 있음을 의마한다. 응답성이 있는 시스템은 **일관성 있는 응답 시간을 제공**한다    
- Resilient (탄력성) : 시스템에 장애가 발생하더라도 응답성을 유지하는 것을 의미한다. 탄력성은 복제(replication), 격리(isolation), 위임(delegation) 으로 구현된다 
- Elastic (유연성) : 시스템의 작업 량이 변화하더라도 응답성을 유지하는 것을 유연성이라고 한다. 즉 요청의 증가, 감소에 따라서 자원을 증가시커나 감소 시킬 수 있음을 의미한다  
- Message Driven (메시지 구동) : 리액티브 시스템은 비동기 메시지 전달을 이용해서 구성 요소 간의 느슨한 결합, 격리, 위치 투명성을 지원할 수 있도록 경계를 명확히 정의할 수 있다.
    * 경계는 장애를 메시지로 전송하는 수단을 제공 -> 탄력성  
    * 명시적인 메시지 전달은 시스템에 메시지 큐를 생성하고 모니터링하여 배압(backpressure)을 제공 -> 유연성 
        
TODO:
> [Lightbend 의 설립자이자 CTO 인 Jonas Boner 가 리엑티브 선언문의 중요성에 대해서 설명한 글](https://www.lightbend.com/blog/why-do-we-need-a-reactive-manifesto) 


## 반응성에 대한 유즈 케이스 
이전에 얘기 했던 웹 스토어 어플리케이션에서 리액티브를 적용한 아키텍쳐를 소개한다  
![store application architecture](images/store_application_architecture.png)
위 그림을 보면 알 수 있듯이 API 게이트웨이 패턴을 사용하여 위치 투명성을 달성했다  
- API 게이트웨이 패턴
여러 클라이언트 앱이 있는 대규모 또는 복잡한 마이크로 서비스 기반 어플리케이션을 다자인하고 빌드하는 경우 API 게이트 웨이가 좋은 방법일 수 있음   
이 방법은 MSA 특정 그룹에 단일 진입점 서비스를 제공하는 방법으로 클라이언트 앱의 요구 사항을 고려하면서 작성되어 BFF(프런트 엔드의 백 엔드) 라고도 불림  
API 게이트웨이는 클라이언트 앱과 마이크로 서비스 사이에 위치하며 클라이언트에서 서비스로 요청을 라우팅하는 역방향 프록시로 사용된다  
이러한 API 게이트웨이를 이용해서 인증, SSL, 종료 및 캐시와 같은 추가 기능을 제공할 수 있음  
  
좀 더 자세한 내용은 아래 링크를 참조   
[MS - 클라이언트 MSA 직접 통신 vs API 게이트웨이 패턴](https://docs.microsoft.com/ko-kr/dotnet/architecture/microservices/architect-microservice-container-applications/direct-client-to-microservice-communication-versus-the-api-gateway-pattern)
  
- [Location Transparency (위치 투명성)](https://www.reactivemanifesto.org/ko/glossary#Location-Transparency)
위치 투명성은 실제 위치가 아닌 이름을 사용하여 네트워크 리소스를 식별하는 것으로 분산 시스템에서는 사용자가 리소스를 요청할 때, 해당 리소스의 이름을 이용하여 리소스를 제공할 수 있음을 의미한다  
ㄴ [위키 - Location_transparency](https://en.wikipedia.org/wiki/Location_transparency)     
리액티브 시스템에서는 비동기 메시지 기반으로 활성화된 공간 분리와 실행시간 인스턴스의 참조 분리로 위치 투명성을 달성한다  
  
띠라서 API 게이트웨이는 사용자의 요청에 해당하는 서비스 이름을 제공 받아서 리지스트리 서비스(서비스 저장소)에 요청해서 특정 서비스 주소를 확인할 수 있다   
서비스 레지스트리 패턴으로 각 서비스가 active 한지 판별하는 책임을 분리하고 클라이언트 측 디스커버리 패턴으로 구현했다 
- 서비스 레지스트리 : MSA 에서 서비스를 등록하는 전략을 의마하며 가장 단순한 예는 DNS 이며 전문화된 솔루션으로 zookeeper, etcd 가 있다 
- 서비스 디스커버리 패턴: MSA 를 클라우드 환경에 올리다 보면 각 서비스의 IP 가 동적으로 변경되는 경우가 많은데, 클라이언트 측에서 서비스의 위치(IP)를 알기 위한 기능을 말한다  
    * client-side : 클라이언트가 직접 서비스 레지스트리를 조회하여 서비스의 위치를 찾는 방법  
    ![client-side-service-discovery](images/client-side-service-discovery.png)
    * server-side : 아래 그림에서 처럼 서비스 앞 단에 proxy(로드 밸런서)를 넣어서 클라이언트는 로드 밸런서를 호출하면 로드 밸런서가 서비스 레지스트리에서 등록된 서비스의 위치를 반환하는 방법 
    ![server-side-service-discovery](images/server-side-service-discovery.png) 
    * 서비스 디스커버리의 전문호된 솔루션으로는 Netflix 의 Eureka 나 Hashicorp 의 Consul 과 같은 서비스가 있다  
출처: [조대협-서비스 디스커버리 패턴:](https://bcho.tistory.com/1252)
  
또한 아파치 카프카를 이용하여 메시지 기반 통신을 구현하고 독립적인 결제 서비스를 구축하여 장애 복원력을 구현한다  
응답성을 유지하기 위해서 주문 요청을 받자마자 우선 응답을 보낸 후 결제 요청, 결제 결과 안내 등은 비동기로 처리한다    
> MSA 관련된 디자인 패턴 자세한 내용 : [https://microservices.io/patterns/](https://microservices.io/patterns/)  

### Analytics 분야
온라인 웹 스토어와 같이 리액티브 시스템이 적절한 분야 중 하나는 Analytics 분야  
Analytics 분야는 엄청난 양의 데이터를 다루면서 런타임에 처리하고 사용자에게 실시간으로 통계를 제공한다  
  
예를들어 기지국 데이터를 기반으로 통신망을 감지한다고 해보자  
이 시스템을 구축하기 위해서 스트리밍 아키텍쳐를 사용한다  
![analytics](images/analytics.png) 
일반적으로 이런 시스템은 짧은 지연 시간과 높은 처리량이 특징이다  
따라서 통신 네트워크 상태에 대한 내용을 응답하거난 전달하는 능력이 매우 중요하다  
이런 시스템을 구축하기 위헤서는 **리액티브 선언문에서 언급한 기본원칙에 의존**해야 한다  
예를 들어 복원성 확보를 위해서는 Back Pressure(배압) 을 지원해야 한다  
- Back Pressure : 한 컴포넌트가 부하를 이겨내기 힘들 때, 시스템 전체가 합리적인 방법으로 대응하는 것으로 과부하 상태의 컴포넌트에 장애가 발생하지 않도록 상위 컴포넌트들이 알아서 제어  

## 왜 리액티브 스프링인가 ? 
JVM 세계에서는 리액티브 관련 여러 프레임 워크가 존재한다 
- Akka, Vert.x, RxJava, Reactor, etc
그렇다면 왜 스프링 리액티브 일까 ?


### 서비스 레벨에서의 응답성
![shoppingcard_service](images/shoppingcard_service.png)
위 그림과 같은 쇼핑 카드 서비스를 구성한다고 해보자  

- 명령형 프로그래밍(imperative-programing)
```java
public interface ShoppingCardService {
    Output calculate(Input value);
}

public class OrdersService {
    private final ShoppingCardService scService;

    void process() {
        Input input = new Input();
        // 동기적으로 호출하고 실행 직후 결과를 얻는 방식 
        // 해당 서비스가 blocking 
        Output output = scService.calculate(input);

        System.out.println(scService.getClass().getSimpleName() + " execution completed");
    }
}
```
위와 같은 blocking 방식은 리액티브 시스템에서 비동기 메시지 기반 원칙에 모순된다  

 
> 하나의 시스템은 작은 시스템의 조합으로 구성되기 때문에 각 요소들의 리액티브 특성에 의존하게 된다. 즉 리액티브 시스템 설계 원칙을 모든 구성 요소에 적용하고 합성할 수 있어야 함  