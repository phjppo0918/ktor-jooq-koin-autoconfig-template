package com.example

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ApplicationTest :
    StringSpec({

        "hello world" {
            "a" shouldBe "a"
        }

        "hello world2" {
            "b" shouldBe "a"
        }
    })
