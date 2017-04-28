package org.reekwest.http.core

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.reekwest.http.core.ContentType.Companion.APPLICATION_XML
import org.reekwest.http.core.ContentType.Companion.TEXT_HTML
import org.reekwest.http.core.Method.GET
import org.reekwest.http.core.ResourceLoader.Companion.Classpath
import org.reekwest.http.core.Status.Companion.NOT_FOUND
import org.reekwest.http.core.Status.Companion.OK
import org.reekwest.http.core.Uri.Companion.uri
import org.reekwest.http.core.body.bodyString

class StaticContentTest {

    private val pkg = this.javaClass.`package`.name.replace('.','/')

    @Test
    fun `looks up contents of existing root file`() {
        val handler = StaticContent("/svc")
        val result = handler(Request(GET, uri("/svc/mybob.xml")))
        assertThat(result.bodyString(), equalTo("<xml>content</xml>"))
        assertThat(result.header("Content-Type"), equalTo(APPLICATION_XML.value))
    }

    @Test
    fun `defaults to index html if is no route`() {
        val handler = StaticContent("/svc")
        val result = handler(Request(GET, uri("/svc")))
        assertThat(result.status, equalTo(OK))
        assertThat(result.bodyString(), equalTo("hello from the root index.html"))
        assertThat(result.header("Content-Type"), equalTo(TEXT_HTML.value))
    }

    @Test
    fun `defaults to index html if is no route - non-root-context`() {
        val handler = StaticContent("/svc", Classpath("org"))
        val result = handler(Request(GET, uri("/svc")))
        assertThat(result.status, equalTo(OK))
        assertThat(result.bodyString(), equalTo("hello from the io index.html"))
        assertThat(result.header("Content-Type"), equalTo(TEXT_HTML.value))
    }

    @Test
    fun `non existing index html if is no route`() {
        val handler = StaticContent("/svc", Classpath("org/reekwest"))
        val result = handler(Request(GET, uri("/svc")))
        assertThat(result.status, equalTo(NOT_FOUND))
    }

    @Test
    fun `looks up contents of existing subdir file - non-root context`() {
        val handler = StaticContent("/svc")
        val result = handler(Request(GET, uri("/svc/$pkg/StaticModule.js")))
        assertThat(result.status, equalTo(OK))
        assertThat(result.bodyString(), equalTo("function hearMeNow() { }"))
        assertThat(result.header("Content-Type"), equalTo("application/javascript"))
    }

    @Test
    fun `looks up contents of existing subdir file`() {
        val handler = StaticContent("")
        val result = handler(Request(GET, uri("/$pkg/StaticModule.js")))
        assertThat(result.status, equalTo(OK))
        assertThat(result.bodyString(), equalTo("function hearMeNow() { }"))
        assertThat(result.header("Content-Type"), equalTo("application/javascript"))
    }

    @Test
    fun `can alter the root path`() {
        val handler = StaticContent("/svc", Classpath(pkg))
        val result = handler(Request(GET, uri("/svc/StaticModule.js")))
        assertThat(result.status, equalTo(OK))
        assertThat(result.bodyString(), equalTo("function hearMeNow() { }"))
        assertThat(result.header("Content-Type"), equalTo("application/javascript"))
    }

    @Test
    fun `looks up non existent-file`() {
        val handler = StaticContent("/svc", Classpath())
        val result = handler(Request(GET, uri("/svc/NotHere.xml")))
        assertThat(result.status, equalTo(NOT_FOUND))
    }

    @Test
    fun `cannot serve a directory`() {
        val handler = StaticContent("/svc", Classpath())
        val result = handler(Request(GET, uri("/svc/org")))
        println(result)
        assertThat(result.status, equalTo(NOT_FOUND))
    }

    @Test
    fun `looks up non existent path`() {
        val handler = StaticContent("/svc")
        val result = handler(Request(GET, uri("/bob/StaticModule.js")))
        assertThat(result.status, equalTo(NOT_FOUND))
    }

    @Test
    fun `can't subvert the path`() {
        val handler = StaticContent("/svc")
        assertThat(handler(Request(GET, uri("/svc/../svc/Bob.xml"))).status, equalTo(NOT_FOUND))
        assertThat(handler(Request(GET, uri("/svc/~/.bashrc"))).status, equalTo(NOT_FOUND))
    }
}