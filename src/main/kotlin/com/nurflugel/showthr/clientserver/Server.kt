//package com.nurflugel.showthr
//
//import io.ktor.http.ContentType
//import io.ktor.server.application.install
//import io.ktor.server.engine.embeddedServer
//import io.ktor.server.netty.Netty
//import io.ktor.server.routing.Routing
//import java.io.File
//
////import io.ktor.samples.fullstack.common.*
//import io.ktor.server.application.*
//import io.ktor.server.engine.*
//import io.ktor.server.html.*
//import io.ktor.server.http.content.*
//import io.ktor.server.netty.*
//import io.ktor.server.response.*
//import io.ktor.server.routing.*
//import kotlinx.html.*
//import java.io.*
//import io.ktor.server.routing.*
//
//fun getCommonWorldString() = "common-world"
//
//fun main() {
//    embeddedServer(Netty, port = 8080) { main() }.start(wait = true)
//}
//
//fun Application.main(){
//
////    @JvmStatic
//    fun main(args: Array<String>) {
//
//        val currentDir = File(".").absoluteFile
////        environment.log.info("Current directory: $currentDir")
//
//        routing {
//            get("/") {
//                call.respondHtml {
//                    body {
//                        +"Hello ${getCommonWorldString()} from Ktor"
//                        div {
//                            id = "js-response"
//                            +"Loading..."
//                        }
//                        script(src = "/static/output.js") {
//                        }
//                    }
//                }
//            }
//            get("/test") {
//                call.respond("I am a test response")
//            }
//            staticResources(remotePath = "/static", basePackage = null)
//        }
//    }
//}