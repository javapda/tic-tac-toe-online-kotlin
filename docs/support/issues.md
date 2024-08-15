# issues | [main README](../../README.md)

## missing [ContentNegotiation](https://ktor.io/docs/serialization.html)
* the ContentNegotiation plugin:
  * negotiates media types between the client and the server. For this, it uses the [Accept](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept) and [Content-Type](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Type) headers
  * serializing/deserializing the content in a specific format
  * Ktor supports the following formats out-of-the-box:
    * [JSON](https://developer.mozilla.org/en-US/docs/Learn/JavaScript/Objects/JSON) - io.ktor:ktor-serialization-kotlinx-json
    * [XML](https://developer.mozilla.org/en-US/docs/Web/XML/XML_introduction) - io.ktor:ktor-serialization-kotlinx-xml
    * [CBOR](https://cbor.io/) - io.ktor:ktor-serialization-kotlinx-cbor
    * ProtoBuf - io.ktor:ktor-serialization-kotlinx-protobuf
* You configure the ContentNegotiation in your Ktor Application module
* If not configured and you attempt to, for example, deserialize an object to JSON you may get an error like
following:
```
Response pipeline couldn't transform 'class tictactoeonline.HelpPayload' to the OutgoingContent
java.lang.IllegalArgumentException: Response pipeline couldn't transform 'class tictactoeonline.HelpPayload' to the OutgoingContent
	at io.ktor.server.engine.BaseApplicationResponse$Companion$setupSendPipeline$1.invokeSuspend(BaseApplicationResponse.kt:311)
	at io.ktor.server.engine.BaseApplicationResponse$Companion$setupSendPipeline$1.invoke(BaseApplicationResponse.kt)
	at io.ktor.server.engine.BaseApplicationResponse$Companion$setupSendPipeline$1.invoke(BaseApplicationResponse.kt)

```