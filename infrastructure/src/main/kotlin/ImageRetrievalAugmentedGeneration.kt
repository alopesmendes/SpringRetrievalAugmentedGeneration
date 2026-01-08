package com.ailtontech

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@EnableMongoRepositories
@SpringBootApplication
class ImageRetrievalAugmentedGeneration

fun main(args: Array<String>) {
    runApplication<ImageRetrievalAugmentedGeneration>(*args)
}
