module com.github.mvysny.unsigned {
    // The package is implemented in Kotlin; build.gradle.kts patches the Kotlin output into this module so
    // javac can see it. An IDE may still flag "Package is Empty" here - that's the same blind spot, and is OK.
    exports com.github.mvysny.unsigned;
    requires kotlin.stdlib;
}
