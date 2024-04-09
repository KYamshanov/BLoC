import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    `maven-publish`
    signing
}

publishing {
    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(tasks.register("${name}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
            archiveAppendix.set(this@withType.name)
        })

        // Provide artifacts information required by Maven Central
        pom {
            name.set("BLoC architect framework library")
            description.set("Business logic components framework library")
            url.set("https://github.com/KYamshanov/BLoC")

            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("https://opensource.org/license/apache-2-0")
                }
            }
            developers {
                developer {
                    id.set("KYamshanov")
                    name.set("Konstantin Yamshanov")
                    organization.set("kyamshanov")
                    organizationUrl.set("https://kyamshanov.ru/")
                }
            }
            scm {
                url.set("https://github.com/KYamshanov/BLoC")
            }
        }
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        useGpgCmd()
        sign(publishing.publications)
    }
}
