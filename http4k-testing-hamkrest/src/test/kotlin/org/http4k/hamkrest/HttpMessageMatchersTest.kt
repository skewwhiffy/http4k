package org.http4k.hamkrest

import com.natpryce.hamkrest.equalTo
import org.http4k.core.Body
import org.http4k.core.ContentType.Companion.APPLICATION_FORM_URLENCODED
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.ContentType.Companion.TEXT_PLAIN
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.with
import org.http4k.lens.Header
import org.http4k.lens.string
import org.junit.Test

class HttpMessageMatchersTest {

    @Test
    fun `header`() = assertMatchAndNonMatch(Request(GET, "/").header("header", "bob"), hasHeader("header", "bob"), hasHeader("header", "bill"))

    @Test
    fun `headers`() = assertMatchAndNonMatch(Request(GET, "/").header("header", "bob").header("header", "bob2"), hasHeader("header", listOf("bob", "bob2")), hasHeader("header", listOf("bill")))

    @Test
    fun `header lens`() =
        Header.required("bob").let {
            assertMatchAndNonMatch(Request(GET, "/").with(it of "bob"), hasHeader(it, equalTo("bob")), hasHeader(it, equalTo("bill")))
        }

    @Test
    fun `content type`() = assertMatchAndNonMatch(Request(GET, "/").header("Content-Type", "application/json"), hasContentType(APPLICATION_JSON), hasContentType(APPLICATION_FORM_URLENCODED))

    @Test
    fun `body`() = assertMatchAndNonMatch(Request(GET, "/").body("bob"), hasBody("bob"), hasBody("bill"))

    @Test
    fun `body lens`() =
        Body.string(TEXT_PLAIN).toLens().let {
            assertMatchAndNonMatch(Request(GET, "/").with(it of "bob"), hasBody(it, equalTo("bob")), hasBody(it, equalTo("bill")))
        }
}