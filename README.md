# Proof-of-concept Redis key-value store. 
# Topics touched: 
* multi-threading 
* sockets
* LRU cache
* parser

# The protocol and a couple of message examples
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
S: *2\r\n   
S: +ERROR\r\n   
S: ${nr of bytes of the string}\r\n     
S: {exception as bulk string}\r\n    

___
## TO-DO:
* Add support for arrays with different data types, for example arrays of strings and integers
* Add support for nested data types, for example nested arrays
* Add support for MSET and MGET
* Add support for multiple commands aka pipelining