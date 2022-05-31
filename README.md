# Proof-of-concept Redis key-value store. 
# Topics touched: 
* multi-threading 
* sockets
* LRU cache
* parser

___
## Current features:
* 

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

{data} will be non-array, however later we will add support to parse complex nested arrays and data<br /> 
if {data} is array, get each message from the array <br />
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
## EXTRA TO-DO's:
- [x] Create the CLI
- [x] Add support for arrays with different data types, for example arrays of strings and integers
- [ ] Add support for nested arrays
- [x] Add support for RENAME
- [ ] Add support for MSET and MGET
- [ ] Add support for the data types: Sets&Sorted Sets and Hashes
- [ ] Add LRU support
- [ ] Add LFU support
- [ ] Add support for keys with limited time to live
- [ ] Add support for multiple databases
- [ ] Read configurations from redis.conf or from remote repository
- [ ] Add serializer/deserializer to persist data on disk if needed
- [ ] Add support for multiple commands aka pipelining
- [ ] Port the sdk interface to JS/TS and default to HTTP if websockets are not enabled in the browser