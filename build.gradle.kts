import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.0.4"
	id("io.spring.dependency-management") version "1.1.0"
	id("org.jmailen.kotlinter") version "3.13.0"
	id("maven-publish")
	id("java-library")
	kotlin("jvm") version "1.7.22"
	kotlin("plugin.spring") version "1.7.22"
}

group = "com.odenizturker"
version = "0.0.4"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
	mavenLocal()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.postgresql:r2dbc-postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

publishing {
	repositories {
		maven {
			name = "Gitlab"
			url = uri("https://gitlab.com/api/v4/projects/46769478/packages/maven")
			credentials(HttpHeaderCredentials::class.java) {
				name = "Deploy-Token"
				value = "n8av-ppL26gFP6XBi4Qf"
			}
			authentication {
				create("header", HttpHeaderAuthentication::class)
			}
		}
	}

	publications {
		create<MavenPublication>("artifact") {
			from(components["java"])
		}
	}
}


tasks.withType<Test> {
	useJUnitPlatform()
}
