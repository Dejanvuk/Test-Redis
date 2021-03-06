# Proof-of-concept Redis key-value store. 
# Topics touched: 
* multi-threading 
* sockets
* LRU cache
* parser

___
## Current features:
* Support for GET,SET,DELETE,RENAME commands using the CLI
* LRU support by default
* Parser library to decode and encode commands
* Dockerized 

## How to run

You can change the REDIS_PORT and REDIS_HOST(only for the client) system environment variables, otherwise the defaults are '6379' and 'localhost'

Create the network: docker network create redis-network

Server: docker run --network-alias redisserver --network redis-network -p 6379:6379 -it redisserver

Client: docker run --network redis-network -it redisclient

## Running Tests

```
mvn test
```
Test Coverage:
1. Parser ~78%
2. CLI 0%
3. Server 0%


# POC

![](redis-poc.png)

# The protocol and a couple of message examples, by default UTF-8
[Redis Protocol](https://redis.io/docs/reference/protocol-spec/)
#### Client requests for **SET("abcd", 123456)**
C: *3\r\n   
C: $3\r\n   
C: SET\r\n  
C: $4\r\n   
C: abcd\r\n     
C: :123456\r\n

#### Client requests for **SET("array", {1, 22, 3})**
C: *5\r\n   
C: $3\r\n   
C: SET\r\n  
C: $4\r\n   
C: abcd\r\n
S: :1\r\n   
S: :22\r\n  
S: :3\r\n

#### Empty **OK** send back to the client for *SET* and *DELETE* messages
S: *1\r\n   
S: +OK\r\n  

#### **OK** with data send back to the client for *GET* message
S: *{nr of messages}\r\n    
S: +OK\r\n  
S: {data}   

{data} will be non-array or an array or a nested array <br />
{nr of messages} is 2 for non-array, and for array is the array size + 1 to account for the OK simple string<br />        

#### Client requests for **GET("abcd")**
C: *2\r\n   
C: $3\r\n   
C: GET\r\n  
C: $4\r\n   
C: abcd\r\n

#### Server response for **GET("abcd")**
S: *2\r\n   
S: +OK\r\n  
S: :123456\r\n

#### Server response for **SET("array", {1,22,3}) , arrays of different types aren't supported yet**
S: *1\r\n   
S: +OK\r\n

#### Server response for **GET("array")**
S: *4\r\n   
S: +OK\r\n  
S: :1\r\n   
S: :22\r\n  
S: :3\r\n   

#### **ERROR** message alongside the exception
S: *1\r\n   
S: -{ERROR} {exception as bulk string}\r\n   



## Ways to know when the message ended
1) Simply close the connection (inneficient, especially for CLI or during high traffic)
2) End the message with a unique end-of-message marker
3) The length is being specified by the protocol(In our case we know that the message is always an array with a pre-determined length specified by the 2nd byte)

Needless to say, the Redis app uses the 3rd way which is the most efficient in this case.
___
## SHORT TERM TO-DO's:
- [ ] Add Lombok to the models
- [ ] Change the packet name '.parser' in the server 
- [ ] If key was already present when using SET, send an OK message saying the key was already present back
- [ ] Add 'TOP' command and 'TOP n most recently used & least recently used' command 

## EXTRA TO-DO's:
- [x] Create the CLI
- [x] Add support for arrays with different data types, for example arrays of strings and integers (Partial, only parser support is missing)
- [ ] Add support for nested arrays
- [x] Add support for RENAME
- [ ] Add support for MSET and MGET
- [ ] Add support for the data types: Sets&Sorted Sets and Hashes
- [x] Add LRU support
- [x] Dockerized
- [ ] Add LFU support
- [ ] Add support for keys with limited time to live (Partial, still thinking on the best approach to implement this to least affect the performance)
- [ ] Add support for multiple databases
- [ ] Read configurations from redis.conf or from remote repository
- [ ] Add serializer/deserializer to persist data on disk if needed
- [ ] Add support for multiple commands aka pipelining
- [ ] Separate the java CLI into an SDK
- [ ] Port the SDK interface to JS/TS and default to HTTP if websockets are not enabled in the browser

## Contributing
1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request