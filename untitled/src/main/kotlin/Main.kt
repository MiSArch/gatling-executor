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
    // This is the main entry point for the Gatling simulation
    // It will be executed when you run the simulation
    val simulation = BuyProcessLoadTest()
    simulation.run { }
}

class BuyProcessLoadTest : Simulation() {

    private val token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJoZm16d2Uyc1UxT05oUFNycmVEN2FyYVpXWmJudDRLNklRNjVLbkVlRTlFIn0.eyJleHAiOjE3NDQwMTg4NzcsImlhdCI6MTc0NDAxNzA3NywianRpIjoiODIzOTU5NDItODY2MS00M2RhLWIyMGYtMDQ3ZmE1NzNhNjQwIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgxL2tleWNsb2FrL3JlYWxtcy9NaXNhcmNoIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImI5MTY1NjMwLTQ1M2MtNGYyNS05MzFlLWZjMjQ5MzgxMmU0OCIsInR5cCI6IkJlYXJlciIsImF6cCI6ImZyb250ZW5kIiwic2Vzc2lvbl9zdGF0ZSI6IjdjZjkwODNmLTU2MmEtNDVmZS1iMzEzLTE4NmUyNmE3N2JiNCIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1taXNhcmNoIiwib2ZmbGluZV9hY2Nlc3MiLCJhZG1pbiIsInVtYV9hdXRob3JpemF0aW9uIiwiZW1wbG95ZWUiLCJidXllciJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsInNpZCI6IjdjZjkwODNmLTU2MmEtNDVmZS1iMzEzLTE4NmUyNmE3N2JiNCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6IkVsaWFzIE3DvGxsZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJlbGlhc211ZWxsZXIiLCJnaXZlbl9uYW1lIjoiRWxpYXMiLCJmYW1pbHlfbmFtZSI6Ik3DvGxsZXIiLCJlbWFpbCI6ImVsaWFzQG11ZWxsZXIta2xlaW5oZWluei5kZSJ9.K2YU848-SROCkWGwSSAKmv4yvTD8oyeRjcoKEKE19guNbStGTZ1-npHuAOEjCLPC8OsswITU-gkgCeBhnZPK79QZarB53K8lh1Zgq2wFvsPyqQGxk3A6_eHwjnazTgPvck8icYH1FZb1DS6fM0rGRtmKLCCh0ottR5sXAB3PUAAiet-S9K-cdHiEqb4iDpQHVbgQgqDrY5Ykosr-IBrNm2FM8IgzDYnMzWkodEbX0ltSKeapjcdEwHh9KvcLHWfjqLpF1zK7ItdJwXTj0HD9l8cxxAQDq2JPnhrv76ebx21Tpy0bIaWVItZpW-xYzFEWUfKJP-gcGMmJnAztStAKtQ"

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
                session.set("productsQuery", "{ \"query\": \"query { products(filter: { isPubliclyVisible: true }, first: 10, orderBy: { direction: ASC, field: ID }, skip: 0) { hasNextPage nodes { id internalName isPubliclyVisible } totalCount } }\" }")
            }
            .exec(
                http("products").post("/graphql")
                    .body(StringBody("#{productsQuery}"))
                    .check(jsonPath("$.data.products.nodes[0].id").saveAs("productId"))
            )
            .pause(7)
            .exec { session ->
                val productId = session.getString("productId")
                session.set("productQuery", "{ \"query\": \"query { product(id: \\\"$productId\\\") { categories { hasNextPage  totalCount } defaultVariant {  id isPubliclyVisible     averageRating } id internalName isPubliclyVisible variants { hasNextPage  totalCount } } }\" }")
            }
            .exec(http("product").post("/graphql").body(StringBody("#{productQuery}")))
            .pause(Duration.ofMillis(100))
            .exec { session ->
                session.set("usersQuery", "{ \"query\": \"query { users(first: 10, orderBy: { direction: ASC, field: ID }, skip: 0) { hasNextPage nodes {  id  birthday dateJoined gender username  } totalCount } }\" }")
            }
            .exec(
                http("users").post("/graphql")
                    .body(StringBody("#{usersQuery}"))
                    .check(jsonPath("$.data.users.nodes[0].id").saveAs("userId")))
            .pause(Duration.ofMillis(100))
            .exec { session ->
                val userId = session.getString("userId")
                session.set("shoppingCartQuery", "{ \"query\": \"query { user(id: \\\"$userId\\\") { addresses { hasNextPage  totalCount } id notifications { hasNextPage  totalCount } reviews {  hasNextPage totalCount } shoppingcart { lastUpdatedAt shoppingcartItems { nodes { id productVariant { id} } } } birthday dateJoined firstName gender lastName username wishlists {  hasNextPage totalCount } } }\" }")
            }
            .exec(http("shoppingCart").post("/graphql").body(StringBody("#{shoppingCartQuery}")))
    init {
        setUp(buyProcessScenario.injectOpen(atOnceUsers(10)).protocols(httpProtocol))
    }
}