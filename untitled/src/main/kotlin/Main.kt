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
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*

import java.time.Duration

fun main() {
    val buyProcessLoadTest = BuyProcessLoadTest()
    buyProcessLoadTest.run {}
}

class BuyProcessLoadTest : Simulation() {

    private val token =
        "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI3NTRZNmxZRFBqcjB0Y2QyNVhpdTJCcFROcUJWTVIybHp1djNRUURfcHBJIn0.eyJleHAiOjE3NDQxNzk0NzksImlhdCI6MTc0NDE3NzY3OSwianRpIjoiZTljOGM2NGMtYzMyMy00MzJhLWFjNTUtMzMyOWU3YzZiMTRiIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgxL2tleWNsb2FrL3JlYWxtcy9NaXNhcmNoIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImI0ZTg5NWRhLThkYzEtNDc2Mi1hNDFhLWEyMDg3MjZjYzI1NyIsInR5cCI6IkJlYXJlciIsImF6cCI6ImZyb250ZW5kIiwic2Vzc2lvbl9zdGF0ZSI6IjU5ZjdiYWI0LTdmMjktNDA3YS1hYThlLTI0MDEzN2E4MzUyZSIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1taXNhcmNoIiwib2ZmbGluZV9hY2Nlc3MiLCJhZG1pbiIsInVtYV9hdXRob3JpemF0aW9uIiwiZW1wbG95ZWUiLCJidXllciJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsInNpZCI6IjU5ZjdiYWI0LTdmMjktNDA3YS1hYThlLTI0MDEzN2E4MzUyZSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6IkVsaWFzIE3DvGxsZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJlbGlhc211ZWxsZXIiLCJnaXZlbl9uYW1lIjoiRWxpYXMiLCJmYW1pbHlfbmFtZSI6Ik3DvGxsZXIiLCJlbWFpbCI6ImVsaWFzQG11ZWxsZXIta2xlaW5oZWluei5kZSJ9.TXJwGmvqi2X1NJaC3ZhRxLOopESIZ8YK0TCK0dpj2XZTH4FPWq3KrcTDA45GJdoOXFvmUOmAz9WN7-deAQAd4SDa2z3JAEJJUiZK8EI1LCtzMHrTvWJ6_zvmgAvRZDyf-Q81yjfsjrW3UvKGgpbW2ku-vWuEfe-D7SyLbqOVQimv6RlXJYTT9yZFDRsRCg_zhT4Xlbwuy4ojvUfD76lXzPHu2ONQocAUYXxSnHNZwrpiQ1d1EX94fYfW8HEePDZCj5ZNV-sWiyZZbY9a0DgneeBj2ZVPw3_t32cx53FSBsoRWS3lGLw7EiAWMbVUd5eYh5ttgjbhR5DIc9rIjwP6LA"

    private val httpProtocol =
        http
            .baseUrl("http://localhost:8080")
            .authorizationHeader("Bearer $token")
            .contentTypeHeader("application/json")
            .doNotTrackHeader("1")
            .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

    private val buyProcessScenario =
        scenario("Buy Process")
            .exec { session ->
                session.set(
                    "productsQuery",
                    "{ \"query\": \"query { products(filter: { isPubliclyVisible: true }, first: 10, orderBy: { direction: ASC, field: ID }, skip: 0) { hasNextPage nodes { id internalName isPubliclyVisible } totalCount } }\" }"
                )
            }
            .exec(
                http("products").post("/graphql")
                    .body(StringBody("#{productsQuery}"))
                    .check(jsonPath("$.data.products.nodes[0].id").saveAs("productId"))
            )
            .pause(7)
            .exec { session ->
                val productId = session.getString("productId")
                session.set(
                    "productQuery",
                    "{ \"query\": \"query { product(id: \\\"$productId\\\") { categories { hasNextPage  totalCount } defaultVariant { id isPubliclyVisible averageRating } id internalName isPubliclyVisible variants { hasNextPage  totalCount } } }\" }"
                )
            }
            .exec(
                http("product").post("/graphql")
                    .body(StringBody("#{productQuery}"))
                    .check(jsonPath("$.data.product.defaultVariant.id").saveAs("productVariantId"))
            )
            .pause(Duration.ofMillis(100))
            .exec { session ->
                session.set(
                    "usersQuery",
                    "{ \"query\": \"query { users(first: 10, orderBy: { direction: ASC, field: ID }, skip: 0) { hasNextPage nodes {  id  birthday dateJoined gender username addresses { nodes { id } } } } }\" }"
                )
            }
            .exec(
                http("users").post("/graphql")
                    .body(StringBody("#{usersQuery}"))
                    .check(jsonPath("$.data.users.nodes[0].addresses.nodes[0].id").saveAs("addressId"))
                    .check(jsonPath("$.data.users.nodes[0].id").saveAs("userId"))
            )
            .pause(Duration.ofMillis(100))
            .exec { session ->
                val userId = session.getString("userId")
                val productVariantId = session.getString("productVariantId")
                session.set(
                    "createShoppingcartItem",
                    "{ \"query\": \"mutation { createShoppingcartItem( input: { id: \\\"$userId\\\" shoppingCartItem: { count: 1 productVariantId: \\\"$productVariantId\\\" } } ) { id } }\" }"
                )
            }.exec (
                http("createShoppingcartItem").post("/graphql")
                    .body(StringBody("#{createShoppingcartItem}"))
                    .check(jsonPath("$.data.createShoppingcartItem.id").saveAs("createShoppingcartItemId"))
            )
    // get shipment methods
    // get payment methods
    // create order
    // place order



    init {
        setUp(buyProcessScenario.injectOpen(rampUsers(100).during(Duration.ofSeconds(50))).protocols(httpProtocol),)
    }
}

// TODO Real World Data for the pauses
// TODO Fuzzing

// Open Injection Patterns
// rampUsers(100).during(Duration.ofSeconds(30))
// constantUsersPerSec(10.0).during(Duration.ofSeconds(60))
// rampUsersPerSec(1.0).to(10.0).during(Duration.ofSeconds(30))
// stressPeakUsers(200)
// heavisideUsers(100).during(Duration.ofSeconds(20)

// Closed Injection Patterns
// constantConcurrentUsers(50).during(Duration.ofMinutes(5))
// rampConcurrentUsers(10).to(100).during(Duration.ofMinutes(10))

// Combination
// setUp(
//    buyProcessScenario.injectOpen(
//        atOnceUsers(10),
//        rampUsers(50).during(Duration.ofSeconds(30)),
//        constantUsersPerSec(20.0).during(Duration.ofMinutes(2))
//    ).protocols(httpProtocol)
//)