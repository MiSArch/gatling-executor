/*
 * Copyright 2011-2024 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.misarch

import io.gatling.javaapi.core.*
import io.gatling.javaapi.http.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*

import java.time.Duration

fun main() {
    // This is the main entry point for the Gatling simulation
    // It will be executed when you run the simulation
    val simulation = KotlinBasicSimulation()
    simulation.run { }
}

class KotlinBasicSimulation : Simulation() {

    val httpProtocol =
        http
            // Here is the root for all relative URLs
            .baseUrl("http://localhost:8080")
            // Here are the common headers
            .contentTypeHeader("application/json")
            .doNotTrackHeader("1")
            //.acceptLanguageHeader("en-US,en;q=0.5")
            //.acceptEncodingHeader("gzip, deflate")
            .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

    // A scenario is a chain of requests and pauses
    val scn =
        scenario("Scenario Name")
            .exec(
                http("request_1").post("/graphql")
                    .body(StringBody("{ \"query\": \"query { products(filter: { isPubliclyVisible: true }, first: 10, orderBy: { direction: ASC, field: ID }, skip: 0) { hasNextPage nodes {   id internalName isPubliclyVisible  } totalCount } }\" }"))
            )
            // Note that Gatling has recorded real time pauses
            .pause(7)
            .exec(
                http("request_2").post("/graphql")
                    .body(StringBody("{ \"query\": \"query { categories(first: 10, orderBy: { direction: ASC, field: ID }, skip: 0) { hasNextPage nodes { description id name } totalCount } }\" }"))
            )


    init {
        setUp(scn.injectOpen(atOnceUsers(10)).protocols(httpProtocol))
    }
}
