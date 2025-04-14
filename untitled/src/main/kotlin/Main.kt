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
        System.getenv("ACCESS_TOKEN") ?: throw IllegalStateException("Environment variable ACCESS_TOKEN is not set")
    private val baseUrl =
        System.getenv("BASE_URL") ?: throw IllegalStateException("Environment variable BASE_URL is not set")
    private val testUUID =
        System.getenv("TEST_UUID") ?: throw IllegalStateException("Environment variable TEST_UUID is not set")

    private val httpProtocol =
        http
            .baseUrl(baseUrl)
            .authorizationHeader("Bearer $token")
            .contentTypeHeader("application/json")
            .doNotTrackHeader("1")
            .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

    private val waitForTriggerScenario =
        scenario("Wait for Trigger")
            .asLongAsDuring({ session ->
                session.getString("triggerResponse") != "true"
            }, Duration.ofSeconds(30)) // Timeout after 30s
            .on(
                exec(
                    http("Check Trigger")
                        .get("http://localhost:8888/trigger/$testUUID")
                        .check(bodyString().saveAs("triggerResponse"))
                ).pause(Duration.ofMillis(100))
            )

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
                    "createShoppingcartItemMutation",
                    "{ \"query\": \"mutation { createShoppingcartItem( input: { id: \\\"$userId\\\" shoppingCartItem: { count: 1 productVariantId: \\\"$productVariantId\\\" } } ) { id } }\" }"
                )
            }.exec(
                http("createShoppingcartItemMutation").post("/graphql")
                    .body(StringBody("#{createShoppingcartItemMutation}"))
                    .check(jsonPath("$.data.createShoppingcartItem.id").saveAs("createShoppingcartItemId"))
            ).exec { session ->
                session.set(
                    "shipmentMethodsQuery",
                    "{ \"query\": \"query { shipmentMethods { totalCount nodes { id name baseFees description feesPerItem feesPerKg } } }\" }",
                )
            }.exec(
                http("shipmentMethodsQuery").post("/graphql")
                    .body(StringBody("#{shipmentMethodsQuery}"))
                    .check(jsonPath("$.data.shipmentMethods.nodes[0].id").saveAs("shipmentMethodId"))
            ).exec { session ->
                session.set(
                    "paymentInformationsQuery",
                    "{ \"query\": \"query { paymentInformations { nodes { id paymentMethod publicMethodDetails payments { nodes { id status totalAmount numberOfRetries payedAt } } } } }\" }",
                )
            }.exec(
                http("paymentInformationsQuery").post("/graphql")
                    .body(StringBody("#{paymentInformationsQuery}"))
                    .check(jsonPath("$.data.paymentInformations.nodes[0].id").saveAs("paymentInformationId"))
            ).exec { session ->
                val userId = session.getString("userId")
                val addressId = session.getString("addressId")
                val createShoppingcartItemId = session.getString("createShoppingcartItemId")
                val shipmentMethodId = session.getString("shipmentMethodId")
                val paymentInformationId = session.getString("paymentInformationId")
                session.set(
                    "createOrderMutation",
                    "{ \"query\": \"mutation { createOrder( input: { userId: \\\"$userId\\\" orderItemInputs: { shoppingCartItemId: \\\"$createShoppingcartItemId\\\" couponIds: [] shipmentMethodId: \\\"$shipmentMethodId\\\" } vatNumber: \\\"AB1234\\\" invoiceAddressId: \\\"$addressId\\\" shipmentAddressId: \\\"$addressId\\\" paymentInformationId: \\\"$paymentInformationId\\\" } ) { id paymentInformationId placedAt } }\" }"
                )
            }.exec(
                http("createOrderMutation").post("/graphql")
                    .body(StringBody("#{createOrderMutation}"))
                    .check(jsonPath("$.data.createOrder.id").saveAs("createOrderId"))
            ).exec { session ->
                val createOrderId = session.getString("createOrderId")
                session.set(
                    "placeOrderMutation",
                    "{ \"query\": \"mutation { placeOrder(input: { id: \\\"$createOrderId\\\", paymentAuthorization: { cvc: 123 } }) { id } }\" }"
                )
            }.exec(
                http("placeOrderMutation").post("/graphql")
                    .body(StringBody("#{placeOrderMutation}"))
            )

    init {
        setUp(
            waitForTriggerScenario.injectOpen(atOnceUsers(1)).andThen(
                buyProcessScenario.injectOpen(rampUsers(100).during(Duration.ofSeconds(50))).protocols(httpProtocol)
            ))
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