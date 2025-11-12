###          Internal Request Flow
Let’s say your Accounts Service calls:
cardsFeignClient.fetchCardsDetails("9876543210");

1️⃣ **Feign Proxy Invocation**
->When the application starts, Spring Boot creates a Feign dynamic proxy for your CardsFeignClient interface.
->When you call the method, Feign doesn’t call it directly — instead it:
->Looks at the @FeignClient(name="CARDS") annotation.
->Prepares an HTTP request template:
  => GET /api/fetch?mobileNumber=1234567890

2️⃣ **Service Name Resolution**
->Feign delegates the request to Spring Cloud LoadBalancer (or Ribbon in older setups).
->Instead of a fixed URL, it has the logical name CARDS.
->Spring Cloud LoadBalancer calls the DiscoveryClient bean (Eureka client in this case).
->Pseudocode:
  =>List<ServiceInstance> instances = discoveryClient.getInstances("CARDS");

3️⃣ **Eureka Lookup**
->The Eureka client inside Accounts Service has a local cache of the service registry (synced every ~30s from Eureka Server).
->It fetches the list of instances for CARDS:
[
{ host: "192.168.1.11", port: 8090, metadata: {...} },
{ host: "192.168.1.13", port: 8090, metadata: {...} }
]

4️⃣ **Load Balancing Decision**
->Spring Cloud LoadBalancer picks one instance (round robin by default):
->chosenInstance = "192.168.1.11:8090"

5️⃣ **Full URL Construction**
->Feign builds the full HTTP request:
GET http://192.168.1.21:9000/api/fetch?mobileNumber=9876543210

6️⃣ **HTTP Request Execution**
->Feign uses an underlying HTTP client (usually ApacheHttpClient or OkHttpClient) to send the request over TCP/IP.

7️⃣ **Cards Service Processing**
->Cards Service receives the request.
->Spring MVC maps /api/fetch to a controller method.
->Business logic runs and returns a JSON response.

8️⃣ **Response Handling in Feign**
->Feign converts the JSON to your method’s return type (List<Card>).
->Returns it to your service method in Accounts Service.

9️⃣ **Final Response to Caller**
->Accounts Service processes the data and sends it to:
->Another service, or A frontend client (React, Angular, etc.)

Accounts Service              Feign Client       LoadBalancer        Eureka Client        Eureka Server         Cards Service
|                            |                  |                   |                   |                   |
|--- fetchCard("12345") ----->|                  |                   |                   |                   |
|                            |---- resolve ---->|                   |                   |                   |
|                            |                  |---- getInstances("CARDS") ----------->|                   |
|                            |                  |                   |<--- instance list-|                   |
|                            |<--- instance ----|                   |                   |                   |
|                            |--- build URL ----|                   |                   |                   |
|                            |--- HTTP GET --------------------------------------------------------------->|
|                            |                                                                              |
|<--------------------------- JSON Response ---------------------------------------------------------------|
|                                                                                                           |
